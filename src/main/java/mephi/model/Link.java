package mephi.model;

import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Link(
    UUID id,
    UUID userId,
    String url,
    String hash,
    int maxClicks,
    int clickCount,
    long createdAt,
    long expiresAt) {
  public static Link create(UUID userId, String url, String hash, int maxClicks) {
    return new Link(
        UUID.randomUUID(),
        userId,
        url,
        hash,
        maxClicks,
        0,
        System.currentTimeMillis(),
        System.currentTimeMillis());
  }

  public Link incrementClicks() {
    return toBuilder().clickCount(clickCount + 1).build();
  }

  public boolean isExhausted() {
    return clickCount >= maxClicks;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() > expiresAt;
  }
}
