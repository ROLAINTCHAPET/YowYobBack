package com.yowyob.user.infrastructure.adapter.out.persistence;

import com.yowyob.user.application.port.out.SearchHistoryRepositoryPort;
import com.yowyob.user.entity.SearchHistory;
import com.yowyob.user.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * JPA adapter implementing the SearchHistoryRepositoryPort output port.
 */
@Component
@RequiredArgsConstructor
public class JpaSearchHistoryRepositoryAdapter implements SearchHistoryRepositoryPort {

    private final SearchHistoryRepository searchHistoryRepository;

    @Override
    public SearchHistory save(SearchHistory history) {
        return searchHistoryRepository.save(history);
    }

    @Override
    public List<SearchHistory> findByUserIdOrderBySearchedAtDesc(UUID userId) {
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        searchHistoryRepository.deleteByUserId(userId);
    }
}
