package com.yowyob.crawler.controller;

import com.yowyob.crawler.scheduler.CrawlerScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerScheduler scheduler;

    @PostMapping("/run")
    public ResponseEntity<String> runNow() {
        scheduler.runCrawl();
        return ResponseEntity.ok("Crawl terminé");
    }
}
