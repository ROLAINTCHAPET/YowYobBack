package com.yowyob.crawler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverpassResponse {

    @JsonProperty("elements")
    private List<OsmElement> elements;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OsmElement {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("type")
        private String type; // "node", "way", "relation"

        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lon")
        private Double lon;

        @JsonProperty("tags")
        private Map<String, String> tags; // nom, adresse, téléphone, horaires...

        // Centroid pour les "way" et "relation" (pas de lat/lon direct)
        @JsonProperty("center")
        private Center center;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Center {
            @JsonProperty("lat")
            private Double lat;
            @JsonProperty("lon")
            private Double lon;
        }

        // Retourne la latitude quelle que soit le type d'élément
        public Double getLatitude() {
            if (lat != null) return lat;
            if (center != null) return center.getLat();
            return null;
        }

        public Double getLongitude() {
            if (lon != null) return lon;
            if (center != null) return center.getLon();
            return null;
        }
    }
}
