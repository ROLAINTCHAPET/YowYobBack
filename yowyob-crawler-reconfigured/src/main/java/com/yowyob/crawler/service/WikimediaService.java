package com.yowyob.crawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.crawler.dto.WikimediaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class WikimediaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String WIKIMEDIA_URL =
        "https://commons.wikimedia.org/w/api.php";

    /**
     * Stratégie hybride :
     * 1. Cherche par nom du commerce
     * 2. Si rien → cherche par coordonnées GPS (rayon 100m)
     * 3. Si rien → retourne null (acceptable)
     */
    public String findPhoto(String name, double lat, double lng) {
        // Étape 1 : recherche par nom
        String photoUrl = searchByName(name);
        if (photoUrl != null) {
            log.debug("Photo trouvée par nom pour : {}", name);
            return photoUrl;
        }

        // Étape 2 : fallback par GPS
        photoUrl = searchByGps(lat, lng);
        if (photoUrl != null) {
            log.debug("Photo trouvée par GPS pour : {}", name);
            return photoUrl;
        }

        log.debug("Aucune photo trouvée pour : {}", name);
        return null;
    }

    // ── Méthodes privées ──────────────────────────────────────────

    /**
     * Recherche une image par nom du commerce
     * Endpoint : action=query&list=search&srnamespace=6
     */
    private String searchByName(String name) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("list", "search")
                .queryParam("srsearch", name)
                .queryParam("srnamespace", "6")   // namespace 6 = fichiers/images
                .queryParam("srlimit", "1")
                .queryParam("format", "json")
                .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "YowYob-Crawler/1.0");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            WikimediaResponse parsed = objectMapper.readValue(
                response.getBody(), WikimediaResponse.class
            );

            if (parsed.getQuery() == null
                    || parsed.getQuery().getSearch() == null
                    || parsed.getQuery().getSearch().isEmpty()) {
                return null;
            }

            String title = parsed.getQuery().getSearch().get(0).getTitle();
            return fetchImageUrl(title);

        } catch (Exception e) {
            log.warn("Erreur recherche Wikimedia par nom '{}' : {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * Recherche une image par coordonnées GPS
     * Endpoint : action=query&generator=geosearch
     */
    private String searchByGps(double lat, double lng) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("generator", "geosearch")
                .queryParam("ggscoord", lat + "|" + lng)
                .queryParam("ggsradius", "100")    // rayon 100 mètres
                .queryParam("ggslimit", "1")
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("format", "json")
                .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "YowYob-Crawler/1.0");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            WikimediaResponse parsed = objectMapper.readValue(
                response.getBody(), WikimediaResponse.class
            );

            if (parsed.getQuery() == null
                    || parsed.getQuery().getPages() == null
                    || parsed.getQuery().getPages().isEmpty()) {
                return null;
            }

            // Prend la première page trouvée et vérifie le type d'image
            return parsed.getQuery().getPages().values().stream()
                .filter(p -> p.getImageinfo() != null && !p.getImageinfo().isEmpty())
                .map(p -> p.getImageinfo().get(0).getUrl())
                .filter(this::isValidImageUrl)
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            log.warn("Erreur recherche Wikimedia par GPS ({},{}) : {}",
                lat, lng, e.getMessage());
            return null;
        }
    }

    /**
     * Récupère l'URL directe d'une image à partir de son titre Wikimedia
     * Endpoint : action=query&prop=imageinfo&iiprop=url
     */
    private String fetchImageUrl(String title) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("titles", title)
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("format", "json")
                .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "YowYob-Crawler/1.0");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            WikimediaResponse parsed = objectMapper.readValue(
                response.getBody(), WikimediaResponse.class
            );

            if (parsed.getQuery() == null
                    || parsed.getQuery().getPages() == null) {
                return null;
            }

            return parsed.getQuery().getPages().values().stream()
                .filter(p -> p.getImageinfo() != null && !p.getImageinfo().isEmpty())
                .map(p -> p.getImageinfo().get(0).getUrl())
                .filter(this::isValidImageUrl)
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            log.warn("Erreur fetchImageUrl pour '{}' : {}", title, e.getMessage());
            return null;
        }
    }

    // Filtre les URLs non-images
    private boolean isValidImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".jpg")
            || lower.endsWith(".jpeg")
            || lower.endsWith(".png")
            || lower.endsWith(".webp");
    }
}
