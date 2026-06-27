package com.yowyob.user.application.port.out;

import com.yowyob.user.entity.SearchHistory;

import java.util.List;
import java.util.UUID;

/**
 * Output port for user search history persistence.
 */
public interface SearchHistoryRepositoryPort {
    SearchHistory save(SearchHistory history);
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(UUID userId);
    void deleteByUserId(UUID userId);
}
