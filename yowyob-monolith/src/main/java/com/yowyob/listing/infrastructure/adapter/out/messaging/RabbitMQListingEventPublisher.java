package com.yowyob.listing.infrastructure.adapter.out.messaging;

import com.yowyob.config.RabbitMQConfig;
import com.yowyob.listing.application.port.out.ListingEventPublisherPort;
import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.event.ListingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQListingEventPublisher implements ListingEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishListingCreated(Listing listing) {
        publishEvent(listing, "CREATED");
    }

    @Override
    public void publishListingUpdated(Listing listing) {
        publishEvent(listing, "UPDATED");
    }

    @Override
    public void publishListingDeleted(Listing listing) {
        publishEvent(listing, "DELETED");
    }

    private void publishEvent(Listing listing, String eventType) {
        ListingEvent event = ListingEvent.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .price(listing.getPrice())
                .category(listing.getCategory())
                .address(listing.getAddress())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .status(listing.getStatus().name())
                .sellerId(listing.getSellerId())
                .eventType(eventType)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.LISTING_EXCHANGE, "listing." + eventType.toLowerCase(), event);
    }
}
