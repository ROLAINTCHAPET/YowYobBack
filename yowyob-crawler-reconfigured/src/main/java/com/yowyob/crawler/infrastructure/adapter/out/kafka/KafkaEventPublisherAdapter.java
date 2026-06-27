package com.yowyob.crawler.infrastructure.adapter.out.kafka;

import com.yowyob.crawler.application.port.out.EventPublisherPort;
import com.yowyob.crawler.domain.model.Listing;
import com.yowyob.crawler.dto.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    @Value("${kafka.topics.listings}")
    private String topic;

    private final KafkaTemplate<String, ListingEvent> kafkaTemplate;

    @Override
    public void publishListing(Listing listing) {
        ListingEvent event = toEvent(listing);
        kafkaTemplate.send(topic, event.getOsmId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Échec Kafka pour {} : {}", event.getName(), ex.getMessage());
                } else {
                    log.debug("Publié : {}", event.getName());
                }
            });
    }

    @Override
    public void publishAllListings(List<Listing> listings) {
        listings.forEach(this::publishListing);
        log.info("{} événements envoyés vers '{}'", listings.size(), topic);
    }

    private ListingEvent toEvent(Listing listing) {
        return ListingEvent.builder()
            .osmId(listing.getId())
            .name(listing.getName())
            .address(listing.getAddress())
            .latitude(listing.getLatitude())
            .longitude(listing.getLongitude())
            .phone(listing.getPhone())
            .website(listing.getWebsite())
            .openingHours(listing.getOpeningHours())
            .category(listing.getCategory())
            .street(listing.getStreet())
            .sourceCity(listing.getSourceCity())
            .crawledAt(listing.getCrawledAt())
            .source(listing.getSource())
            .imageUrl(listing.getImageUrl())
            .build();
    }
}
