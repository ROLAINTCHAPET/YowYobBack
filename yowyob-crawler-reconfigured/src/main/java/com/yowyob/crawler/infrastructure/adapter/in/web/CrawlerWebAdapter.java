package com.yowyob.crawler.infrastructure.adapter.in.web;

import com.yowyob.crawler.application.port.in.CrawlerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerWebAdapter {

    private final CrawlerUseCase crawlerUseCase;

    @PostMapping("/run")
    public ResponseEntity<String> runNow() {
        crawlerUseCase.executeCrawl();
        return ResponseEntity.ok("Crawl hexagonal terminé avec succès");
    }
}
