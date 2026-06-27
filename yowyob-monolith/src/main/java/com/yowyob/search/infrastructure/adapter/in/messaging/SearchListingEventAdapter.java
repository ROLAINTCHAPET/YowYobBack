package com.yowyob.search.infrastructure.adapter.in.messaging;

import com.yowyob.config.RabbitMQConfig;
import com.yowyob.listing.event.ListingEvent;
import com.yowyob.search.application.port.in.SearchUseCase;
import com.yowyob.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ messaging adapter for Search.
 * Consumes listing events from the queue and triggers product indexing.
 */
@Component("searchListingEventAdapter")
@Slf4j
@RequiredArgsConstructor
public class SearchListingEventAdapter {

    private final SearchUseCase searchUseCase;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_QUEUE)
    public void handleListingEvent(ListingEvent event) {
        log.info("Received Listing Event: {} for Listing ID: {}", event.getEventType(), event.getId());

        if ("DELETED".equals(event.getEventType())) {
            log.info("Skipping delete for now (not implemented yet)");
        } else {
            ProductDocument doc = new ProductDocument();
            doc.setId(event.getId().toString());
            doc.setTitle(event.getTitle());
            doc.setDescription(event.getDescription());
            doc.setPrice(event.getPrice());
            doc.setCategory(event.getCategory());
            doc.setCity(event.getAddress());
            doc.setServiceType("LISTING");
            doc.setRating(0.0);

            searchUseCase.indexProduct(doc).subscribe(
                    result -> log.info("Successfully indexed listing: {}", result.getId()),
                    error -> log.error("Error indexing listing: {}", error.getMessage()));
        }
    }
}
