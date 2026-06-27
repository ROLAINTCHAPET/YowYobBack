package com.yowyob.geo.application.service;

import com.yowyob.geo.application.port.in.GeoUseCase;
import com.yowyob.geo.dto.DistanceResponse;
import com.yowyob.geo.dto.GeoLocationDto;
import com.yowyob.geo.dto.GeocodeResponse;
import com.yowyob.geo.dto.RouteResponse;
import com.yowyob.geo.service.GeoService;
import com.yowyob.geo.service.IpGeolocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Application service implementing all geo use cases.
 * Delegates to the existing GeoService and IpGeolocationService infrastructure
 * components (retained as infrastructure adapters due to external API dependencies).
 */
@Service
@RequiredArgsConstructor
public class GeoApplicationService implements GeoUseCase {

    private final GeoService geoService;
    private final IpGeolocationService ipGeolocationService;

    @Override
    public Mono<GeocodeResponse> geocode(String address) {
        return geoService.geocode(address);
    }

    @Override
    public DistanceResponse calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return geoService.calculateDistance(lat1, lon1, lat2, lon2);
    }

    @Override
    public Mono<GeoLocationDto> getLocationFromIp(String ip) {
        return ipGeolocationService.getLocationFromIp(ip);
    }

    @Override
    public Mono<RouteResponse> getRoute(double startLat, double startLon, double endLat, double endLon, String mode) {
        return geoService.getRoute(startLat, startLon, endLat, endLon, mode);
    }
}
