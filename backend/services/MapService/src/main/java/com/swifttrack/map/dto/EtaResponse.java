package com.swifttrack.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Response DTO for ETA (Estimated Time of Arrival) API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtaResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Status of the ETA calculation
     */
    private EtaStatus status;
    
    /**
     * Error message if any
     */
    @JsonProperty("error_message")
    private String errorMessage;
    
    /**
     * Origin location
     */
    private Coordinates origin;
    
    /**
     * Destination location
     */
    private Coordinates destination;
    
    /**
     * Travel mode
     */
    @JsonProperty("travel_mode")
    private TravelMode travelMode;
    
    /**
     * Estimated duration in seconds
     */
    @JsonProperty("duration_seconds")
    private Long durationSeconds;
    
    /**
     * Duration display text
     */
    @JsonProperty("duration_text")
    private String durationText;
    
    /**
     * Distance in meters
     */
    @JsonProperty("distance_meters")
    private Double distanceMeters;
    
    /**
     * Distance display text
     */
    @JsonProperty("distance_text")
    private String distanceText;
    
    /**
     * Departure time (if provided)
     */
    @JsonProperty("departure_time")
    private Instant departureTime;
    
    /**
     * Estimated arrival time
     */
    @JsonProperty("estimated_arrival_time")
    private Instant estimatedArrivalTime;
    
    /**
     * Traffic condition
     */
    @JsonProperty("traffic_condition")
    private TrafficCondition trafficCondition;
    
    /**
     * Confidence level (0-1)
     */
    private Double confidence;
    
    /**
     * ETA range - minimum seconds
     */
    @JsonProperty("eta_min_seconds")
    private Long etaMinSeconds;
    
    /**
     * ETA range - maximum seconds
     */
    @JsonProperty("eta_max_seconds")
    private Long etaMaxSeconds;
    
    /**
     * Provider specific data (for delivery mode)
     */
    @JsonProperty("provider_id")
    private String providerId;
    
    /**
     * Whether pickup time is included
     */
    @JsonProperty("includes_pickup_time")
    private Boolean includesPickupTime;
    
    /**
     * Estimated pickup time in seconds (for delivery)
     */
    @JsonProperty("pickup_time_seconds")
    private Long pickupTimeSeconds;
    
    /**
     * Format ETA as human readable string
     */
    public String getFormattedEta() {
        if (durationSeconds == null) return "Unknown";
        
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        }
        return String.format("%d min", minutes);
    }
    
    /**
     * Get ETA with range
     */
    public String getFormattedEtaRange() {
        if (etaMinSeconds == null || etaMaxSeconds == null) {
            return getFormattedEta();
        }
        
        long minMinutes = etaMinSeconds / 60;
        long maxMinutes = etaMaxSeconds / 60;
        
        return String.format("%d - %d min", minMinutes, maxMinutes);
    }
    
    /**
     * ETA calculation status
     */
    public enum EtaStatus {
        OK,
        NOT_FOUND,
        TOO_FAR,
        INVALID_REQUEST,
        SERVICE_UNAVAILABLE,
        UNKNOWN_ERROR
    }
    
    /**
     * Traffic conditions
     */
    public enum TrafficCondition {
        LIGHT,
        NORMAL,
        HEAVY,
        VERY_HEAVY,
        UNKNOWN
    }
}
