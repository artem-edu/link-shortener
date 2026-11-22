package mephi.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class BrowserServiceTest {

  @Test
  void openDoesNotThrow() {
    BrowserService browserService = new BrowserService();

    assertDoesNotThrow(() -> browserService.open("not-a-real-url"));
  }
}
