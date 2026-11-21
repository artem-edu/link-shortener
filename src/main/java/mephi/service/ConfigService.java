package mephi.service;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigService {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getStringValue(String key) {
        return dotenv.get(key);
    }

    public static int getIntValue(String key) {
        return Integer.parseInt(dotenv.get(key));
    }
}
