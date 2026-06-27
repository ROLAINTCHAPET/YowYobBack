package com.yowyob.listing.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {
    private UUID id;
    private String title;
    private String description;
    private Double price;
    private String category;
    private UUID sellerId;
    private String address;
    private Double latitude;
    private Double longitude;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
