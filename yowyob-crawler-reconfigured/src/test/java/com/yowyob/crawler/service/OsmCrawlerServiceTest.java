package com.yowyob.crawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.crawler.dto.ListingEvent;
import com.yowyob.crawler.dto.OverpassResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsmCrawlerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OsmCrawlerService osmCrawlerService;

    // Réponse JSON simulée d'Overpass
    private static final String MOCK_OVERPASS_RESPONSE = """
        {
          "elements": [
            {
              "id": 123456,
              "type": "node",
              "lat": 3.8480,
              "lon": 11.5021,
              "tags": {
                "name": "Restaurant Le Beau Village",
                "amenity": "restaurant",
                "phone": "+237 6XX XXX XXX"
              }
            },
            {
              "id": 789,
              "type": "node",
              "lat": null,
              "lon": null,
              "tags": { "name": "Commerce sans GPS" }
            }
          ]
        }
        """;

    @Test
    @DisplayName("Doit retourner les éléments valides et filtrer ceux sans coordonnées")
    void fetchByTypeAndLocation_shouldReturnValidElements() throws Exception {
        // ARRANGE
        ResponseEntity<String> mockResponse =
            ResponseEntity.ok(MOCK_OVERPASS_RESPONSE);

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(mockResponse);

        OverpassResponse mockParsed = new ObjectMapper()
            .readValue(MOCK_OVERPASS_RESPONSE, OverpassResponse.class);

        when(objectMapper.readValue(anyString(), eq(OverpassResponse.class)))
            .thenReturn(mockParsed);

        // ACT
        List<OverpassResponse.OsmElement> results =
            osmCrawlerService.fetchByTypeAndLocation("restaurant", 3.848, 11.502, 15000);

        // ASSERT
        assertThat(results).hasSize(1); // Le 2e élément sans GPS doit être filtré
        assertThat(results.get(0).getTags().get("name"))
            .isEqualTo("Restaurant Le Beau Village");
        assertThat(results.get(0).getLatitude()).isEqualTo(3.8480);
    }

    @Test
    @DisplayName("Doit retourner une liste vide si Overpass retourne HTTP 429")
    void fetchByTypeAndLocation_shouldReturnEmptyOnRateLimit() {
        // ARRANGE
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // ACT
        List<OverpassResponse.OsmElement> results =
            osmCrawlerService.fetchByTypeAndLocation("restaurant", 3.848, 11.502, 15000);

        // ASSERT
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("toListingEvent doit correctement mapper les tags OSM")
    void toListingEvent_shouldMapTagsCorrectly() {
        // ARRANGE
        OverpassResponse.OsmElement element = new OverpassResponse.OsmElement();
        element.setId(123L);
        element.setLat(3.848);
        element.setLon(11.502);

        Map<String, String> tags = new HashMap<>();
        tags.put("name", "Pharmacie Centrale");
        tags.put("amenity", "pharmacy");
        tags.put("phone", "+237 6XX XXX XXX");
        tags.put("opening_hours", "Mo-Sa 08:00-20:00");
        element.setTags(tags);

        // ACT
        ListingEvent event = osmCrawlerService.toListingEvent(element, "Yaoundé");

        // ASSERT
        assertThat(event.getOsmId()).isEqualTo("osm_123");
        assertThat(event.getName()).isEqualTo("Pharmacie Centrale");
        assertThat(event.getCategory()).isEqualTo("pharmacy");
        assertThat(event.getPhone()).isEqualTo("+237 6XX XXX XXX");
        assertThat(event.getSourceCity()).isEqualTo("Yaoundé");
        assertThat(event.getSource()).isEqualTo("openstreetmap");
        assertThat(event.getLatitude()).isEqualTo(3.848);
    }
}
