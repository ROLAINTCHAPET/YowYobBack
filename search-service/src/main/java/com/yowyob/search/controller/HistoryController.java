package com.yowyob.search.controller;

import com.yowyob.search.document.SearchHistory;
import com.yowyob.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/search/history")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {
    private final SearchHistoryService historyService;

    @GetMapping
    public Flux<SearchHistory> getHistory(@RequestHeader(value = "X-User-Id") String userId) {
        log.info("Fetching history for User ID: {}", userId);
        return historyService.getUserHistory(userId)
                .doOnComplete(() -> log.info("History fetch completed for user: {}", userId))
                .doOnError(e -> log.error("Error fetching history for user: {}", userId, e));
    }
}
