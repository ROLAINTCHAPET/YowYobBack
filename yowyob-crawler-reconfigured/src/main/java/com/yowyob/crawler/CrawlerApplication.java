package com.yowyob.crawler;

import com.yowyob.crawler.config.CrawlerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée du Crawler Service YowYob.
 *
 * Responsabilité : découvrir des commerces locaux via Google Places API
 * et publier les données sur Kafka (topic: listings.raw).
 *
 * Architecture :
 *  PlacesSearchService → GooglePlacesCrawlerScheduler → ListingKafkaProducer → Kafka
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(CrawlerProperties.class)
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
        System.out.println("═══════════════════════════════════════");
        System.out.println("   YowYob Crawler Service — démarré");
        System.out.println("   Source : Google Places API");
        System.out.println("   Port   : 8086");
        System.out.println("═══════════════════════════════════════");
    }

    /**
     * ObjectMapper global avec support des types Java 8 Date/Time (Instant, LocalDateTime…).
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}