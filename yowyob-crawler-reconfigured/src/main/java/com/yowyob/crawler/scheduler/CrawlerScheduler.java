package com.yowyob.crawler.scheduler;

import com.yowyob.crawler.config.CrawlerProperties;
import com.yowyob.crawler.dto.ListingEvent;
import com.yowyob.crawler.dto.OverpassResponse;
import com.yowyob.crawler.kafka.ListingKafkaProducer;
import com.yowyob.crawler.service.OsmCrawlerService;
import com.yowyob.crawler.service.WikimediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrawlerScheduler {

    private final OsmCrawlerService osmService;
    private final WikimediaService wikimediaService;   // ← NOUVEAU
    private final ListingKafkaProducer kafkaProducer;
    private final CrawlerProperties props;

    @Scheduled(cron = "${crawler.schedule.cron}")
    public void runCrawl() {
        log.info("===== DÉBUT CRAWL OSM + WIKIMEDIA =====");
        int total = 0;

        for (CrawlerProperties.CityConfig city : props.cities()) {
            for (String type : props.osmTypes()) {

                // Étape 1 : récupération OSM
                List<OverpassResponse.OsmElement> elements =
                    osmService.fetchByTypeAndLocation(
                        type, city.lat(), city.lng(), city.radiusMeters()
                    );

                // Étape 2 : enrichissement Wikimedia + construction ListingEvent
                List<ListingEvent> events = elements.stream()
                    .map(e -> {
                        ListingEvent event = osmService.toListingEvent(e, city.name());

                        // Enrichissement photo (peut retourner null — c'est OK)
                        String photo = wikimediaService.findPhoto(
                            event.getName(),
                            event.getLatitude(),
                            event.getLongitude()
                        );
                        event.setImageUrl(photo);

                        return event;
                    })
                    .collect(Collectors.toList());

                // Étape 3 : envoi vers Kafka
                kafkaProducer.publishAll(events);
                total += events.size();

                // Pause obligatoire (politique Overpass)
                sleep(2000);
            }
        }

        log.info("===== FIN CRAWL : {} commerces publiés =====", total);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
