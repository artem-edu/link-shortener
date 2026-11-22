package mephi.service;

import java.util.Optional;
import java.util.UUID;
import mephi.model.User;
import mephi.repository.UserRepository;

public class UserService {
  private final UserRepository userRepository;

  private User currentUser = null;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User createUser() {
    currentUser = userRepository.save(User.create());
    return currentUser;
  }

  public Optional<User> getCurrentUser() {
    return Optional.ofNullable(currentUser);
  }

  public User login(UUID id) {
    currentUser =
        userRepository
            .findUserById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    return currentUser;
  }

  public void logout() {
    currentUser = null;
  }
}
