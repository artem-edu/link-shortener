package mephi.service;

import mephi.model.Link;
import mephi.model.User;
import mephi.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredLinksCleanerTest {
    private static final int CHECK_INTERVAL_MS = 50;

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private UserService userService;
    @Mock
    private Consumer<String> notifier;

    private Link expiredLink(UUID userId, String hash) {
        return Link.create(userId, "https://example.com/" + hash, hash, 10)
                .toBuilder()
                .expiresAt(System.currentTimeMillis() - 1000)
                .build();
    }

    private ExpiredLinksCleaner cleanerWithFastInterval() {
        try (MockedStatic<ConfigService> config = mockStatic(ConfigService.class)) {
            config.when(() -> ConfigService.getIntValue("CLEANUP_INTERVAL_MS"))
                    .thenReturn(CHECK_INTERVAL_MS);
            return new ExpiredLinksCleaner(linkRepository, notifier, userService);
        }
    }

    @Test
    void deletesExpiredLinkAndNotifiesOwner() {
        UUID userId = UUID.randomUUID();
        Link link = expiredLink(userId, "hash1");
        when(linkRepository.getAllExpired()).thenReturn(List.of(link));
        when(userService.getCurrentUser()).thenReturn(Optional.of(new User(userId)));

        ExpiredLinksCleaner cleaner = cleanerWithFastInterval();
        try {
            cleaner.start();

            verify(linkRepository, timeout(2000)).delete(link);
            verify(notifier, timeout(2000)).accept(contains("удалена"));
        } finally {
            cleaner.stop();
        }
    }

    @Test
    void doesNotNotifyOtherUser() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Link link = expiredLink(userId, "hash1");
        when(linkRepository.getAllExpired()).thenReturn(List.of(link));
        when(userService.getCurrentUser()).thenReturn(Optional.of(User.create()));

        ExpiredLinksCleaner cleaner = cleanerWithFastInterval();
        try {
            cleaner.start();
            Thread.sleep(CHECK_INTERVAL_MS * 5);

            verify(linkRepository, atLeastOnce()).delete(link);
            verify(notifier, never()).accept(anyString());
        } finally {
            cleaner.stop();
        }
    }

    @Test
    void ignoresRepositoryFailure() throws InterruptedException {
        when(linkRepository.getAllExpired()).thenThrow(new RuntimeException("db error"));

        ExpiredLinksCleaner cleaner = cleanerWithFastInterval();
        try {
            cleaner.start();
            Thread.sleep(CHECK_INTERVAL_MS * 5);

            verify(linkRepository, never()).delete(any(Link.class));
            verify(notifier, never()).accept(anyString());
        } finally {
            cleaner.stop();
        }
    }
}
