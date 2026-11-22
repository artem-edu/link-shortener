package mephi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BrowserServiceTest {

    @Test
    void openDoesNotThrow() {
        BrowserService browserService = new BrowserService();

        assertDoesNotThrow(() -> browserService.open("not-a-real-url"));
    }
}
