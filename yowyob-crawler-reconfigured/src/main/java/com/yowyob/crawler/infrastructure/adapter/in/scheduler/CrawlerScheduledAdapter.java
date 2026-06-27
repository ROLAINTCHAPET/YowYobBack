package com.yowyob.crawler.infrastructure.adapter.in.scheduler;

import com.yowyob.crawler.application.port.in.CrawlerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrawlerScheduledAdapter {

    private final CrawlerUseCase crawlerUseCase;

    @Scheduled(cron = "${crawler.schedule.cron}")
    public void runCrawl() {
        log.info("Lancement du crawl planifié (adapter hexagonal)");
        crawlerUseCase.executeCrawl();
    }
}
