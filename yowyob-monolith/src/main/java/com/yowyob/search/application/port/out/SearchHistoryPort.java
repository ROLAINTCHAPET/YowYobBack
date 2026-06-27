package com.yowyob.search.application.port.out;

import com.yowyob.search.document.SearchHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port for persisting and retrieving user search history.
 */
public interface SearchHistoryPort {
    Mono<Void> saveSearch(String userId, String query, String type, String city);
    Flux<SearchHistory> getUserHistory(String userId);
}
