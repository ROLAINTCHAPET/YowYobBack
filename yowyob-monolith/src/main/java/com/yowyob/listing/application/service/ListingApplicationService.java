package com.yowyob.listing.application.service;

import com.yowyob.auth.exception.ResourceNotFoundException;
import com.yowyob.listing.application.port.in.ListingUseCase;
import com.yowyob.listing.application.port.out.ListingEventPublisherPort;
import com.yowyob.listing.application.port.out.ListingRepositoryPort;
import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.model.ListingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingApplicationService implements ListingUseCase {

    private final ListingRepositoryPort listingRepositoryPort;
    private final ListingEventPublisherPort listingEventPublisherPort;

    @Override
    public Listing createListing(Listing listing) {
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        if (listing.getStatus() == null) {
            listing.setStatus(ListingStatus.ACTIVE);
        }
        Listing savedListing = listingRepositoryPort.save(listing);
        listingEventPublisherPort.publishListingCreated(savedListing);
        return savedListing;
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepositoryPort.findAll();
    }

    @Override
    public List<Listing> searchListings(LocalDateTime updatedAfter) {
        return listingRepositoryPort.findByUpdatedAtAfter(updatedAfter);
    }

    @Override
    public Optional<Listing> getListingById(UUID id) {
        return listingRepositoryPort.findById(id);
    }

    @Override
    public List<Listing> getListingsBySellerId(UUID sellerId) {
        return listingRepositoryPort.findBySellerId(sellerId);
    }

    @Override
    public Listing updateListing(UUID id, Listing listingDetails) {
        Listing listing = listingRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", id.toString()));

        listing.setTitle(listingDetails.getTitle());
        listing.setDescription(listingDetails.getDescription());
        listing.setPrice(listingDetails.getPrice());
        listing.setCategory(listingDetails.getCategory());
        listing.setAddress(listingDetails.getAddress());
        listing.setLatitude(listingDetails.getLatitude());
        listing.setLongitude(listingDetails.getLongitude());
        listing.setStatus(listingDetails.getStatus());
        listing.setUpdatedAt(LocalDateTime.now());

        Listing updatedListing = listingRepositoryPort.save(listing);
        listingEventPublisherPort.publishListingUpdated(updatedListing);
        return updatedListing;
    }

    @Override
    public void deleteListing(UUID id) {
        Listing listing = listingRepositoryPort.findById(id).orElse(null);
        if (listing != null) {
            listingRepositoryPort.delete(listing);
            listingEventPublisherPort.publishListingDeleted(listing);
        }
    }
}
