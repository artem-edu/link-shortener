package mephi.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mephi.model.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LinkRepositoryTest {
  @TempDir Path tempDir;

  private String fileDb;

  @BeforeEach
  void setUp() {
    fileDb = tempDir.resolve("links.json").toString();
  }

  private Link link(UUID userId, String hash, long expiresAt) {
    return Link.create(userId, "https://example.com/" + hash, hash, 10).toBuilder()
        .expiresAt(expiresAt)
        .build();
  }

  @Test
  void saveMakesLinkFindableByIdAndHash() {
    LinkRepository repository = new LinkRepository(fileDb);
    Link link = link(UUID.randomUUID(), "hash1", System.currentTimeMillis() + 100_000);

    repository.save(link);

    assertEquals(Optional.of(link), repository.findById(link.id()));
    assertEquals(Optional.of(link), repository.findByHash("hash1"));
  }

  @Test
  void loadsDataFromFileOnStartup() {
    Link link = link(UUID.randomUUID(), "hash1", System.currentTimeMillis() + 100_000);
    new LinkRepository(fileDb).save(link);

    LinkRepository reloaded = new LinkRepository(fileDb);

    assertEquals(Optional.of(link), reloaded.findById(link.id()));
    assertEquals(Optional.of(link), reloaded.findByHash("hash1"));
  }

  @Test
  void deleteRemovesLink() {
    LinkRepository repository = new LinkRepository(fileDb);
    Link link = link(UUID.randomUUID(), "hash1", System.currentTimeMillis() + 100_000);
    repository.save(link);

    repository.delete(link);

    assertTrue(repository.findById(link.id()).isEmpty());
    assertTrue(repository.findByHash("hash1").isEmpty());
  }

  @Test
  void findByUserIdReturnsOnlyUserLinks() {
    LinkRepository repository = new LinkRepository(fileDb);
    UUID userId = UUID.randomUUID();
    Link userLink = link(userId, "hash1", System.currentTimeMillis() + 100_000);
    Link otherLink = link(UUID.randomUUID(), "hash2", System.currentTimeMillis() + 100_000);
    repository.save(userLink);
    repository.save(otherLink);

    List<Link> result = repository.findByUserId(userId);

    assertEquals(List.of(userLink), result);
  }

  @Test
  void getAllExpiredReturnsOnlyExpiredLinks() {
    LinkRepository repository = new LinkRepository(fileDb);
    UUID userId = UUID.randomUUID();
    Link expired = link(userId, "hash1", System.currentTimeMillis() - 1000);
    Link active = link(userId, "hash2", System.currentTimeMillis() + 100_000);
    repository.save(expired);
    repository.save(active);

    assertEquals(List.of(expired), repository.getAllExpired());
  }
}
