package mephi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import mephi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {
  @TempDir Path tempDir;

  private String fileDb;

  @BeforeEach
  void setUp() {
    fileDb = tempDir.resolve("users.json").toString();
  }

  @Test
  void saveMakesUserFindableById() {
    UserRepository repository = new UserRepository(fileDb);
    User user = User.create();

    repository.save(user);

    assertEquals(Optional.of(user), repository.findUserById(user.id()));
  }

  @Test
  void loadsDataFromFileOnStartup() {
    User user = User.create();
    new UserRepository(fileDb).save(user);

    UserRepository reloaded = new UserRepository(fileDb);

    assertEquals(Optional.of(user), reloaded.findUserById(user.id()));
  }

  @Test
  void findUserByIdReturnsEmptyForUnknownId() {
    UserRepository repository = new UserRepository(fileDb);

    assertTrue(repository.findUserById(java.util.UUID.randomUUID()).isEmpty());
  }
}
