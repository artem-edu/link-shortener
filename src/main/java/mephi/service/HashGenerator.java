package mephi.service;

import java.security.SecureRandom;

public class HashGenerator {
  private static final String SYMBOLS =
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_~";
  private static final SecureRandom random = new SecureRandom();

  public static String generate(int len) {
    StringBuilder stringBuilder = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      stringBuilder.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
    }

    return stringBuilder.toString();
  }
}
