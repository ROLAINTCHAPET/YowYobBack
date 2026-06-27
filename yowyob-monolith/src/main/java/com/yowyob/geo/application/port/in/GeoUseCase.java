package com.yowyob.geo.application.port.in;

import com.yowyob.geo.dto.DistanceResponse;
import com.yowyob.geo.dto.GeoLocationDto;
import com.yowyob.geo.dto.GeocodeResponse;
import com.yowyob.geo.dto.RouteResponse;
import reactor.core.publisher.Mono;

/**
 * Input port (use case) for all geolocation operations.
 */
public interface GeoUseCase {
    Mono<GeocodeResponse> geocode(String address);
    DistanceResponse calculateDistance(double lat1, double lon1, double lat2, double lon2);
    Mono<GeoLocationDto> getLocationFromIp(String ip);
    Mono<RouteResponse> getRoute(double startLat, double startLon, double endLat, double endLon, String mode);
}
