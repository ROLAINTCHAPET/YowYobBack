package com.yowyob.crawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.crawler.dto.ListingEvent;
import com.yowyob.crawler.dto.OverpassResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OsmCrawlerService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String OVERPASS_URL =
        "https://overpass-api.de/api/interpreter";

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10_000; // 10 secondes entre retries
    private static final long REQUEST_DELAY_MS = 3_000; // 3 secondes entre requêtes

    public List<OverpassResponse.OsmElement> fetchByTypeAndLocation(
            String osmType, double lat, double lng, int radiusMeters) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Overpass query: type={} tentative={}/{}", osmType, attempt, MAX_RETRIES);

                List<OverpassResponse.OsmElement> results =
                    doFetch(osmType, lat, lng, radiusMeters);

                // Pause obligatoire APRÈS chaque requête réussie
                sleep(REQUEST_DELAY_MS);

                return results;

            } catch (HttpClientErrorException e) {

                // HTTP 429 = trop de requêtes → on attend plus longtemps
                if (e.getStatusCode().value() == 429) {
                    log.warn("Rate limit Overpass (429) — attente {}s avant retry {}/{}",
                        RETRY_DELAY_MS / 1000, attempt, MAX_RETRIES);
                    sleep(RETRY_DELAY_MS * attempt); // attente exponentielle
                } else {
                    log.error("Erreur HTTP Overpass {} : {}", e.getStatusCode(), e.getMessage());
                    return Collections.emptyList();
                }

            } catch (Exception e) {
                log.error("Erreur Overpass tentative {}/{} : {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleep(RETRY_DELAY_MS);
                }
            }
        }

        log.error("Overpass API inaccessible après {} tentatives pour type={}", MAX_RETRIES, osmType);
        return Collections.emptyList();
    }

    private List<OverpassResponse.OsmElement> doFetch(
            String osmType, double lat, double lng, int radiusMeters) throws Exception {

        String query = buildOverpassQuery(osmType, lat, lng, radiusMeters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Header poli — identifie ton application auprès d'Overpass
        headers.set("User-Agent", "YowYob-Crawler/1.0 (projet academique)");

        HttpEntity<String> request = new HttpEntity<>(
            "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8),
            headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            OVERPASS_URL, request, String.class
        );

        OverpassResponse parsed = objectMapper.readValue(
            response.getBody(), OverpassResponse.class
        );

        List<OverpassResponse.OsmElement> elements =
            parsed.getElements() != null ? parsed.getElements() : List.of();

        return elements.stream()
            .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
            .filter(e -> e.getTags() != null && e.getTags().containsKey("name"))
            .collect(Collectors.toList());
    }

    public ListingEvent toListingEvent(
            OverpassResponse.OsmElement element, String city) {

        Map<String, String> tags = element.getTags();

        String address = buildAddress(tags);
        String phone = tags.getOrDefault("phone",
                       tags.getOrDefault("contact:phone", null));
        String website = tags.getOrDefault("website",
                         tags.getOrDefault("contact:website", null));
        String category = tags.getOrDefault("amenity",
                          tags.getOrDefault("shop",
                          tags.getOrDefault("tourism", "unknown")));

        return ListingEvent.builder()
            .osmId("osm_" + element.getId())
            .name(tags.get("name"))
            .address(address)
            .latitude(element.getLatitude())
            .longitude(element.getLongitude())
            .phone(phone)
            .website(website)
            .openingHours(tags.getOrDefault("opening_hours", null))
            .category(category)
            .street(tags.get("addr:street"))
            .sourceCity(city)
            .crawledAt(Instant.now().toString())
            .source("openstreetmap")
            .build();
    }

    private String buildOverpassQuery(String type, double lat, double lng, int radius) {
        return String.format("""
            [out:json][timeout:30];
            (
              node["%s"](around:%d,%f,%f);
              way["%s"](around:%d,%f,%f);
              relation["%s"](around:%d,%f,%f);
            );
            out center tags;
            """,
            typeToOsmTag(type), radius, lat, lng,
            typeToOsmTag(type), radius, lat, lng,
            typeToOsmTag(type), radius, lat, lng
        );
    }

    private String typeToOsmTag(String type) {
        return switch (type) {
            case "shop", "supermarket", "market" -> "shop";
            default -> "amenity";
        };
    }

    private String buildAddress(Map<String, String> tags) {
        StringBuilder sb = new StringBuilder();
        if (tags.containsKey("addr:housenumber"))
            sb.append(tags.get("addr:housenumber")).append(" ");
        if (tags.containsKey("addr:street"))
            sb.append(tags.get("addr:street")).append(", ");
        if (tags.containsKey("addr:city"))
            sb.append(tags.get("addr:city"));
        String result = sb.toString().trim().replaceAll(", $", "");
        return result.isEmpty() ? null : result;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
