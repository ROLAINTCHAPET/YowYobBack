package com.yowyob.search.application.port.in;

import com.yowyob.search.document.ProductDocument;
import com.yowyob.search.dto.SearchResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Input port (use case) for all search operations.
 */
public interface SearchUseCase {
    Mono<SearchResponse> search(String query, String type, String city, String userId, String ipAddress);
    Mono<SearchResponse> searchByProximity(String query, String city, Double radiusKm);
    Mono<SearchResponse> searchByUserProximity(String query, Double userLatitude, Double userLongitude, String type);
    Mono<List<String>> autocomplete(String query);
    Mono<ProductDocument> getProductById(String id);
    Mono<ProductDocument> indexProduct(ProductDocument product);
}
