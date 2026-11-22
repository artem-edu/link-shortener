package mephi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import mephi.model.User;
import mephi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository);
  }

  @Test
  void createUserSavesAndBecomesCurrent() {
    User user = User.create();
    when(userRepository.save(any(User.class))).thenReturn(user);

    User result = userService.createUser();

    assertEquals(user, result);
    assertTrue(userService.getCurrentUser().isPresent());
    assertEquals(user, userService.getCurrentUser().get());
  }

  @Test
  void getCurrentUserIsEmptyBeforeLogin() {
    assertTrue(userService.getCurrentUser().isEmpty());
  }

  @Test
  void loginFindsUserAndSetsCurrent() {
    User user = User.create();
    when(userRepository.findUserById(user.id())).thenReturn(Optional.of(user));

    assertEquals(user, userService.login(user.id()));
    assertTrue(userService.getCurrentUser().isPresent());
  }

  @Test
  void loginThrowsWhenUserNotFound() {
    UUID id = UUID.randomUUID();
    when(userRepository.findUserById(id)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> userService.login(id));
  }

  @Test
  void logoutClearsCurrentUser() {
    User user = User.create();
    when(userRepository.save(any(User.class))).thenReturn(user);
    userService.createUser();

    userService.logout();

    assertTrue(userService.getCurrentUser().isEmpty());
  }
}
