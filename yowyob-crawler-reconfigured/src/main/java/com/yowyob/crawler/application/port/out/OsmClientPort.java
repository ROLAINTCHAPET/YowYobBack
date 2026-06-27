package com.yowyob.crawler.application.port.out;

import com.yowyob.crawler.domain.model.Listing;
import java.util.List;

public interface OsmClientPort {
    List<Listing> fetchListings(String type, double lat, double lng, int radius, String city);
}
