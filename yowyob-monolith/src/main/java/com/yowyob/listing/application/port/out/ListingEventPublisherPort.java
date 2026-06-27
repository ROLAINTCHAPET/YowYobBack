package com.yowyob.listing.application.port.out;

import com.yowyob.listing.domain.model.Listing;

public interface ListingEventPublisherPort {
    void publishListingCreated(Listing listing);
    void publishListingUpdated(Listing listing);
    void publishListingDeleted(Listing listing);
}
