package com.swifttrack.map.service;

import com.swifttrack.map.client.NominatimClient;
import com.swifttrack.map.config.CacheConfig;
import com.swifttrack.map.dto.Coordinates;
import com.swifttrack.map.dto.NormalizedLocation;
import com.swifttrack.map.exception.GeocodingException;
import com.swifttrack.map.util.CacheKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service for geocoding operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {
    
    private final NominatimClient nominatimClient;
    
    /**
     * Forward geocoding - convert address to coordinates
     * Results are cached for 24 hours
     */
    @Cacheable(value = CacheConfig.GEOCODE_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forGeocode(#query)")
    public List<NormalizedLocation> search(String query) {
        log.info("Geocoding search: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            throw new GeocodingException("Search query cannot be empty");
        }
        
        return nominatimClient.search(query.trim())
            .doOnSuccess(results -> log.debug("Found {} results for: {}", results.size(), query))
            .block();
    }
    
    /**
     * Forward geocoding with limit
     */
    @Cacheable(value = CacheConfig.GEOCODE_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forGeocode(#query + ':' + #limit)")
    public List<NormalizedLocation> search(String query, int limit) {
        log.info("Geocoding search: {} (limit={})", query, limit);
        
        if (query == null || query.trim().isEmpty()) {
            throw new GeocodingException("Search query cannot be empty");
        }
        
        if (limit < 1 || limit > 50) {
            limit = 5;
        }
        
        return nominatimClient.search(query.trim(), limit)
            .doOnSuccess(results -> log.debug("Found {} results for: {}", results.size(), query))
            .block();
    }
    
    /**
     * Reverse geocoding - convert coordinates to address
     * Results are cached for 24 hours
     */
    @Cacheable(value = CacheConfig.REVERSE_GEOCODE_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forReverseGeocode(#latitude, #longitude)")
    public NormalizedLocation reverseGeocode(double latitude, double longitude) {
        log.info("Reverse geocoding: {}, {}", latitude, longitude);
        
        validateCoordinates(latitude, longitude);
        
        return nominatimClient.reverse(latitude, longitude)
            .doOnSuccess(result -> log.debug("Reverse geocoded to: {}", result.getFormattedAddress()))
            .block();
    }
    
    /**
     * Reverse geocoding from Coordinates object
     */
    public NormalizedLocation reverseGeocode(Coordinates coordinates) {
        if (coordinates == null) {
            throw new GeocodingException("Coordinates cannot be null");
        }
        return reverseGeocode(coordinates.getLatitude(), coordinates.getLongitude());
    }
    
    /**
     * Async forward geocoding
     */
    public Mono<List<NormalizedLocation>> searchAsync(String query) {
        log.debug("Async geocoding search: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return Mono.error(new GeocodingException("Search query cannot be empty"));
        }
        
        return nominatimClient.search(query.trim());
    }
    
    /**
     * Async reverse geocoding
     */
    public Mono<NormalizedLocation> reverseGeocodeAsync(double latitude, double longitude) {
        log.debug("Async reverse geocoding: {}, {}", latitude, longitude);
        
        try {
            validateCoordinates(latitude, longitude);
        } catch (GeocodingException e) {
            return Mono.error(e);
        }
        
        return nominatimClient.reverse(latitude, longitude);
    }
    
    /**
     * Get the best match from search results
     */
    public NormalizedLocation searchBest(String query) {
        List<NormalizedLocation> results = search(query, 1);
        if (results == null || results.isEmpty()) {
            throw new GeocodingException("No results found for: " + query);
        }
        return results.get(0);
    }
    
    /**
     * Validate coordinates
     */
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new GeocodingException("Invalid latitude: " + latitude + ". Must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new GeocodingException("Invalid longitude: " + longitude + ". Must be between -180 and 180.");
        }
    }
}
