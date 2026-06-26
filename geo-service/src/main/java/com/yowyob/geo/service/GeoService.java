package com.yowyob.geo.service;

import com.yowyob.geo.dto.DistanceResponse;
import com.yowyob.geo.dto.GeocodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.yowyob.geo.dto.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;

@Service
@Slf4j
public class GeoService {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, GeocodeResponse> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.nominatim.url}")
    private String nominatimUrl;

    @Value("${app.nominatim.user-agent}")
    private String userAgent;

    // Base OSRM URL
    private static final String OSRM_BASE_URL = "http://router.project-osrm.org/route/v1";

    public GeoService(WebClient.Builder webClientBuilder,
            ReactiveRedisTemplate<String, GeocodeResponse> redisTemplate) {
        this.webClient = webClientBuilder.build();
        this.redisTemplate = redisTemplate;
    }

    public Mono<GeocodeResponse> geocode(String address) {
        String cacheKey = "geo:geocode:" + address.toLowerCase().trim().replaceAll("\\s+", "_");

        return redisTemplate.opsForValue().get(cacheKey)
                .switchIfEmpty(fetchFromNominatim(address)
                        .flatMap(response -> redisTemplate.opsForValue().set(cacheKey, response, Duration.ofDays(30))
                                .thenReturn(response)));
    }

    private Mono<GeocodeResponse> fetchFromNominatim(String address) {
        log.info("Fetching coordinates for address: {}", address);
        return webClient.get()
                .uri(nominatimUrl + "/search?format=json&q=" + address + "&limit=1")
                .header("User-Agent", userAgent)
                .retrieve()
                .bodyToMono(List.class)
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.empty();
                    }
                    Map<String, Object> result = (Map<String, Object>) list.get(0);
                    GeocodeResponse response = GeocodeResponse.builder()
                            .address((String) result.get("display_name"))
                            .latitude(Double.parseDouble((String) result.get("lat")))
                            .longitude(Double.parseDouble((String) result.get("lon")))
                            .build();
                    return Mono.just(response);
                })
                .onErrorResume(e -> {
                    log.error("Error calling Nominatim: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    public DistanceResponse calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double distanceKm = haversine(lat1, lon1, lat2, lon2);
        return new DistanceResponse(distanceKm, distanceKm * 0.621371);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public Mono<RouteResponse> getRoute(double startLat, double startLon, double endLat, double endLon, String mode) {
        // Validate and sanitize mode
        String transportMode = "driving";
        if (mode != null) {
            mode = mode.toLowerCase();
            if (mode.equals("walking") || mode.equals("bike") || mode.equals("cycling") || mode.equals("foot")) {
                if (mode.equals("bike"))
                    transportMode = "cycling";
                else if (mode.equals("foot"))
                    transportMode = "walking";
                else
                    transportMode = mode;
            }
        }

        String coordinates = String.format("%f,%f;%f,%f", startLon, startLat, endLon, endLat);
        String uri = String.format("%s/%s/%s?overview=full&geometries=geojson", OSRM_BASE_URL, transportMode,
                coordinates);

        log.info("Fetching route from OSRM: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        JsonNode root = objectMapper.readTree(json);
                        JsonNode route = root.path("routes").get(0);

                        double distance = route.path("distance").asDouble();
                        double duration = route.path("duration").asDouble();
                        JsonNode geometry = route.path("geometry").path("coordinates");

                        // Convert [lon, lat] arrays to [[lat, lon], ...] structure for frontend
                        List<List<Double>> points = new ArrayList<>();
                        if (geometry.isArray()) {
                            for (JsonNode point : geometry) {
                                List<Double> latLng = new ArrayList<>();
                                latLng.add(point.get(1).asDouble()); // lat
                                latLng.add(point.get(0).asDouble()); // lon
                                points.add(latLng);
                            }
                        }

                        return RouteResponse.builder()
                                .distance(distance)
                                .duration(duration)
                                .polyline(objectMapper.writeValueAsString(points))
                                .build();
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing OSRM response", e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error fetching route: {}", e.getMessage());
                    // Fallback to straight line
                    try {
                        List<List<Double>> simpleRoute = new ArrayList<>();
                        simpleRoute.add(List.of(startLat, startLon));
                        simpleRoute.add(List.of(endLat, endLon));

                        return Mono.just(RouteResponse.builder()
                                .distance(haversine(startLat, startLon, endLat, endLon) * 1000)
                                .duration(0)
                                .polyline(objectMapper.writeValueAsString(simpleRoute))
                                .build());
                    } catch (Exception ex) {
                        return Mono.empty();
                    }
                });
    }
}
