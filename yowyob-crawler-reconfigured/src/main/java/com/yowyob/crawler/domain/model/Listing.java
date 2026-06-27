package com.yowyob.crawler.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Listing {
    private String id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String website;
    private String openingHours;
    private String category;
    private String street;
    private String sourceCity;
    private String crawledAt;
    private String source;
    private String imageUrl;
}
