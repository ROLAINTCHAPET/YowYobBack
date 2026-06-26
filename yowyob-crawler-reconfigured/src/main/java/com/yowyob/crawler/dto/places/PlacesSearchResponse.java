package com.yowyob.crawler.dto.places;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO mappant exactement la réponse JSON de l'API Google Places.
 * Endpoints compatibles :
 *   - Nearby Search : /maps/api/place/nearbysearch/json
 *   - Text Search   : /maps/api/place/textsearch/json
 *
 * Documentation : https://developers.google.com/maps/documentation/places/web-service
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacesSearchResponse {

    /** Liste des résultats retournés par Google (20 max par page). */
    @JsonProperty("results")
    private List<PlaceResult> results;

    /**
     * Token permettant de récupérer la page suivante.
     * Google retourne jusqu'à 3 pages (60 résultats max).
     * ATTENTION : le token n'est valide qu'après 2 secondes.
     */
    @JsonProperty("next_page_token")
    private String nextPageToken;

    /**
     * Statut de la réponse.
     * Valeurs possibles : OK, ZERO_RESULTS, OVER_QUERY_LIMIT,
     *                     REQUEST_DENIED, INVALID_REQUEST
     */
    @JsonProperty("status")
    private String status;

    // ─────────────────────────────────────────────────────────────────────
    // Classes internes représentant les éléments du JSON
    // ─────────────────────────────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaceResult {

        /** Identifiant unique Google — utilisé pour la déduplication. */
        @JsonProperty("place_id")
        private String placeId;

        @JsonProperty("name")
        private String name;

        /** Adresse complète formatée. */
        @JsonProperty("formatted_address")
        private String formattedAddress;

        /** Adresse courte (quartier, ville) — disponible en Nearby Search. */
        @JsonProperty("vicinity")
        private String vicinity;

        /** Coordonnées GPS du commerce. */
        @JsonProperty("geometry")
        private Geometry geometry;

        /** Note Google (0.0 à 5.0). */
        @JsonProperty("rating")
        private Double rating;

        /** Nombre total d'avis. */
        @JsonProperty("user_ratings_total")
        private Integer userRatingsTotal;

        /**
         * Types de commerce (ex: ["restaurant", "food", "establishment"]).
         * Le premier type est le plus précis.
         */
        @JsonProperty("types")
        private List<String> types;

        /** Statut d'ouverture en temps réel. */
        @JsonProperty("opening_hours")
        private OpeningHours openingHours;

        /**
         * Statut du commerce.
         * Valeurs : OPERATIONAL, CLOSED_TEMPORARILY, CLOSED_PERMANENTLY
         */
        @JsonProperty("business_status")
        private String businessStatus;

        /**
         * Niveau de prix (0 = gratuit, 4 = très cher).
         * Peut être null si non disponible.
         */
        @JsonProperty("price_level")
        private Integer priceLevel;

        /** Logo/icône du type de commerce. */
        @JsonProperty("icon")
        private String icon;

        /** Photos du commerce (référence pour construire l'URL de la photo). */
        @JsonProperty("photos")
        private List<Photo> photos;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        @JsonProperty("location")
        private Location location;
    }

    @Data
    public static class Location {
        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpeningHours {
        @JsonProperty("open_now")
        private Boolean openNow;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Photo {
        @JsonProperty("photo_reference")
        private String photoReference;

        @JsonProperty("height")
        private Integer height;

        @JsonProperty("width")
        private Integer width;
    }
}
