package mephi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import mephi.dto.CreateLink;
import mephi.dto.EditLink;
import mephi.model.Link;
import mephi.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {
  private static final UUID USER_ID = UUID.randomUUID();

  @Mock private LinkRepository linkRepository;
  @Mock private BrowserService browserService;

  private LinkService linkService;

  @BeforeEach
  void setUp() {
    linkService = new LinkService(linkRepository, browserService);
  }

  private Link activeLink(String hash, int maxClicks, int clickCount) {
    return Link.create(USER_ID, "https://example.com", hash, maxClicks).toBuilder()
        .clickCount(clickCount)
        .expiresAt(System.currentTimeMillis() + 100_000)
        .build();
  }

  private CreateLink dto(String url) {
    return new CreateLink(url, 10, USER_ID);
  }

  @Test
  void createSavesLinkWithUniqueHash() {
    when(linkRepository.findByHash(anyString())).thenReturn(Optional.empty());
    when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Link link = linkService.create(dto("https://example.com"));

    assertEquals("https://example.com", link.url());
    assertEquals(USER_ID, link.userId());
    assertEquals(5, link.hash().length());
    verify(linkRepository).save(link);
  }

  @Test
  void createThrowsOnInvalidUrl() {
    assertThrows(RuntimeException.class, () -> linkService.create(dto("not-a-url")));
    assertThrows(RuntimeException.class, () -> linkService.create(dto("ftp://example.com")));
    verify(linkRepository, never()).save(any(Link.class));
  }

  @Test
  void goToOpensBrowserAndIncrementsClicks() {
    Link link = activeLink("abcde", 10, 0);
    when(linkRepository.findByHash("abcde")).thenReturn(Optional.of(link));
    when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

    linkService.goTo("abcde");

    verify(browserService).open("https://example.com");
    verify(linkRepository).save(argThat(l -> l.clickCount() == 1));
  }

  @Test
  void goToThrowsWhenLinkNotFound() {
    when(linkRepository.findByHash("abcde")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> linkService.goTo("abcde"));
  }

  @Test
  void goToDeletesAndThrowsWhenLinkExpired() {
    Link expired =
        Link.create(USER_ID, "https://example.com", "abcde", 10).toBuilder()
            .expiresAt(System.currentTimeMillis() - 1000)
            .build();
    when(linkRepository.findByHash("abcde")).thenReturn(Optional.of(expired));

    assertThrows(RuntimeException.class, () -> linkService.goTo("abcde"));

    verify(linkRepository).delete(expired);
    verify(browserService, never()).open(anyString());
  }

  @Test
  void goToThrowsWhenClickLimitExhausted() {
    Link exhausted = activeLink("abcde", 1, 1);
    when(linkRepository.findByHash("abcde")).thenReturn(Optional.of(exhausted));

    assertThrows(RuntimeException.class, () -> linkService.goTo("abcde"));

    verify(browserService, never()).open(anyString());
  }

  @Test
  void editSavesUpdatedLink() {
    Link link = Link.create(USER_ID, "https://example.com", "abcde", 10);
    when(linkRepository.findById(link.id())).thenReturn(Optional.of(link));
    when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Link updated = linkService.edit(new EditLink(link.id(), 5, 123456789L));

    assertEquals(5, updated.maxClicks());
    assertEquals(123456789L, updated.expiresAt());
    verify(linkRepository).save(updated);
  }

  @Test
  void editThrowsWhenLinkNotFound() {
    UUID id = UUID.randomUUID();
    when(linkRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> linkService.edit(new EditLink(id, 5, 123456789L)));
  }
}
