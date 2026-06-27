package com.yowyob.search.infrastructure.adapter.out.persistence;

import com.yowyob.search.application.port.out.SearchHistoryPort;
import com.yowyob.search.document.SearchHistory;
import com.yowyob.search.repository.ElasticSearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Elasticsearch adapter implementing the SearchHistoryPort output port.
 * Persists and retrieves user search history documents.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchHistoryAdapter implements SearchHistoryPort {

    private final ElasticSearchHistoryRepository historyRepository;

    @Override
    public Mono<Void> saveSearch(String userId, String query, String type, String city) {
        if (userId == null || userId.isEmpty()) {
            return Mono.empty();
        }
        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .query(query)
                .type(type)
                .city(city)
                .timestamp(Instant.now())
                .build();

        return historyRepository.save(history)
                .doOnError(e -> log.error("Failed to save search history", e))
                .then();
    }

    @Override
    public Flux<SearchHistory> getUserHistory(String userId) {
        return historyRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, 50));
    }
}
