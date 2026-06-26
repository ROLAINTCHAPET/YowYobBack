package com.yowyob.search.service;

import com.yowyob.search.client.GeoServiceClient;
import com.yowyob.search.document.ProductDocument;
import com.yowyob.search.dto.ProductWithDistance;
import com.yowyob.search.dto.SearchResponse;
import com.yowyob.search.repository.ProductSearchRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ProductSearchRepository searchRepository;
    private final ReactiveElasticsearchOperations elasticsearchOperations;
    private final GeoServiceClient geoServiceClient;
    private final KeywordParser keywordParser;
    private final SearchHistoryService searchHistoryService;

    /**
     * Normalise une chaîne en supprimant les accents "Yaoundé" → "Yaounde"
     */
    private String normalizeAccents(String input) {
        if (input == null)
            return "";
        // Remplacement simple des accents français/allemands
        return input.replace("é", "e").replace("È", "E").replace("É", "E").replace("è", "e").replace("ê", "e")
                .replace("ë", "e").replace("à", "a").replace("â", "a").replace("ù", "u").replace("û", "u")
                .replace("ô", "o").replace("ö", "o").replace("ç", "c").replace("Ç", "C").replace("î", "i")
                .replace("ï", "i");
    }

    public Mono<SearchResponse> search(String query, String type, String city, String userId, String ipAddress) {
        log.info("Searching: query={}, type={}, city={}, userId={}, ip={}", query, type, city, userId, ipAddress);

        // 1. Analyse Sémantique de la requête
        KeywordParser.ParsedQueryResult parsed = keywordParser.parseWithCity(query);

        // 2. Vérifier si c'est une recherche "Près de chez moi"
        if (parsed.isProximitySearch && ipAddress != null) {
            log.info("Proximity search detected via IP: {}", ipAddress);
            return geoServiceClient.getLocationFromIp(ipAddress).flatMap(location -> {
                if (location.getLatitude() != null && location.getLongitude() != null) {
                    return searchByUserProximity(query, location.getLatitude(), location.getLongitude(), type);
                }
                return Mono.empty();
            }).switchIfEmpty(Mono.defer(() -> executeStandardSearch(parsed, query, type, city, userId)));
        }

        return executeStandardSearch(parsed, query, type, city, userId);
    }

    private Mono<SearchResponse> executeStandardSearch(KeywordParser.ParsedQueryResult parsed, String originalQuery,
            String type, String city, String userId) {
        Flux<ProductDocument> resultFlux;
        String effectiveCity = city;
        // String query = parsed.queryOriginal; // We need original or parsed?
        // parsed.query is "pizza" (without "near me")

        // Initialisation de la requête native builder
        Query searchQuery;

        if (parsed.query != null && !parsed.query.isEmpty()) {
            String parsedQuery = parsed.query;
            String extractedCity = parsed.extractedCity;
            String inferredCategory = parsed.inferredCategory;

            if (effectiveCity == null && extractedCity != null) {
                effectiveCity = extractedCity;
            }

            // 2. Construction de la requête "Intelligente" (Boolean Query)
            // L'objectif est de combiner les facteurs de pertinence dans le score de score
            searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        // A. Clause Principale (Match Textuel large avec tolérance)
                         if (parsedQuery != null && !parsedQuery.isEmpty()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(parsedQuery)
                                    .fields("title^3", "description", "category")
                                    .fuzziness("AUTO") // Tolérance aux fautes (ex: "iphon" -> "iphone")
                                    .prefixLength(2) // Les 2 premières lettres doivent correspondre
                                    .operator(Operator.Or) // Au moins un terme doit correspondre
                            ));

                            // B. Boost: Correspondance exacte de phrase (ex: "Maison Bastos") -> Boost
                            // Maxime
                            b.should(s -> s.matchPhrase(mp -> mp.field("description").query(parsedQuery)
                                    .boost(10.0f) // Boost massif pour phrase exacte
                            ));
                            b.should(s -> s.matchPhrase(mp -> mp.field("title").query(parsedQuery).boost(15.0f) // Boost
                                                                                                                // encore
                                                                                                                // plus
                                                                                                                // massif
                                                                                                                // pour
                                                                                                                // titre
                                                                                                                // exact
                            ));
                        }

                        // C. Boost: Catégorie Déduite (ex: "manger" -> boost Restaurant)
                        if (inferredCategory != null) {
                            b.should(s -> s.match(m -> m.field("category").query(inferredCategory)
                                    .boost(5.0f) // Pousse les résultats de la bonne catégorie vers le haut
                            ));
                            b.should(s -> s.match(m -> m.field("serviceType").query(inferredCategory).boost(5.0f)));
                        }

                        // D. Boost: Localisation (si ville détectée ou fournie)
                        if (extractedCity != null) {
                            b.should(s -> s.match(m -> m.field("city").query(extractedCity).boost(3.0f)));
                        }

                        return b;
                    }))
                    .build();

            // Exécution unique de la requête optimisée
            resultFlux = elasticsearchOperations.search(searchQuery, ProductDocument.class)
                    .map(SearchHit::getContent);

        } else {
            // Pas de requête textuelle, tout récupérer
            resultFlux = searchRepository.findAll();
        }

        resultFlux = resultFlux.distinct();

        if (type != null && !"all".equalsIgnoreCase(type)) {
            final String filterType = type.toLowerCase();
            resultFlux = resultFlux.filter(doc -> {
                // Get both possible type fields
                String docServiceType = (doc.getServiceType() != null) ? doc.getServiceType().toLowerCase() : "";
                String docType = (doc.getType() != null) ? doc.getType().toLowerCase() : "";

                if (filterType.equals("shop")) {
                    // Shops: serviceType=user OR type=shop
                    return "user".equals(docServiceType) || "shop".equals(docType);
                } else if (filterType.equals("product") || filterType.equals("products")) {
                    // Products: serviceType=listing OR type=product
                    return "listing".equals(docServiceType) || "product".equals(docType);
                } else if (filterType.equals("service") || filterType.equals("services")) {
                    // Services: type=service (no crawler equivalent)
                    return "service".equals(docType);
                }

                // Fallback: match either field
                return filterType.equals(docServiceType) || filterType.equals(docType);
            });
        }

        final String finalCityForFilter = effectiveCity;
        if (finalCityForFilter != null && !finalCityForFilter.isEmpty()) {
            final String normalizedFilterCity = normalizeAccents(finalCityForFilter).toLowerCase();
            resultFlux = resultFlux.filter(doc -> {
                if (doc.getCity() == null)
                    return false;
                // Comparer avec normalisation des accents
                String normalizedDocCity = normalizeAccents(doc.getCity()).toLowerCase();
                // Comparaison directe case-insensitive
                boolean directMatch = doc.getCity().equalsIgnoreCase(finalCityForFilter);
                // Comparaison après normalisation des accents
                boolean normalizedMatch = normalizedDocCity.contains(normalizedFilterCity);
                return directMatch || normalizedMatch;
            });
        }

        return resultFlux.map(doc -> toProductDto(doc, null, null)).collectList()
                .map(results -> SearchResponse.builder().success(true)
                        .query(parsed.query).total(results.size()).results(results).build())
                .flatMap(response -> {
                    if (userId != null && parsed.query != null && !parsed.query.isEmpty()) {
                        return searchHistoryService.saveSearch(userId, parsed.query, type, city).thenReturn(response);
                    }
                    return Mono.just(response);
                });
    }

    public Mono<ProductDocument> indexProduct(ProductDocument product) {
        // If city is provided but coordinates are missing, geocode the city
        if (product.getCity() != null && (product.getLatitude() == null || product.getLongitude() == null)) {
            return geoServiceClient.geocode(product.getCity()).map(geoLocation -> {
                product.setLatitude(geoLocation.getLatitude());
                product.setLongitude(geoLocation.getLongitude());
                return product;
            }).switchIfEmpty(Mono.just(product)).flatMap(searchRepository::save)
                    .doOnNext(saved -> log.info("Product indexed with geo coordinates: {}", saved.getTitle()));
        }

        return searchRepository.save(product);
    }

    public Mono<ProductDocument> getProductById(String id) {
        log.info("Getting product details for ID: {}", id);
        return searchRepository.findById(id).doOnNext(doc -> log.info("Found product: {}", doc.getTitle()))
                .doOnError(error -> log.error("Error retrieving product: {}", error.getMessage()));
    }

    /**
     * Search products by proximity to user's location Supports queries like
     * "restaurants près de chez moi"
     * 
     * @param query         Search query (can include proximity expressions)
     * @param userLatitude  User's latitude
     * @param userLongitude User's longitude
     * @param type          Product type filter
     * @return Mono containing search results sorted by distance
     */
    public Mono<SearchResponse> searchByUserProximity(String query, Double userLatitude, Double userLongitude,
            String type) {
        log.info("Searching by user proximity: query={}, lat={}, lon={}, type={}", query, userLatitude, userLongitude,
                type);

        if (userLatitude == null || userLongitude == null) {
            log.warn("User location not provided, falling back to standard search");
            return search(query, type, null, null, null);
        }

        // Parse the query to extract keywords and proximity radius
        KeywordParser.ParsedQueryResult parsed = keywordParser.parseWithCity(query);
        String parsedQuery = parsed.query;
        Double proximityRadius = parsed.proximityRadius != null ? parsed.proximityRadius : 10.0; // Default 10km

        log.info("Parsed query: {}", parsedQuery);
        log.info("Is proximity search: {}, radius: {}km", parsed.isProximitySearch, proximityRadius);

        // Construction de la requête SANS geo_distance (problème avec Spring Data
        // Elasticsearch)
        try {
            Query searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        // Requête textuelle si fournie
                        if (parsedQuery != null && !parsedQuery.isEmpty()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(parsedQuery)
                                    .fields("title^3", "description", "category", "city^2")
                                    .fuzziness("AUTO")
                                    .prefixLength(2)
                                    .operator(Operator.Or)));
                        } else {
                            // Si pas de requête textuelle, match all
                            b.must(m -> m.matchAll(ma -> ma));
                        }

                        // Filtre par type si fourni
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

            log.info("Executing proximity search with manual distance filtering and sorting");

            // Exécution de la requête Elasticsearch
            return elasticsearchOperations.search(searchQuery, ProductDocument.class)
                    .map(SearchHit::getContent)
                    .filter(doc -> {
                        // Filtrage manuel par distance
                        if (doc.getLatitude() != null && doc.getLongitude() != null) {
                            double distance = calculateDistance(userLatitude, userLongitude,
                                    doc.getLatitude(), doc.getLongitude());
                            return distance <= proximityRadius;
                        }
                        return false;
                    })
                    .collectList()
                    .map(results -> {
                        // Tri manuel par distance
                        results.sort((a, b) -> {
                            double distA = calculateDistance(userLatitude, userLongitude,
                                    a.getLatitude(), a.getLongitude());
                            double distB = calculateDistance(userLatitude, userLongitude,
                                    b.getLatitude(), b.getLongitude());
                            return Double.compare(distA, distB);
                        });

                        log.info("Found {} results within {}km", results.size(), proximityRadius);

                        // Conversion en DTO
                        List<SearchResponse.ProductDto> dtos = results.stream()
                                .map(doc -> toProductDto(doc, userLatitude, userLongitude))
                                .toList();

                        return SearchResponse.builder()
                                .success(true)
                                .query(query)
                                .total(dtos.size())
                                .results(dtos)
                                .build();
                    })
                    .defaultIfEmpty(SearchResponse.builder()
                            .success(true)
                            .query(query)
                            .total(0)
                            .results(List.of())
                            .build())
                    .onErrorResume(e -> {
                        log.error("Error executing proximity search", e);
                        return Mono.just(SearchResponse.builder()
                                .success(false)
                                .query(query)
                                .total(0)
                                .results(null)
                                .build());
                    });
        } catch (Exception e) {
            log.error("Error building proximity search query", e);
            return Mono.just(SearchResponse.builder()
                    .success(false)
                    .query(query)
                    .total(0)
                    .results(null)
                    .build());
        }
    }

    public Mono<List<String>> autocomplete(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Mono.just(List.of());
        }

        String cleanQuery = query.trim();

        return searchRepository.findByTitleContainingOrDescriptionContaining(cleanQuery, cleanQuery)
                .map(ProductDocument::getTitle).distinct().take(5) // Limit to 5 suggestions
                .collectList();
    }

    /**
     * Search products by proximity to a city
     * 
     * @param query    Search query
     * @param city     Reference city for proximity search
     * @param radiusKm Search radius in kilometers
     * @return Mono containing search results
     */
    public Mono<SearchResponse> searchByProximity(String query, String city, Double radiusKm) {
        log.info("Searching by proximity: query={}, city={}, radiusKm={}", query, city, radiusKm);

        return geoServiceClient.geocode(city).flatMap(geoLocation -> {
            log.info("Geocoded reference city: lat={}, lng={}", geoLocation.getLatitude(), geoLocation.getLongitude());

            final Double radius = radiusKm != null ? radiusKm : 10.0; // Default 10 km

            // Construction de la requête avec geo_distance
            Query searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {
                        // Requête textuelle si fournie
                        if (query != null && !query.isEmpty()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title^3", "description", "category")
                                    .fuzziness("AUTO")));
                        }

                        // Filtre géographique natif
                        b.filter(f -> f.geoDistance(gd -> gd
                                .field("location")
                                .distance(radius + "km")
                                .location(gl -> gl.latlon(latlon -> latlon
                                        .lat(geoLocation.getLatitude())
                                        .lon(geoLocation.getLongitude())))));

                        return b;
                    }))
                    // Tri par distance
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

    /**
     * Calculate distance between two coordinates using Haversine formula
     * 
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in kilometers
     */
    private Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private SearchResponse.ProductDto toProductDto(ProductDocument doc, Double userLat, Double userLon) {
        Double distance = null;
        if (userLat != null && userLon != null && doc.getLatitude() != null && doc.getLongitude() != null) {
            distance = calculateDistance(userLat, userLon, doc.getLatitude(), doc.getLongitude());
        }

        // Determine the effective type: prefer explicit 'type' field, fall back to
        // serviceType mapping
        String effectiveType = doc.getType();
        if (effectiveType == null || effectiveType.isEmpty()) {
            String st = doc.getServiceType();
            if ("listing".equalsIgnoreCase(st) || "businessbook".equalsIgnoreCase(st)) {
                effectiveType = "product";
            } else if ("user".equalsIgnoreCase(st)) {
                effectiveType = "shop";
            } else {
                effectiveType = "product"; // default
            }
        }

        return SearchResponse.ProductDto.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .price(doc.getPrice())
                .serviceType(doc.getServiceType())
                .type(effectiveType)
                .category(doc.getCategory())
                .city(doc.getCity())
                .quartier(doc.getQuartier())
                .rating(doc.getRating())
                .images(doc.getImages())
                .latitude(doc.getLatitude())
                .longitude(doc.getLongitude())
                .distanceKm(distance)
                .build();
    }
}