package com.yowyob.listing.infrastructure.adapter.out.persistence;

import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.model.ListingStatus;
import com.yowyob.listing.entity.Listing;
import com.yowyob.listing.entity.ListingStatus;

// Use qualified names to avoid confusion
public class ListingMapper {

    public static com.yowyob.listing.entity.Listing toEntity(com.yowyob.listing.domain.model.Listing domain) {
        if (domain == null) return null;
        return com.yowyob.listing.entity.Listing.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .category(domain.getCategory())
                .sellerId(domain.getSellerId())
                .address(domain.getAddress())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .status(domain.getStatus() != null ? com.yowyob.listing.entity.ListingStatus.valueOf(domain.getStatus().name()) : null)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static com.yowyob.listing.domain.model.Listing toDomain(com.yowyob.listing.entity.Listing entity) {
        if (entity == null) return null;
        return com.yowyob.listing.domain.model.Listing.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .category(entity.getCategory())
                .sellerId(entity.getSellerId())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus() != null ? com.yowyob.listing.domain.model.ListingStatus.valueOf(entity.getStatus().name()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
