package mephi.repository;

import mephi.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {
    private static final Map<UUID, User> data = new HashMap<>();

    public User save(User user) {
        data.put(user.id(), user);
        return user;
    }

    public Optional<User> findUserById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }
}
