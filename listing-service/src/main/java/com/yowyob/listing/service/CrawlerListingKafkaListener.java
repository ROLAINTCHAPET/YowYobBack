package com.yowyob.listing.service;

import com.yowyob.listing.dto.CrawlerListingRequest;
import com.yowyob.listing.entity.Listing;
import com.yowyob.listing.entity.ListingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class CrawlerListingKafkaListener {

    private final ListingService listingService;

    public CrawlerListingKafkaListener(ListingService listingService) {
        this.listingService = listingService;
    }

    @KafkaListener(
            topics = "crawler.listings.events",
            groupId = "listing-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCrawlerListing(CrawlerListingRequest request,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received crawler listing from topic={} offset={} listingId={}",
                topic, offset, request.getExternalId());
        try {
            Listing listing = Listing.builder()
                    .title(request.getTitle())
                    .description(request.getDescription() != null ? request.getDescription() : "Source: " + request.getSource())
                    .price(request.getPrice() != null ? request.getPrice() : 0.0)
                    .category(request.getCategory() != null ? request.getCategory() : "GENERAL")
                    .address(request.getCity() != null ? request.getCity() + ", " + request.getCountry() : request.getCountry())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .imageUrl(request.getImageUrl())
                    .phone(request.getPhone())
                    .openingHours(request.getOpeningHours())
                    .rating(request.getRating())
                    .reviewsCount(request.getReviewsCount())
                    .status(ListingStatus.ACTIVE)
                    .externalId(request.getExternalId())
                    // Use a generic system UUID for crawler bot
                    .sellerId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                    .build();

            listingService.createListing(listing);
            log.info("Listing saved successfully — externalId={}", request.getExternalId());
        } catch (Exception e) {
            log.error("Failed to process listing externalId={} — reason: {}",
                    request.getExternalId(), e.getMessage(), e);
            throw e; // Relance pour déclencher le DefaultErrorHandler → DLT
        }
    }
}
