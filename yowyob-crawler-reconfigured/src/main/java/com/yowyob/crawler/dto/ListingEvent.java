package com.yowyob.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingEvent {

    private String osmId;           // identifiant unique OSM
    private String name;            // nom du commerce
    private String address;         // adresse complète
    private Double latitude;
    private Double longitude;
    private String phone;           // tag "phone" ou "contact:phone"
    private String website;         // tag "website"
    private String openingHours;    // tag "opening_hours"
    private String category;        // type OSM (restaurant, pharmacy...)
    private String imageUrl;        // ← MODIFIÉ (pour matcher avec le listing-service)
    private String street;          // ← NOUVEAU (rue extraite d'OSM)
    private String sourceCity;      // ville crawlée
    private String crawledAt;       // timestamp ISO-8601
    private String source;          // toujours "openstreetmap"
}
