package mephi.repository;

import com.google.gson.reflect.TypeToken;
import mephi.core.FileRepository;
import mephi.model.User;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class UserRepository extends FileRepository<UUID, User> {
    public UserRepository() {
        this("db/users.json");
    }

    public UserRepository(String fileDb) {
        super(fileDb, new TypeToken<HashMap<UUID, User>>() {
        });
        loadFromFile();
    }

    public User save(User user) {
        data.put(user.id(), user);

        saveToFile();
        return user;
    }

    public Optional<User> findUserById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }
}
