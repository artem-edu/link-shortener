package mephi.repository;

import com.google.gson.reflect.TypeToken;
import mephi.core.FileRepository;
import mephi.model.Link;

import java.util.*;
import java.util.stream.Collectors;

public class LinkRepository extends FileRepository<UUID, Link> {
    private final Map<String, UUID> hashIndex = new HashMap<>();

    public LinkRepository() {
        this("db/links.json");
    }

    public LinkRepository(String fileDb) {
        super(fileDb, new TypeToken<HashMap<UUID, Link>>() {
        });
        loadFromFile();
    }

    public Link save(Link link) {
        data.put(link.id(), link);
        hashIndex.put(link.hash(), link.id());
        saveToFile();

        return link;
    }

    public void delete(Link link) {
        data.remove(link.id());
        saveToFile();
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

    public List<Link> findAll() {
        return List.copyOf(data.values());
    }

    @Override
    protected void afterLoad(Map<UUID, Link> loadedData) {
        for (Link link : loadedData.values()) {
            this.hashIndex.put(link.hash(), link.id());
        }
    }
}
