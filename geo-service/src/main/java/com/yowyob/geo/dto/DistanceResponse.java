package com.yowyob.geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistanceResponse {
    private Double distanceKm;
    private Double distanceMiles;
}
