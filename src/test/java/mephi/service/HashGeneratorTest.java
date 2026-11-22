package mephi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashGeneratorTest {
    private static final String SYMBOLS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_~";

    @Test
    void generatesStringOfRequestedLength() {
        assertEquals(5, HashGenerator.generate(5).length());
        assertEquals(20, HashGenerator.generate(20).length());
    }

    @Test
    void generatesOnlyAllowedSymbols() {
        String hash = HashGenerator.generate(100);
        for (char c : hash.toCharArray()) {
            assertTrue(SYMBOLS.indexOf(c) >= 0, "Недопустимый символ: " + c);
        }
    }
}
