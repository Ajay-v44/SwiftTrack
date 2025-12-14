package com.swifttrack.map.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Map Service external providers
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "map")
public class MapServiceProperties {
    
    /**
     * Nominatim configuration
     */
    private NominatimConfig nominatim = new NominatimConfig();
    
    /**
     * OSRM configuration
     */
    private OsrmConfig osrm = new OsrmConfig();
    
    /**
     * GraphHopper configuration
     */
    private GraphHopperConfig graphhopper = new GraphHopperConfig();
    
    /**
     * Selected routing engine
     */
    private String routingEngine = "osrm";
    
    /**
     * Cache TTL configuration
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * Rate limit configuration
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();
    
    /**
     * Default settings
     */
    private DefaultsConfig defaults = new DefaultsConfig();
    
    @Data
    public static class NominatimConfig {
        private String baseUrl = "https://nominatim.openstreetmap.org";
        private String localUrl = "http://localhost:8088";
        private boolean useLocal = false;
        private String userAgent = "SwiftTrack-MapService/1.0";
        private int timeout = 5000;
        private int retryAttempts = 3;
        private int rateLimitPerSecond = 1;
        
        public String getEffectiveUrl() {
            return useLocal ? localUrl : baseUrl;
        }
    }
    
    @Data
    public static class OsrmConfig {
        private String baseUrl = "https://router.project-osrm.org";
        private String localUrl = "http://localhost:5000";
        private boolean useLocal = false;
        private int timeout = 10000;
        private int retryAttempts = 3;
        private ProfilesConfig profiles = new ProfilesConfig();
        
        public String getEffectiveUrl() {
            return useLocal ? localUrl : baseUrl;
        }
    }
    
    @Data
    public static class GraphHopperConfig {
        private String baseUrl = "http://localhost:8989";
        private String apiKey = "";
        private boolean enabled = false;
        private int timeout = 10000;
        private ProfilesConfig profiles = new ProfilesConfig();
    }
    
    @Data
    public static class ProfilesConfig {
        private String driving = "car";
        private String walking = "foot";
        private String bike = "bike";
        private String delivery = "car";
    }
    
    @Data
    public static class CacheConfig {
        private long geocodeTtl = 86400;       // 24 hours
        private long reverseGeocodeTtl = 86400; // 24 hours
        private long routeTtl = 3600;           // 1 hour
        private long matrixTtl = 1800;          // 30 minutes
        private long etaTtl = 600;              // 10 minutes
    }
    
    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int requestsPerMinute = 1000;
        private int burstSize = 50;
    }
    
    @Data
    public static class DefaultsConfig {
        private String countryCodes = "in";
        private String language = "en";
        private int maxMatrixOrigins = 50;
        private int maxMatrixDestinations = 50;
        private double snapRadiusMeters = 50;
    }
}
