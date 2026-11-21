package mephi.repository;

import mephi.model.Link;

import java.util.*;
import java.util.stream.Collectors;

public class LinkRepository {
    private final Map<UUID, Link> data = new HashMap<>();
    private final Map<String, UUID> hashIndex = new HashMap<>();

    public Link save(Link link) {
        data.put(link.id(), link);
        hashIndex.put(link.hash(), link.id());

        return link;
    }

    public void delete(Link link) {
        data.remove(link.id());
    }

    public Optional<Link> findByHash(String hash) {
        UUID id = hashIndex.get(hash);

        return id == null ? Optional.empty() : Optional.ofNullable(data.get(id));
    }

    public Optional<Link> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<Link> findByUserId(UUID userId) {
        return data.values()
                .stream()
                .filter(link -> link.userId().equals(userId))
                .collect(Collectors.toList());
    }
}
