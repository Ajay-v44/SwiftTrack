package com.swifttrack.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a point snapped to the road network
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnappedPoint implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Original coordinates before snapping
     */
    @JsonProperty("original_location")
    private Coordinates originalLocation;
    
    /**
     * Snapped coordinates on road
     */
    @JsonProperty("snapped_location")
    private Coordinates snappedLocation;
    
    /**
     * Distance from original to snapped point in meters
     */
    @JsonProperty("snap_distance_meters")
    private Double snapDistanceMeters;
    
    /**
     * Index of the original point
     */
    @JsonProperty("original_index")
    private Integer originalIndex;
    
    /**
     * Whether the snap was successful
     */
    @JsonProperty("is_snapped")
    private Boolean isSnapped;
    
    /**
     * Street/Road name at snapped location
     */
    @JsonProperty("street_name")
    private String streetName;
    
    /**
     * Road reference/number
     */
    @JsonProperty("road_ref")
    private String roadRef;
    
    /**
     * Waypoint index in OSRM response
     */
    @JsonProperty("waypoint_index")
    private Integer waypointIndex;
    
    /**
     * Bearing at snapped point (degrees from north)
     */
    private Double bearing;
    
    /**
     * Confidence of the snap (0-1)
     */
    private Double confidence;
    
    /**
     * Check if the point was snapped within acceptable distance
     */
    public boolean isWithinThreshold(double thresholdMeters) {
        return isSnapped != null && isSnapped && 
               snapDistanceMeters != null && 
               snapDistanceMeters <= thresholdMeters;
    }
}
