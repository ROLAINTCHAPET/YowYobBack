package com.yowyob.search.infrastructure.adapter.in.web;

import com.yowyob.search.application.port.in.SearchUseCase;
import com.yowyob.search.application.port.out.SearchHistoryPort;
import com.yowyob.search.document.ProductDocument;
import com.yowyob.search.document.SearchHistory;
import com.yowyob.search.dto.SearchResponse;
import com.yowyob.geo.service.IpGeolocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller adapter for search endpoints (hexagonal architecture).
 * Delegates all operations to the SearchUseCase input port.
 *
 * @author YowYob Team
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search", description = "Endpoints for product search and indexing")
public class SearchControllerAdapter {

    private final SearchUseCase searchUseCase;
    private final SearchHistoryPort searchHistoryPort;
    private final IpGeolocationService ipGeolocationService;

    @GetMapping
    @Operation(summary = "Search products", description = "Search products in Elasticsearch with query, type and city filters")
    public Mono<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "Remote-Addr", required = false) String remoteAddr) {

        String ip = xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : remoteAddr;
        if (ip == null) ip = "127.0.0.1";

        return searchUseCase.search(q, type, city, userId, ip);
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete suggestions", description = "Get suggestions based on partial query")
    public Mono<List<String>> autocomplete(@RequestParam String q) {
        return searchUseCase.autocomplete(q);
    }

    @GetMapping("/proximity")
    @Operation(summary = "Search products by proximity", description = "Search products within a radius of a specified city")
    public Mono<SearchResponse> searchByProximity(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "10") Double radius) {
        return searchUseCase.searchByProximity(q, city, radius);
    }

    @GetMapping("/near-me")
    @Operation(summary = "Search products near user", description = "Search products by proximity to user's location")
    public Mono<SearchResponse> searchNearMe(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String type) {
        if (ip != null && !ip.isEmpty() && (latitude == null || longitude == null)) {
            return ipGeolocationService.getLocationFromIp(ip)
                    .flatMap(location -> searchUseCase.searchByUserProximity(q, location.getLatitude(), location.getLongitude(), type))
                    .switchIfEmpty(searchUseCase.searchByUserProximity(q, latitude, longitude, type));
        }
        return searchUseCase.searchByUserProximity(q, latitude, longitude, type);
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get product details", description = "Get detailed information about a product by ID")
    public Mono<ProductDocument> getProductDetails(@PathVariable String id) {
        return searchUseCase.getProductById(id);
    }

    @PostMapping("/index")
    @Operation(summary = "Index product", description = "Add a new product to the Elasticsearch index")
    public Mono<ProductDocument> indexProduct(@RequestBody ProductDocument product) {
        return searchUseCase.indexProduct(product);
    }

    @GetMapping("/history")
    @Operation(summary = "Get search history", description = "Get the search history for a user")
    public Flux<SearchHistory> getHistory(@RequestHeader(value = "X-User-Id") String userId) {
        log.info("Fetching history for User ID: {}", userId);
        return searchHistoryPort.getUserHistory(userId);
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Search Service is running!");
    }
}
