package com.swifttrack.map.util;

import com.swifttrack.map.dto.Coordinates;
import lombok.experimental.UtilityClass;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for generating cache keys
 */
@UtilityClass
public class CacheKeyGenerator {
    
    private static final String KEY_SEPARATOR = ":";
    private static final int COORDINATE_PRECISION = 5; // 5 decimal places
    
    /**
     * Generate cache key for geocoding
     */
    public String forGeocode(String query) {
        return "geocode" + KEY_SEPARATOR + hash(normalize(query));
    }
    
    /**
     * Generate cache key for reverse geocoding
     */
    public String forReverseGeocode(double latitude, double longitude) {
        return "reverse" + KEY_SEPARATOR + 
               roundCoordinate(latitude) + KEY_SEPARATOR + 
               roundCoordinate(longitude);
    }
    
    /**
     * Generate cache key for routing
     */
    public String forRoute(Coordinates origin, Coordinates destination, String mode) {
        return "route" + KEY_SEPARATOR +
               roundCoordinate(origin.getLatitude()) + KEY_SEPARATOR +
               roundCoordinate(origin.getLongitude()) + KEY_SEPARATOR +
               roundCoordinate(destination.getLatitude()) + KEY_SEPARATOR +
               roundCoordinate(destination.getLongitude()) + KEY_SEPARATOR +
               mode.toLowerCase();
    }
    
    /**
     * Generate cache key for distance matrix
     */
    public String forMatrix(List<Coordinates> origins, List<Coordinates> destinations, String mode) {
        String originsHash = hashCoordinates(origins);
        String destinationsHash = hashCoordinates(destinations);
        
        return "matrix" + KEY_SEPARATOR + originsHash + KEY_SEPARATOR + destinationsHash + KEY_SEPARATOR + mode.toLowerCase();
    }
    
    /**
     * Generate cache key for ETA
     */
    public String forEta(Coordinates origin, Coordinates destination, String mode) {
        return "eta" + KEY_SEPARATOR +
               roundCoordinate(origin.getLatitude()) + KEY_SEPARATOR +
               roundCoordinate(origin.getLongitude()) + KEY_SEPARATOR +
               roundCoordinate(destination.getLatitude()) + KEY_SEPARATOR +
               roundCoordinate(destination.getLongitude()) + KEY_SEPARATOR +
               mode.toLowerCase();
    }
    
    /**
     * Generate cache key for snap-to-road
     */
    public String forSnap(List<Coordinates> path) {
        return "snap" + KEY_SEPARATOR + hashCoordinates(path);
    }
    
    /**
     * Round coordinate to specified precision
     */
    private double roundCoordinate(double value) {
        double multiplier = Math.pow(10, COORDINATE_PRECISION);
        return Math.round(value * multiplier) / multiplier;
    }
    
    /**
     * Normalize query string for consistent caching
     */
    private String normalize(String query) {
        if (query == null) return "";
        return query.toLowerCase().trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Generate MD5 hash of string
     */
    private String hash(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Hash a list of coordinates
     */
    private String hashCoordinates(List<Coordinates> coords) {
        if (coords == null || coords.isEmpty()) {
            return "empty";
        }
        
        String coordString = coords.stream()
            .map(c -> roundCoordinate(c.getLatitude()) + "," + roundCoordinate(c.getLongitude()))
            .collect(Collectors.joining(";"));
        
        return hash(coordString);
    }
}
