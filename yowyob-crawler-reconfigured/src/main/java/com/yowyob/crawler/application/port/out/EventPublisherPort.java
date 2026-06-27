package com.yowyob.crawler.application.port.out;

import com.yowyob.crawler.domain.model.Listing;
import java.util.List;

public interface EventPublisherPort {
    void publishListing(Listing listing);
    void publishAllListings(List<Listing> listings);
}
