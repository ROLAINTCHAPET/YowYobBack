package com.yowyob.search.listener;

import com.yowyob.search.config.RabbitMQConfig;
import com.yowyob.search.event.ListingEvent;
import com.yowyob.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListingEventListener {

    private final SearchService searchService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleListingEvent(ListingEvent event) {
        log.info("Received Listing Event: {} for Listing ID: {}", event.getEventType(), event.getId());

        if ("DELETED".equals(event.getEventType())) {
            // searchService.deleteProduct(event.getId()); // Assuming delete implemented
            log.info("Skipping delete for now (not implemented in SearchService yet)");
        } else {
            com.yowyob.search.document.ProductDocument doc = new com.yowyob.search.document.ProductDocument();
            doc.setId(event.getId().toString());
            doc.setTitle(event.getTitle());
            doc.setDescription(event.getDescription());
            doc.setPrice(event.getPrice());
            doc.setCategory(event.getCategory());
            doc.setCity(event.getAddress()); // Using address as city for now
            doc.setServiceType("LISTING");
            
            doc.setImageUrl(event.getImageUrl());
            doc.setRating(event.getRating());
            doc.setReviewsCount(event.getReviewsCount());
            doc.setPhone(event.getPhone());
            doc.setOpeningHours(event.getOpeningHours());

            searchService.indexProduct(doc).subscribe(
                    result -> log.info("Successfully indexed listing: {}", result.getId()),
                    error -> log.error("Error indexing listing: {}", error.getMessage()));
        }
    }
}
