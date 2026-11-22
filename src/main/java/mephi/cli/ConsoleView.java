package mephi.cli;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mephi.model.Link;
import mephi.model.User;

public class ConsoleView {
  private static final String SHORT_LINK_PREFIX = "localhost/";
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

  public void printWelcome() {
    System.out.println("=== Сервис коротких ссылок ===");
  }

  public void printMessage(String message) {
    System.out.println(message);
  }

  public void printNotification(String message) {
    System.out.println("Уведомление: " + message);
  }

  public void printMenu(Optional<User> currentUser) {
    System.out.println("----------------------------------------");
    currentUser.ifPresentOrElse(
        user -> System.out.println("Текущий пользователь: " + user.id()),
        () -> System.out.println("Текущий пользователь: не авторизован"));
    System.out.println("1 - Сократить ссылку");
    System.out.println("2 - Перейти по короткой ссылке");
    System.out.println("3 - Мои ссылки");
    System.out.println("4 - Изменить лимит переходов");
    System.out.println("5 - Удалить ссылку");
    System.out.println("6 - Войти по UUID");
    System.out.println("7 - Выйти из аккаунта");
    System.out.println("0 - Выход");
    System.out.println("help - вывод спправки");
  }

  public void printCurrentUserCreated(UUID id) {
    System.out.println("Новый UUID пользователя: " + id);
    System.out.println("Сохраните его - по этому UUID вы сможете управлять своими ссылками.");
  }

  public void printShortLinkCreated(Link link, long ttlMs) {
    System.out.println("Готово! Короткая ссылка: " + shortLink(link));
    System.out.println("Лимит переходов: " + link.maxClicks());
    System.out.println(
        "Срок жизни: "
            + formatTtl(ttlMs)
            + " (до "
            + TIME_FORMAT.format(Instant.ofEpochMilli(link.expiresAt()))
            + ")");
  }

  public void printLinks(List<Link> links) {
    for (int i = 0; i < links.size(); i++) {
      Link link = links.get(i);
      if (link.isExhausted()) {
        System.out.printf(
            "%d) %s -> %s [лимит переходов исчерпан]%n", i + 1, shortLink(link), link.url());
      } else {
        System.out.printf(
            "%d) %s -> %s [переходы: %d/%d, истекает %s]%n",
            i + 1,
            shortLink(link),
            link.url(),
            link.clickCount(),
            link.maxClicks(),
            TIME_FORMAT.format(Instant.ofEpochMilli(link.expiresAt())));
      }
    }
  }

  public void printNumberedLinks(List<Link> links) {
    for (int i = 0; i < links.size(); i++) {
      Link link = links.get(i);
      System.out.printf(
          "%d) %s -> %s (переходы: %d/%d)%n",
          i + 1, shortLink(link), link.url(), link.clickCount(), link.maxClicks());
    }
  }

  public void printExpiredRemoved(Link link) {
    System.out.printf(
        "%s -> %s - время жизни истекло, ссылка удалена.%n", shortLink(link), link.url());
  }

  public String shortLink(Link link) {
    return SHORT_LINK_PREFIX + link.hash();
  }

  private String formatTtl(long ttlMs) {
    long seconds = ttlMs / 1000;
    long days = seconds / 86400;
    long hours = (seconds % 86400) / 3600;
    long minutes = (seconds % 3600) / 60;
    long secs = seconds % 60;
    if (days > 0) {
      return days + " дн " + hours + " ч";
    }
    if (hours > 0) {
      return hours + " ч " + minutes + " мин";
    }
    if (minutes > 0) {
      return minutes + " мин " + secs + " сек";
    }
    return secs + " сек";
  }
}
