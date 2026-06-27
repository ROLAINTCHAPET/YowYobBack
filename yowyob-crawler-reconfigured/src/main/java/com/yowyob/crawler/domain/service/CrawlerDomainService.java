package com.yowyob.crawler.domain.service;

import com.yowyob.crawler.application.port.in.CrawlerUseCase;
import com.yowyob.crawler.application.port.out.EventPublisherPort;
import com.yowyob.crawler.application.port.out.ImageClientPort;
import com.yowyob.crawler.application.port.out.OsmClientPort;
import com.yowyob.crawler.config.CrawlerProperties;
import com.yowyob.crawler.domain.model.Listing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlerDomainService implements CrawlerUseCase {

    private final OsmClientPort osmClientPort;
    private final ImageClientPort imageClientPort;
    private final EventPublisherPort eventPublisherPort;
    private final CrawlerProperties props;

    @Override
    public void executeCrawl() {
        log.info("===== DÉBUT CRAWL HEXAGONAL =====");
        int total = 0;

        for (CrawlerProperties.CityConfig city : props.cities()) {
            for (String type : props.osmTypes()) {

                // Étape 1 : Récupération via Port de sortie OSM
                List<Listing> listings = osmClientPort.fetchListings(
                    type, city.lat(), city.lng(), city.radiusMeters(), city.name()
                );

                // Étape 2 : Enrichissement via Port de sortie Image
                List<Listing> enrichedListings = listings.stream()
                    .map(listing -> {
                        String imageUrl = imageClientPort.findImageUrl(
                            listing.getName(), listing.getLatitude(), listing.getLongitude()
                        );
                        listing.setImageUrl(imageUrl);
                        return listing;
                    })
                    .collect(Collectors.toList());

                // Étape 3 : Publication via Port de sortie Événements
                eventPublisherPort.publishAllListings(enrichedListings);
                total += enrichedListings.size();

                // Respect de la politique Overpass
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
