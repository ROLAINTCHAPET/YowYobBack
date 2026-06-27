package com.yowyob.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProduct {
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
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> images;
    private float[] textVector;
}
