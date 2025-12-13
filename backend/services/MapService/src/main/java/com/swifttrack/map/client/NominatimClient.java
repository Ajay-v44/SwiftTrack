package com.swifttrack.map.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.swifttrack.map.config.MapServiceProperties;
import com.swifttrack.map.dto.*;
import com.swifttrack.map.exception.GeocodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for Nominatim OpenStreetMap Geocoding API
 * 
 * @see <a href="https://nominatim.org/release-docs/latest/api/Overview/">Nominatim API Documentation</a>
 */
@Slf4j
@Component
public class NominatimClient {
    
    private final WebClient webClient;
    private final MapServiceProperties properties;
    
    public NominatimClient(@Qualifier("nominatimWebClient") WebClient webClient,
                          MapServiceProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }
    
    /**
     * Forward geocoding - convert address to coordinates
     */
    public Mono<List<NormalizedLocation>> search(String query) {
        return search(query, 5);
    }
    
    /**
     * Forward geocoding with limit
     */
    public Mono<List<NormalizedLocation>> search(String query, int limit) {
        log.debug("Nominatim search: query={}, limit={}", query, limit);
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("q", query)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", 1)
                .queryParam("limit", limit)
                .queryParam("countrycodes", properties.getDefaults().getCountryCodes())
                .queryParam("accept-language", properties.getDefaults().getLanguage())
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(properties.getNominatim().getRetryAttempts(), Duration.ofMillis(500)))
            .map(this::parseSearchResults)
            .doOnError(e -> log.error("Nominatim search failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new GeocodingException("Failed to search address: " + e.getMessage(), e)));
    }
    
    /**
     * Reverse geocoding - convert coordinates to address
     */
    public Mono<NormalizedLocation> reverse(double latitude, double longitude) {
        log.debug("Nominatim reverse: lat={}, lng={}", latitude, longitude);
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/reverse")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", 1)
                .queryParam("zoom", 18) // Max detail level
                .queryParam("accept-language", properties.getDefaults().getLanguage())
                .build())
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(properties.getNominatim().getRetryAttempts(), Duration.ofMillis(500)))
            .map(this::parseReverseResult)
            .doOnError(e -> log.error("Nominatim reverse geocode failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new GeocodingException("Failed to reverse geocode: " + e.getMessage(), e)));
    }
    
    /**
     * Parse Nominatim search results
     */
    private List<NormalizedLocation> parseSearchResults(JsonNode response) {
        List<NormalizedLocation> results = new ArrayList<>();
        
        if (response == null || !response.isArray()) {
            return results;
        }
        
        for (JsonNode item : response) {
            try {
                results.add(parseLocationNode(item));
            } catch (Exception e) {
                log.warn("Failed to parse search result: {}", e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Parse Nominatim reverse result
     */
    private NormalizedLocation parseReverseResult(JsonNode response) {
        if (response == null || response.has("error")) {
            String error = response != null ? response.path("error").asText("Unknown error") : "Empty response";
            throw new GeocodingException("Reverse geocoding failed: " + error);
        }
        
        return parseLocationNode(response);
    }
    
    /**
     * Parse a single Nominatim result node
     */
    private NormalizedLocation parseLocationNode(JsonNode node) {
        JsonNode address = node.path("address");
        
        // Parse bounding box
        double[] boundingBox = null;
        if (node.has("boundingbox") && node.get("boundingbox").isArray()) {
            JsonNode bbox = node.get("boundingbox");
            boundingBox = new double[] {
                bbox.get(0).asDouble(), // south
                bbox.get(2).asDouble(), // west
                bbox.get(1).asDouble(), // north
                bbox.get(3).asDouble()  // east
            };
        }
        
        // Parse classification
        String osmClass = node.path("class").asText(null);
        String osmType = node.path("type").asText(null);
        LocationClassification classification = LocationClassification.fromOsmClass(osmClass, osmType);
        
        // Calculate confidence based on importance
        Double confidence = node.has("importance") ? node.get("importance").asDouble() : null;
        
        return NormalizedLocation.builder()
            .placeId(node.path("place_id").asText(null))
            .displayName(node.path("display_name").asText(null))
            .formattedAddress(node.path("display_name").asText(null))
            .coordinates(Coordinates.builder()
                .latitude(node.path("lat").asDouble())
                .longitude(node.path("lon").asDouble())
                .build())
            .houseNumber(address.path("house_number").asText(null))
            .street(getStreetName(address))
            .locality(address.path("suburb").asText(address.path("neighbourhood").asText(null)))
            .sublocality(address.path("suburb").asText(null))
            .neighborhood(address.path("neighbourhood").asText(null))
            .city(getCity(address))
            .district(address.path("county").asText(address.path("state_district").asText(null)))
            .state(address.path("state").asText(null))
            .stateCode(address.path("ISO3166-2-lvl4").asText(null))
            .postalCode(address.path("postcode").asText(null))
            .country(address.path("country").asText(null))
            .countryCode(address.path("country_code").asText(null))
            .locationType(classification)
            .boundingBox(boundingBox)
            .confidence(confidence)
            .osmId(node.path("osm_id").asLong(0))
            .osmType(node.path("osm_type").asText(null))
            .build();
    }
    
    /**
     * Get street name from address components
     */
    private String getStreetName(JsonNode address) {
        // Try various street name fields
        String[] streetFields = {"road", "street", "pedestrian", "footway", "cycleway"};
        for (String field : streetFields) {
            if (address.has(field)) {
                return address.get(field).asText();
            }
        }
        return null;
    }
    
    /**
     * Get city name from address components
     */
    private String getCity(JsonNode address) {
        // Try various city name fields in order of preference
        String[] cityFields = {"city", "town", "village", "municipality", "hamlet"};
        for (String field : cityFields) {
            if (address.has(field)) {
                return address.get(field).asText();
            }
        }
        return null;
    }
}
