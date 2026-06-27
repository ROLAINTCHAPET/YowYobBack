package com.yowyob.listing.application.port.in;

import com.yowyob.listing.domain.model.Listing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface ListingUseCase {
    Listing createListing(Listing listing);
    List<Listing> getAllListings();
    List<Listing> searchListings(LocalDateTime updatedAfter);
    Optional<Listing> getListingById(UUID id);
    List<Listing> getListingsBySellerId(UUID sellerId);
    Listing updateListing(UUID id, Listing listingDetails);
    void deleteListing(UUID id);
}
