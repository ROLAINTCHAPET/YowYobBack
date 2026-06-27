package com.yowyob.crawler.infrastructure.adapter.out.wikimedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.crawler.application.port.out.ImageClientPort;
import com.yowyob.crawler.dto.WikimediaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class WikimediaImageAdapter implements ImageClientPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String WIKIMEDIA_URL = "https://commons.wikimedia.org/w/api.php";

    @Override
    public String findImageUrl(String name, double lat, double lng) {
        String photoUrl = searchByName(name);
        if (photoUrl != null) return photoUrl;
        return searchByGps(lat, lng);
    }

    private String searchByName(String name) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("list", "search")
                .queryParam("srsearch", name)
                .queryParam("srnamespace", "6")
                .queryParam("srlimit", "1")
                .queryParam("format", "json")
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(), String.class);
            WikimediaResponse parsed = objectMapper.readValue(response.getBody(), WikimediaResponse.class);

            if (parsed.getQuery() == null || parsed.getQuery().getSearch() == null || parsed.getQuery().getSearch().isEmpty()) {
                return null;
            }

            return fetchImageUrl(parsed.getQuery().getSearch().get(0).getTitle());
        } catch (Exception e) {
            return null;
        }
    }

    private String searchByGps(double lat, double lng) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("generator", "geosearch")
                .queryParam("ggscoord", lat + "|" + lng)
                .queryParam("ggsradius", "100")
                .queryParam("ggslimit", "1")
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("format", "json")
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(), String.class);
            WikimediaResponse parsed = objectMapper.readValue(response.getBody(), WikimediaResponse.class);

            if (parsed.getQuery() == null || parsed.getQuery().getPages() == null || parsed.getQuery().getPages().isEmpty()) {
                return null;
            }

            return parsed.getQuery().getPages().values().stream()
                .filter(p -> p.getImageinfo() != null && !p.getImageinfo().isEmpty())
                .map(p -> p.getImageinfo().get(0).getUrl())
                .filter(this::isValidImageUrl)
                .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchImageUrl(String title) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("titles", title)
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("format", "json")
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(), String.class);
            WikimediaResponse parsed = objectMapper.readValue(response.getBody(), WikimediaResponse.class);

            if (parsed.getQuery() == null || parsed.getQuery().getPages() == null) return null;

            return parsed.getQuery().getPages().values().stream()
                .filter(p -> p.getImageinfo() != null && !p.getImageinfo().isEmpty())
                .map(p -> p.getImageinfo().get(0).getUrl())
                .filter(this::isValidImageUrl)
                .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private HttpEntity<Void> createEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "YowYob-Crawler/1.0");
        return new HttpEntity<>(headers);
    }

    private boolean isValidImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
    }
}
