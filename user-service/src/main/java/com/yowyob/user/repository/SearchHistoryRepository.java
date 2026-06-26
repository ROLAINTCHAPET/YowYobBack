package com.yowyob.user.repository;

import com.yowyob.user.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(UUID userId);

    void deleteByUserId(UUID userId);
}
