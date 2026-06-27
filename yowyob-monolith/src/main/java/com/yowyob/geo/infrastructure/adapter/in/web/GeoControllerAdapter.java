package com.yowyob.geo.infrastructure.adapter.in.web;

import com.yowyob.geo.application.port.in.GeoUseCase;
import com.yowyob.geo.dto.DistanceRequest;
import com.yowyob.geo.dto.DistanceResponse;
import com.yowyob.geo.dto.GeoLocationDto;
import com.yowyob.geo.dto.GeocodeResponse;
import com.yowyob.geo.dto.RouteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller adapter for geo endpoints (hexagonal architecture).
 * Delegates all operations to the GeoUseCase input port.
 */
@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
@Tag(name = "Geo Service", description = "Endpoints for geolocation, geocoding, and routing")
public class GeoControllerAdapter {

    private final GeoUseCase geoUseCase;

    @GetMapping("/geocode")
    public Mono<ResponseEntity<GeocodeResponse>> geocode(@RequestParam String address) {
        return geoUseCase.geocode(address)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(@RequestBody DistanceRequest request) {
        DistanceResponse response = geoUseCase.calculateDistance(
                request.getLat1(), request.getLon1(),
                request.getLat2(), request.getLon2());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ip-location")
    public Mono<ResponseEntity<GeoLocationDto>> getIpLocation(
            @RequestParam(required = false) String ip,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "Remote-Addr", required = false) String remoteAddr) {

        String targetIp = ip;
        if (targetIp == null || targetIp.isEmpty()) {
            targetIp = xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : remoteAddr;
        }
        if (targetIp == null || targetIp.equals("0:0:0:0:0:0:0:1") || targetIp.equals("127.0.0.1")) {
            targetIp = "127.0.0.1";
        }

        String finalIp = targetIp;
        return geoUseCase.getLocationFromIp(finalIp)
                .map(ResponseEntity::ok)
                .doOnError(error -> System.err.println("Error getting IP location for " + finalIp + ": " + error.getMessage()));
    }

    @GetMapping("/route")
    public Mono<ResponseEntity<RouteResponse>> getRoute(
            @RequestParam double startLat, @RequestParam double startLon,
            @RequestParam double endLat, @RequestParam double endLon,
            @RequestParam(required = false, defaultValue = "driving") String mode) {
        return geoUseCase.getRoute(startLat, startLon, endLat, endLon, mode)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public String health() {
        return "Geo Service is running!";
    }
}
