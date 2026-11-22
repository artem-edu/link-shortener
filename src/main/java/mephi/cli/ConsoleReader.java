package mephi.cli;

import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleReader {
  private final Scanner scanner = new Scanner(System.in);
  private final ConsoleView view;

  public ConsoleReader(ConsoleView view) {
    this.view = view;
  }

  public String readMenuChoice() {
    return readLine(">> ");
  }

  public String readUrl() {
    return readLine("Введите URL: ");
  }

  public String readHash() {
    String input = readLine("Введите короткую ссылку: ");
    int slashIndex = input.lastIndexOf('/');
    return (slashIndex >= 0 ? input.substring(slashIndex + 1) : input).trim();
  }

  public int readMaxClicks(int defaultLimit) {
    while (true) {
      String input =
          readLine("Максимальное количество переходов [по умолчанию " + defaultLimit + "]: ");
      if (input.isEmpty()) {
        return defaultLimit;
      }
      try {
        int value = Integer.parseInt(input);
        if (value > 0) {
          return value;
        }
        view.printMessage("Лимит должен быть положительным числом.");
      } catch (NumberFormatException e) {
        view.printMessage("Введите число или оставьте строку пустой для значения по умолчанию.");
      }
    }
  }

  public int readLinkIndex(int size) {
    while (true) {
      String input = readLine("Введите номер ссылки: ");
      try {
        int index = Integer.parseInt(input) - 1;
        if (index >= 0 && index < size) {
          return index;
        }
      } catch (NumberFormatException ignored) {
      }
      view.printMessage("Нет ссылки с таким номером. Попробуйте снова.");
    }
  }

  public Optional<UUID> readUuid() {
    String input = readLine("Введите UUID пользователя: ");
    try {
      return Optional.of(UUID.fromString(input));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private String readLine(String prompt) {
    System.out.println();
    System.out.print(prompt);
    return scanner.nextLine().trim();
  }
}
