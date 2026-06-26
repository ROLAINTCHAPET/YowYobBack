package com.yowyob.listing.service;

import com.yowyob.listing.config.RabbitMQConfig;
import com.yowyob.listing.entity.Listing;
import com.yowyob.listing.entity.ListingStatus;
import com.yowyob.listing.event.ListingEvent;
import com.yowyob.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final RabbitTemplate rabbitTemplate;

    public Listing createListing(Listing listing) {
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        if (listing.getStatus() == null) {
            listing.setStatus(ListingStatus.ACTIVE);
        }
        if (listing.getSellerId() == null) {
            // Assign a default system seller ID if missing (e.g. from crawler)
            listing.setSellerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
        Listing savedListing = listingRepository.save(listing);

        publishEvent(savedListing, "CREATED");

        return savedListing;
    }

    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    public List<Listing> searchListings(LocalDateTime updatedAfter) {
        if (updatedAfter == null) {
            return listingRepository.findAll();
        }
        return listingRepository.findByUpdatedAtAfter(updatedAfter);
    }

    public Optional<Listing> getListingById(UUID id) {
        return listingRepository.findById(id);
    }

    public List<Listing> getListingsBySellerId(UUID sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }

    public Listing updateListing(UUID id, Listing listingDetails) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id " + id));

        listing.setTitle(listingDetails.getTitle());
        listing.setDescription(listingDetails.getDescription());
        listing.setPrice(listingDetails.getPrice());
        listing.setCategory(listingDetails.getCategory());
        listing.setAddress(listingDetails.getAddress());
        listing.setLatitude(listingDetails.getLatitude());
        listing.setLongitude(listingDetails.getLongitude());
        
        listing.setImageUrl(listingDetails.getImageUrl());
        listing.setPhone(listingDetails.getPhone());
        listing.setOpeningHours(listingDetails.getOpeningHours());
        listing.setRating(listingDetails.getRating());
        listing.setReviewsCount(listingDetails.getReviewsCount());
        
        listing.setStatus(listingDetails.getStatus());
        listing.setUpdatedAt(LocalDateTime.now());

        Listing updatedListing = listingRepository.save(listing);

        publishEvent(updatedListing, "UPDATED");

        return updatedListing;
    }

    public void deleteListing(UUID id) {
        Listing listing = listingRepository.findById(id).orElse(null);
        if (listing != null) {
            listingRepository.delete(listing);
            publishEvent(listing, "DELETED");
        }
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
                .imageUrl(listing.getImageUrl())
                .phone(listing.getPhone())
                .openingHours(listing.getOpeningHours())
                .rating(listing.getRating())
                .reviewsCount(listing.getReviewsCount())
                .status(listing.getStatus().name())
                .sellerId(listing.getSellerId())
                .eventType(eventType)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "listing." + eventType.toLowerCase(), event);
    }
}
