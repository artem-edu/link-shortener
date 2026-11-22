package mephi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigServiceTest {

    @Test
    void readsStringValue() {
        assertNotNull(ConfigService.getStringValue("TTL_MS"));
    }

    @Test
    void readsIntValue() {
        assertTrue(ConfigService.getIntValue("CLEANUP_INTERVAL_MS") >= 0);
    }
}
