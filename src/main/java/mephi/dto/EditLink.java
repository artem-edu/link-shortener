package mephi.dto;

import java.util.UUID;

public record EditLink(UUID id, int maxClicks, long expiresAt) {}
