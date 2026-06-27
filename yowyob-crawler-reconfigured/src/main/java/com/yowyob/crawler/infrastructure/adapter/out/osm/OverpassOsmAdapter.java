package com.yowyob.crawler.infrastructure.adapter.out.osm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.crawler.application.port.out.OsmClientPort;
import com.yowyob.crawler.domain.model.Listing;
import com.yowyob.crawler.dto.OverpassResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class OverpassOsmAdapter implements OsmClientPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10_000;

    @Override
    public List<Listing> fetchListings(String type, double lat, double lng, int radius, String city) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                List<OverpassResponse.OsmElement> elements = doFetch(type, lat, lng, radius);
                return elements.stream()
                    .map(e -> toDomain(e, city))
                    .collect(Collectors.toList());
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    sleep(RETRY_DELAY_MS * attempt);
                } else {
                    return Collections.emptyList();
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) sleep(RETRY_DELAY_MS);
            }
        }
        return Collections.emptyList();
    }

    private List<OverpassResponse.OsmElement> doFetch(String osmType, double lat, double lng, int radiusMeters) throws Exception {
        String query = buildOverpassQuery(osmType, lat, lng, radiusMeters);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "YowYob-Crawler/1.0 (projet academique)");

        HttpEntity<String> request = new HttpEntity<>(
            "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8),
            headers
        );

        ResponseEntity<String> response = restTemplate.postForEntity(OVERPASS_URL, request, String.class);
        OverpassResponse parsed = objectMapper.readValue(response.getBody(), OverpassResponse.class);

        return parsed.getElements() != null ? parsed.getElements() : List.of();
    }

    private Listing toDomain(OverpassResponse.OsmElement element, String city) {
        Map<String, String> tags = element.getTags();
        String address = buildAddress(tags);
        String phone = tags.getOrDefault("phone", tags.getOrDefault("contact:phone", null));
        String website = tags.getOrDefault("website", tags.getOrDefault("contact:website", null));
        String category = tags.getOrDefault("amenity", tags.getOrDefault("shop", tags.getOrDefault("tourism", "unknown")));

        return Listing.builder()
            .id("osm_" + element.getId())
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
        String osmTag = switch (type) {
            case "shop", "supermarket", "market" -> "shop";
            default -> "amenity";
        };
        return String.format("""
            [out:json][timeout:30];
            (
              node["%s"](around:%d,%f,%f);
              way["%s"](around:%d,%f,%f);
              relation["%s"](around:%d,%f,%f);
            );
            out center tags;
            """, osmTag, radius, lat, lng, osmTag, radius, lat, lng, osmTag, radius, lat, lng);
    }

    private String buildAddress(Map<String, String> tags) {
        StringBuilder sb = new StringBuilder();
        if (tags.containsKey("addr:housenumber")) sb.append(tags.get("addr:housenumber")).append(" ");
        if (tags.containsKey("addr:street")) sb.append(tags.get("addr:street")).append(", ");
        if (tags.containsKey("addr:city")) sb.append(tags.get("addr:city"));
        return sb.toString().trim().replaceAll(", $", "");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
