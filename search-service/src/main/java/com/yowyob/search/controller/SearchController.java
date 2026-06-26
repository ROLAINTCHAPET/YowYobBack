/**
 * Search Controller for product search using Elasticsearch.
 * @author Matteo Owona, Rouchda Yampen
 * @date 2024-01-14
 */
package com.yowyob.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.yowyob.search.client.GeoServiceClient;
import com.yowyob.search.document.ProductDocument;
import com.yowyob.search.dto.SearchResponse;
import com.yowyob.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Endpoints for product search and indexing")
public class SearchController {

    private final SearchService searchService;
    private final GeoServiceClient geoServiceClient;

    @GetMapping
    @Operation(summary = "Search products", description = "Search products in Elasticsearch with query, type and city filters")
    public Mono<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "Remote-Addr", required = false) String remoteAddr) {

        // Determine IP address
        String ip = xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : remoteAddr;
        // For local dev fallback
        if (ip == null)
            ip = "127.0.0.1";

        return searchService.search(q, type, city, userId, ip);
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete suggestions", description = "Get suggestions based on partial query")
    public Mono<java.util.List<String>> autocomplete(@RequestParam String q) {
        return searchService.autocomplete(q);
    }

    @GetMapping("/proximity")
    @Operation(summary = "Search products by proximity", description = "Search products within a radius of a specified city using geocoded coordinates")
    public Mono<SearchResponse> searchByProximity(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "10") Double radius) {
        return searchService.searchByProximity(q, city, radius);
    }

    @GetMapping("/near-me")
    @Operation(summary = "Search products near user", description = "Search products by proximity to user's location. Supports queries like 'restaurants près de chez moi'. Can use latitude/longitude directly or provide IP address for automatic geolocation.")
    public Mono<SearchResponse> searchNearMe(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String type) {
        // If IP is provided but no coordinates, first geolocate the IP
        if (ip != null && !ip.isEmpty() && (latitude == null || longitude == null)) {
            return geoServiceClient.getLocationFromIp(ip)
                    .flatMap(location -> searchService.searchByUserProximity(q, location.getLatitude(),
                            location.getLongitude(), type))
                    .switchIfEmpty(searchService.searchByUserProximity(q, latitude, longitude, type));
        }
        return searchService.searchByUserProximity(q, latitude, longitude, type);
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get product details", description = "Get detailed information about a product by ID, including link to full listing")
    public Mono<ProductDocument> getProductDetails(@PathVariable String id) {
        return searchService.getProductById(id);
    }

    @PostMapping("/index")
    @Operation(summary = "Index product", description = "Add a new product to the Elasticsearch index. Automatically geocodes the city if coordinates are not provided.")
    public Mono<ProductDocument> indexProduct(@RequestBody ProductDocument product) {
        return searchService.indexProduct(product);
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Search Service with Elasticsearch and GeoService Integration!");
    }
}