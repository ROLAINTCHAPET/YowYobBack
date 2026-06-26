package com.yowyob.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private Boolean success;
    private String query;
    private Integer total;
    private List<ProductDto> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDto {
        private String id;
        private String title;
        private String description;
        private Double price;
        private String serviceType;
        private String type;
        private String category;
        private String city;
        private String quartier;
        private Double rating;
        private List<String> images;
        private Double latitude;
        private Double longitude;

        // Proximity info
        private Double distanceKm;

        // Link to full listing details
        public String getDetailsUrl() {
            return "/api/search/" + id + "/details";
        }
    }
}