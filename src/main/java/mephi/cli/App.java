package mephi.cli;

import mephi.dto.CreateLink;
import mephi.dto.EditLink;
import mephi.model.Link;
import mephi.model.User;
import mephi.repository.LinkRepository;
import mephi.repository.UserRepository;
import mephi.service.BrowserService;
import mephi.service.ConfigService;
import mephi.service.ExpiredLinksCleaner;
import mephi.service.LinkService;
import mephi.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class App {
    private final UserService userService;
    private final LinkService linkService;
    private final LinkRepository linkRepository;
    private final ExpiredLinksCleaner expiredLinksCleaner;
    private final ConsoleView view;
    private final ConsoleReader reader;

    private App(UserService userService,
                LinkService linkService,
                LinkRepository linkRepository,
                ExpiredLinksCleaner expiredLinksCleaner,
                ConsoleView view,
                ConsoleReader reader) {
        this.userService = userService;
        this.linkService = linkService;
        this.linkRepository = linkRepository;
        this.expiredLinksCleaner = expiredLinksCleaner;
        this.view = view;
        this.reader = reader;
    }

    public static void bootstrap() {
        UserRepository userRepository = new UserRepository();
        LinkRepository linkRepository = new LinkRepository();
        UserService userService = new UserService(userRepository);
        ConsoleView view = new ConsoleView();

        new App(
                userService,
                new LinkService(linkRepository, new BrowserService()),
                linkRepository,
                new ExpiredLinksCleaner(linkRepository, view::printNotification, userService),
                view,
                new ConsoleReader(view)
        ).run();
    }

    private void run() {
        view.printWelcome();
        expiredLinksCleaner.start();
        view.printMenu(userService.getCurrentUser());

        boolean running = true;
        while (running) {
            switch (reader.readMenuChoice()) {
                case "1" -> createShortLink();
                case "2" -> openShortLink();
                case "3" -> userLinks();
                case "4" -> editLink();
                case "5" -> deleteLink();
                case "6" -> login();
                case "7" -> logout();
                case "0" -> running = false;
                case "help" -> view.printMenu(userService.getCurrentUser());
                default -> view.printMessage("Неизвестная команда. Введите help для справки.");
            }
        }

        expiredLinksCleaner.stop();
    }

    private void createShortLink() {
        User user = userService.getCurrentUser().orElseGet(this::createUser);

        String url = reader.readUrl();
        int maxClicks = reader.readMaxClicks(ConfigService.getIntValue("DEFAULT_LINK_CLICK_LIMIT"));

        try {
            Link link = linkService.create(new CreateLink(url, maxClicks, user.id()));

            long ttlMs = ConfigService.getIntValue("TTL_MS");
            link = linkService.edit(new EditLink(link.id(), link.maxClicks(), System.currentTimeMillis() + ttlMs));

            view.printShortLinkCreated(link, ttlMs);
        } catch (RuntimeException e) {
            view.printMessage("Не удалось создать ссылку: " + e.getMessage());
        }
    }

    private void openShortLink() {
        String hash = reader.readHash();
        if (hash.isEmpty()) {
            view.printMessage("Ссылка не указана.");
            return;
        }

        try {
            linkService.goTo(hash);
        } catch (RuntimeException e) {
            view.printNotification(e.getMessage());
            return;
        }

        linkRepository.findByHash(hash).ifPresent(link -> {
            if (link.isExhausted()) {
                view.printNotification("лимит переходов исчерпан, ссылка больше недоступна.");
            }
        });
    }

    private void userLinks() {
        UUID userId = currentUserId();
        if (userId == null) {
            return;
        }

        List<Link> links = linkRepository.findByUserId(userId);
        if (links.isEmpty()) {
            view.printMessage("У вас пока нет ссылок.");
            return;
        }

        List<Link> activeLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.isExpired()) {
                linkRepository.delete(link);
                view.printExpiredRemoved(link);
            } else {
                activeLinks.add(link);
            }
        }
        view.printLinks(activeLinks);
    }

    private void editLink() {
        Link link = selectLink();
        if (link == null) {
            return;
        }
        if (link.isExpired()) {
            view.printNotification("время жизни ссылки истекло, редактирование недоступно.");
            return;
        }

        int maxClicks = reader.readMaxClicks(ConfigService.getIntValue("DEFAULT_LINK_CLICK_LIMIT"));
        linkService.edit(new EditLink(link.id(), maxClicks, link.expiresAt()));
        view.printMessage("Лимит переходов обновлен: " + maxClicks);
    }

    private void deleteLink() {
        Link link = selectLink();
        if (link == null) {
            return;
        }

        linkRepository.delete(link);
        view.printMessage("Ссылка " + view.shortLink(link) + " удалена.");
    }

    private void login() {
        UUID id = reader.readUuid().orElse(null);
        if (id == null) {
            view.printMessage("Некорректный UUID.");
            return;
        }

        try {
            User user = userService.login(id);
            view.printMessage("Вход выполнен. Пользователь: " + user.id());
        } catch (RuntimeException e) {
            view.printMessage("Ошибка входа: " + e.getMessage());
        }
    }

    private void logout() {
        userService.logout();
        view.printMessage("Вы вышли из аккаунта.");
    }

    private User createUser() {
        User user = userService.createUser();
        view.printCurrentUserCreated(user.id());
        return user;
    }

    private Link selectLink() {
        UUID userId = currentUserId();
        if (userId == null) {
            return null;
        }

        List<Link> links = linkRepository.findByUserId(userId);
        if (links.isEmpty()) {
            view.printMessage("У вас нет ссылок.");
            return null;
        }

        view.printNumberedLinks(links);
        return links.get(reader.readLinkIndex(links.size()));
    }

    private UUID currentUserId() {
        return userService.getCurrentUser()
                .map(User::id)
                .orElseGet(() -> {
                    view.printMessage("Для просмотра требуется авторизация.");
                    return null;
                });
    }
}
