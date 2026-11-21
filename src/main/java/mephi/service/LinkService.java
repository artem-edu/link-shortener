package mephi.service;

import mephi.dto.CreateLink;
import mephi.dto.EditLink;
import mephi.model.Link;
import mephi.repository.LinkRepository;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public class LinkService {
    private final LinkRepository linkRepository;
    private final BrowserService browserService;

    public LinkService(LinkRepository linkRepository, BrowserService browserService) {
        this.linkRepository = linkRepository;
        this.browserService = browserService;
    }

    public Link create(CreateLink dto) {
        String url = parseHttpUrl(dto.url())
                .orElseThrow(() -> new RuntimeException("Некорректный URL"))
                .toString();
        String hash = HashGenerator.generate(5);
        while (linkRepository.findByHash(hash).isPresent()) {
            hash = HashGenerator.generate(5);
        }

        return linkRepository.save(Link.create(dto.userId(), url, hash, dto.maxClicks()));
    }

    public void goTo(String hash) {
        Link link = linkRepository.findByHash(hash)
                .orElseThrow(() -> new RuntimeException("Ссылка не найдена"));

        if (link.isExpired()) {
            linkRepository.delete(link);
            throw new RuntimeException("Время жизни ссылки истекло");
        }

        if (link.isExhausted()) {
            throw new RuntimeException("Превышено количество переходов");
        }

        browserService.open(link.url());
        linkRepository.save(link.incrementClicks());
    }

    public Link edit(EditLink dto) {
        Link link = linkRepository.findById(dto.id())
                .orElseThrow(() -> new RuntimeException("Ссылка не найдена"));

        Link updatedLink = link.toBuilder()
                .expiresAt(dto.expiresAt())
                .maxClicks(dto.maxClicks())
                .build();

        return linkRepository.save(updatedLink);
    }

    private Optional<URL> parseHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && host != null && !host.isBlank()) {
                return Optional.of(uri.toURL());
            }
            return Optional.empty();
        } catch (IllegalArgumentException | MalformedURLException e) {
            return Optional.empty();
        }
    }
}
