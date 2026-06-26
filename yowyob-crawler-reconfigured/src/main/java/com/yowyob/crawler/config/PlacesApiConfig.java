package com.yowyob.crawler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration du client HTTP pour Google Places API.
 * Définit un RestTemplate dédié avec timeouts adaptés aux appels externes.
 */
@Configuration
public class PlacesApiConfig {

    @Value("${google.places.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${google.places.read-timeout-ms:15000}")
    private int readTimeoutMs;

    /**
     * Bean RestTemplate utilisé exclusivement pour les appels vers Google Places API.
     * Nommé "placesRestTemplate" pour éviter tout conflit avec d'autres beans RestTemplate.
     */
    @Bean(name = "placesRestTemplate")
    public RestTemplate placesRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
