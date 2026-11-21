package mephi.model;

import java.util.UUID;

public record User(UUID id) {
    public static User create() {
        return new User(UUID.randomUUID());
    }
}
