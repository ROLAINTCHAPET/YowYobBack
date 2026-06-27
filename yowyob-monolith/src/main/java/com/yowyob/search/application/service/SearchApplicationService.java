package com.yowyob.search.application.service;

import com.yowyob.geo.service.GeoService;
import com.yowyob.geo.service.IpGeolocationService;
import com.yowyob.search.application.port.in.SearchUseCase;
import com.yowyob.search.application.port.out.SearchHistoryPort;
import com.yowyob.search.document.ProductDocument;
import com.yowyob.search.dto.SearchResponse;
import com.yowyob.search.service.EmbeddingClient;
import com.yowyob.search.service.KeywordParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;

import java.util.List;

/**
 * Application service implementing all search use cases.
 * Orchestrates search logic using the reactive Elasticsearch stack,
 * geo-services, embedding generation, and search history persistence.
 *
 * @author YowYob Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchApplicationService implements SearchUseCase {

    private final ReactiveElasticsearchOperations elasticsearchOperations;
    private final com.yowyob.search.repository.ProductSearchRepository searchRepository;
    private final GeoService geoService;
    private final IpGeolocationService ipGeolocationService;
    private final KeywordParser keywordParser;
    private final SearchHistoryPort searchHistoryPort;
    private final EmbeddingClient embeddingClient;

    // -------------------------------------------------------------------------
    // Normalisation helpers
    // -------------------------------------------------------------------------

    private String normalizeAccents(String input) {
        if (input == null) return "";
        return input.replace("é", "e").replace("È", "E").replace("É", "E").replace("è", "e").replace("ê", "e")
                .replace("ë", "e").replace("à", "a").replace("â", "a").replace("ù", "u").replace("û", "u")
                .replace("ô", "o").replace("ö", "o").replace("ç", "c").replace("Ç", "C").replace("î", "i")
                .replace("ï", "i");
    }

    // -------------------------------------------------------------------------
    // Use case implementations
    // -------------------------------------------------------------------------

    @Override
    public Mono<SearchResponse> search(String query, String type, String city, String userId, String ipAddress) {
        log.info("Searching: query={}, type={}, city={}, userId={}, ip={}", query, type, city, userId, ipAddress);

        KeywordParser.ParsedQueryResult parsed = keywordParser.parseWithCity(query);

        if (parsed.is_proximity_search && ipAddress != null) {
            log.info("Proximity search detected via IP: {}", ipAddress);
            return ipGeolocationService.getLocationFromIp(ipAddress).flatMap(location -> {
                if (location.getLatitude() != null && location.getLongitude() != null) {
                    return searchByUserProximity(query, location.getLatitude(), location.getLongitude(), type);
                }
                return Mono.empty();
            }).switchIfEmpty(Mono.defer(() -> executeStandardSearch(parsed, query, type, city, userId)));
        }

        return executeStandardSearch(parsed, query, type, city, userId);
    }

    @Override
    public Mono<SearchResponse> searchByProximity(String query, String city, Double radiusKm) {
        log.info("Searching by proximity: query={}, city={}, radiusKm={}", query, city, radiusKm);

        return geoService.geocode(city).flatMap(geoLocation -> {
            final Double radius = radiusKm != null ? radiusKm : 10.0;

            Query searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        if (query != null && !query.isEmpty()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title^3", "description", "category")
                                    .fuzziness("AUTO")));
                        }
                        b.filter(f -> f.geoDistance(gd -> gd
                                .field("location")
                                .distance(radius + "km")
                                .location(gl -> gl.latlon(latlon -> latlon
                                        .lat(geoLocation.getLatitude())
                                        .lon(geoLocation.getLongitude())))));
                        return b;
                    }))
                    .withSort(s -> s.geoDistance(gd -> gd
                            .field("location")
                            .location(gl -> gl.latlon(latlon -> latlon
                                    .lat(geoLocation.getLatitude())
                                    .lon(geoLocation.getLongitude())))
                            .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)))
                    .build();

            return elasticsearchOperations.search(searchQuery, ProductDocument.class)
                    .map(SearchHit::getContent)
                    .map(doc -> toProductDto(doc, geoLocation.getLatitude(), geoLocation.getLongitude()))
                    .collectList()
                    .map(results -> SearchResponse.builder()
                            .success(true)
                            .query(query)
                            .total(results.size())
                            .results(results)
                            .build());
        }).switchIfEmpty(Mono.just(SearchResponse.builder().success(false).query(query).total(0).build()));
    }

    @Override
    public Mono<SearchResponse> searchByUserProximity(String query, Double userLatitude, Double userLongitude, String type) {
        log.info("Searching by user proximity: query={}, lat={}, lon={}, type={}", query, userLatitude, userLongitude, type);

        if (userLatitude == null || userLongitude == null) {
            return search(query, type, null, null, null);
        }

        KeywordParser.ParsedQueryResult parsed = keywordParser.parseWithCity(query);
        String parsedQuery = parsed.query;
        Double proximityRadius = parsed.proximity_radius != null ? parsed.proximity_radius : 10.0;

        try {
            Query searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        if (parsedQuery != null && !parsedQuery.isEmpty()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(parsedQuery)
                                    .fields("title^3", "description", "category", "city^2")
                                    .fuzziness("AUTO")
                                    .prefixLength(2)
                                    .operator(Operator.Or)));
                        } else {
                            b.must(m -> m.matchAll(ma -> ma));
                        }
                        if (type != null && !"all".equalsIgnoreCase(type)) {
                            final String filterType = type.toLowerCase();
                            if (filterType.equals("shop")) {
                                b.filter(f -> f.term(t -> t.field("serviceType").value("user")));
                            } else if (filterType.equals("product") || filterType.equals("service")
                                    || filterType.equals("products") || filterType.equals("services")) {
                                b.filter(f -> f.term(t -> t.field("serviceType").value("listing")));
                            } else {
                                b.filter(f -> f.term(t -> t.field("serviceType").value(filterType)));
                            }
                        }
                        return b;
                    }))
                    .build();

            final Double proximityRadiusFinal = proximityRadius;
            return elasticsearchOperations.search(searchQuery, ProductDocument.class)
                    .map(SearchHit::getContent)
                    .filter(doc -> {
                        if (doc.getLatitude() != null && doc.getLongitude() != null) {
                            double distance = calculateDistance(userLatitude, userLongitude, doc.getLatitude(), doc.getLongitude());
                            return distance <= proximityRadiusFinal;
                        }
                        return false;
                    })
                    .collectList()
                    .map(results -> {
                        results.sort((a, b) -> {
                            double distA = calculateDistance(userLatitude, userLongitude, a.getLatitude(), a.getLongitude());
                            double distB = calculateDistance(userLatitude, userLongitude, b.getLatitude(), b.getLongitude());
                            return Double.compare(distA, distB);
                        });
                        List<SearchResponse.ProductDto> dtos = results.stream()
                                .map(doc -> toProductDto(doc, userLatitude, userLongitude))
                                .toList();
                        return SearchResponse.builder().success(true).query(query).total(dtos.size()).results(dtos).build();
                    })
                    .defaultIfEmpty(SearchResponse.builder().success(true).query(query).total(0).results(List.of()).build())
                    .onErrorResume(e -> {
                        log.error("Error executing proximity search", e);
                        return Mono.just(SearchResponse.builder().success(false).query(query).total(0).results(null).build());
                    });
        } catch (Exception e) {
            log.error("Error building proximity search query", e);
            return Mono.just(SearchResponse.builder().success(false).query(query).total(0).results(null).build());
        }
    }

    @Override
    public Mono<List<String>> autocomplete(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Mono.just(List.of());
        }
        String cleanQuery = query.trim();
        return searchRepository.findByTitleContainingOrDescriptionContaining(cleanQuery, cleanQuery)
                .map(ProductDocument::getTitle).distinct().take(5)
                .collectList();
    }

    @Override
    public Mono<ProductDocument> getProductById(String id) {
        log.info("Getting product details for ID: {}", id);
        return searchRepository.findById(id)
                .doOnNext(doc -> log.info("Found product: {}", doc.getTitle()))
                .doOnError(error -> log.error("Error retrieving product: {}", error.getMessage()));
    }

    @Override
    public Mono<ProductDocument> indexProduct(ProductDocument product) {
        String textToEmbed = String.format("%s %s %s",
                product.getTitle() != null ? product.getTitle() : "",
                product.getCategory() != null ? product.getCategory() : "",
                product.getDescription() != null ? product.getDescription() : "").trim();

        Mono<ProductDocument> locationMono = Mono.just(product);
        if (product.getCity() != null && (product.getLatitude() == null || product.getLongitude() == null)) {
            locationMono = geoService.geocode(product.getCity()).map(geoLocation -> {
                product.setLatitude(geoLocation.getLatitude());
                product.setLongitude(geoLocation.getLongitude());
                return product;
            }).switchIfEmpty(Mono.just(product));
        }

        return locationMono.flatMap(p -> {
            if (!textToEmbed.isEmpty()) {
                return embeddingClient.generateEmbedding(textToEmbed)
                        .flatMap(vectorList -> {
                            if (vectorList != null && !vectorList.isEmpty()) {
                                float[] vectorArray = new float[vectorList.size()];
                                for (int i = 0; i < vectorList.size(); i++) {
                                    vectorArray[i] = vectorList.get(i);
                                }
                                p.setTextVector(vectorArray);
                            }
                            return searchRepository.save(p);
                        })
                        .switchIfEmpty(searchRepository.save(p));
            }
            return searchRepository.save(p);
        }).doOnNext(saved -> log.info("Product indexed with vector & geo: {}", saved.getTitle()));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Mono<SearchResponse> executeStandardSearch(KeywordParser.ParsedQueryResult parsed, String originalQuery,
            String type, String city, String userId) {

        String tempCity = city;
        if (parsed.query != null && !parsed.query.isEmpty()) {
            final String parsedQuery = parsed.query;
            final String extractedCity = parsed.extracted_city;
            String inferredCategory = parsed.inferred_category;

            if (tempCity == null && extractedCity != null) {
                tempCity = extractedCity;
            }
            final String effectiveCity = tempCity;

            return embeddingClient.generateEmbedding(originalQuery)
                    .flatMap(vectorList -> {
                        float[] vectorArray = null;
                        if (vectorList != null && !vectorList.isEmpty()) {
                            vectorArray = new float[vectorList.size()];
                            for (int i = 0; i < vectorList.size(); i++) {
                                vectorArray[i] = vectorList.get(i);
                            }
                        }
                        final float[] finalVectorArray = vectorArray;

                        Query searchQuery = NativeQuery.builder()
                                .withQuery(q -> q.bool(b -> {
                                    if (parsedQuery != null && !parsedQuery.isEmpty()) {
                                        b.should(s -> s.multiMatch(mm -> mm
                                                .query(parsedQuery)
                                                .fields("title^2", "description^1", "category^1")
                                                .fuzziness("AUTO")
                                                .prefixLength(1)
                                                .operator(Operator.Or)
                                                .boost(1.0f)));
                                        b.should(s -> s.matchPhrase(mp -> mp.field("description").query(parsedQuery).boost(3.0f)));
                                        b.should(s -> s.matchPhrase(mp -> mp.field("title").query(parsedQuery).boost(5.0f)));
                                    }
                                    if (inferredCategory != null) {
                                        b.should(s -> s.match(m -> m.field("category").query(inferredCategory).boost(3.0f)));
                                        b.should(s -> s.match(m -> m.field("serviceType").query(inferredCategory).boost(3.0f)));
                                    }
                                    b.minimumShouldMatch("0");
                                    return b;
                                }))
                                .withKnnSearches(co.elastic.clients.elasticsearch._types.KnnSearch.of(knn -> {
                                    if (finalVectorArray != null) {
                                        List<Float> vectorListForKnn = new java.util.ArrayList<>();
                                        for (float f : finalVectorArray) {
                                            vectorListForKnn.add(f);
                                        }
                                        return knn.field("text_vector").queryVector(vectorListForKnn).k(20).numCandidates(100).boost(2.0f);
                                    }
                                    return knn;
                                }))
                                .build();

                        return getProcessedFlux(
                                elasticsearchOperations.search(searchQuery, ProductDocument.class).map(SearchHit::getContent),
                                type, effectiveCity, parsed, userId, originalQuery);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        Query fallbackQuery = NativeQuery.builder()
                                .withQuery(q -> q.bool(b -> {
                                    b.must(m -> m.multiMatch(mm -> mm
                                            .query(parsedQuery)
                                            .fields("title^3", "description", "category")
                                            .fuzziness("AUTO")
                                            .prefixLength(2)
                                            .operator(Operator.Or)));
                                    return b;
                                })).build();
                        return getProcessedFlux(
                                elasticsearchOperations.search(fallbackQuery, ProductDocument.class).map(SearchHit::getContent),
                                type, effectiveCity, parsed, userId, originalQuery);
                    }));
        } else {
            Flux<ProductDocument> resultFlux = searchRepository.findAll();
            return getProcessedFlux(resultFlux, type, tempCity, parsed, userId, originalQuery);
        }
    }

    private Mono<SearchResponse> getProcessedFlux(Flux<ProductDocument> resultFlux, String type, String effectiveCity,
            KeywordParser.ParsedQueryResult parsed, String userId, String originalQuery) {

        resultFlux = resultFlux.distinct();

        if (type != null && !"all".equalsIgnoreCase(type)) {
            final String filterType = type.toLowerCase();
            resultFlux = resultFlux.filter(doc -> {
                String docServiceType = (doc.getServiceType() != null) ? doc.getServiceType().toLowerCase() : "";
                String docType = (doc.getType() != null) ? doc.getType().toLowerCase() : "";
                if (filterType.equals("shop")) return "user".equals(docServiceType) || "shop".equals(docType);
                if (filterType.equals("product") || filterType.equals("products")) return "listing".equals(docServiceType) || "product".equals(docType);
                if (filterType.equals("service") || filterType.equals("services")) return "service".equals(docType);
                return filterType.equals(docServiceType) || filterType.equals(docType);
            });
        }

        final String finalCityFilter = effectiveCity;
        if (finalCityFilter != null && !finalCityFilter.isEmpty()) {
            final String normalizedFilterCity = normalizeAccents(finalCityFilter).toLowerCase();
            resultFlux = resultFlux.filter(doc -> {
                if (doc.getCity() == null) return false;
                String normalizedDocCity = normalizeAccents(doc.getCity()).toLowerCase();
                return doc.getCity().equalsIgnoreCase(finalCityFilter) || normalizedDocCity.equals(normalizedFilterCity);
            });
        }

        return resultFlux.map(doc -> toProductDto(doc, null, null)).collectList()
                .map(results -> SearchResponse.builder().success(true).query(parsed.query).total(results.size()).results(results).build())
                .flatMap(response -> {
                    if (userId != null && parsed.query != null && !parsed.query.isEmpty()) {
                        return searchHistoryPort.saveSearch(userId, parsed.query, type, effectiveCity)
                                .thenReturn(response);
                    }
                    return Mono.just(response);
                });
    }

    private Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private SearchResponse.ProductDto toProductDto(ProductDocument doc, Double userLat, Double userLon) {
        Double distance = null;
        if (userLat != null && userLon != null && doc.getLatitude() != null && doc.getLongitude() != null) {
            distance = calculateDistance(userLat, userLon, doc.getLatitude(), doc.getLongitude());
        }

        String effectiveType = doc.getType();
        if (effectiveType == null || effectiveType.isEmpty()) {
            String st = doc.getServiceType();
            if ("listing".equalsIgnoreCase(st) || "businessbook".equalsIgnoreCase(st)) effectiveType = "product";
            else if ("user".equalsIgnoreCase(st)) effectiveType = "shop";
            else effectiveType = "product";
        }

        return SearchResponse.ProductDto.builder()
                .id(doc.getId()).title(doc.getTitle()).description(doc.getDescription())
                .price(doc.getPrice()).serviceType(doc.getServiceType()).type(effectiveType)
                .category(doc.getCategory()).city(doc.getCity()).quartier(doc.getQuartier())
                .rating(doc.getRating()).images(doc.getImages())
                .latitude(doc.getLatitude()).longitude(doc.getLongitude()).distanceKm(distance)
                .build();
    }
}
