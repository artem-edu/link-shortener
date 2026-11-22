package mephi.dto;

import java.util.UUID;

public record CreateLink(String url, int maxClicks, UUID userId) {}
