package mephi.service;

import mephi.model.Link;
import mephi.model.User;
import mephi.repository.LinkRepository;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExpiredLinksCleaner {
    private static final long DEFAULT_CHECK_INTERVAL_MS = 60_000;

    private final LinkRepository linkRepository;
    private final UserService userService;
    private final Consumer<String> notifier;
    private final long checkIntervalMs;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "expired-links-cleaner");
        thread.setDaemon(true);
        return thread;
    });

    public ExpiredLinksCleaner(
            LinkRepository linkRepository,
            Consumer<String> notifier,
            UserService userService
    ) {
        this(linkRepository, userService, notifier, resolveCheckIntervalMs());
    }

    private ExpiredLinksCleaner(
            LinkRepository linkRepository,
            UserService userService,
            Consumer<String> notifier,
            long checkIntervalMs
    ) {
        this.linkRepository = linkRepository;
        this.userService = userService;
        this.notifier = notifier;
        this.checkIntervalMs = checkIntervalMs;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::clean, checkIntervalMs, checkIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void clean() {
        List<Link> expiredsLinks;
        try {
            expiredsLinks = linkRepository.getAllExpired();
        } catch (RuntimeException e) {
            return;
        }

        for (Link link : expiredsLinks) {
            linkRepository.delete(link);
            User user = userService.getCurrentUser().orElse(null);
            if (user == null) {
                continue;
            }

            if (user.id().equals(link.userId())) {
                notifier.accept("ссылка " + link.hash() + " (" + link.url() + ") удалена: время жизни истекло");
            }

        }
    }

    private static long resolveCheckIntervalMs() {
        try {
            return ConfigService.getIntValue("CLEANUP_INTERVAL_MS");
        } catch (RuntimeException e) {
            return DEFAULT_CHECK_INTERVAL_MS;
        }
    }
}
