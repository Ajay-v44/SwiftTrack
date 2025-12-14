package com.swifttrack.map.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swifttrack.map.dto.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Serviceability/Area Check API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaCheckResponse {
    
    /**
     * The point that was checked
     */
    private Coordinates point;
    
    /**
     * Whether the point is inside the polygon
     */
    @JsonProperty("is_inside")
    private boolean isInside;
    
    /**
     * Service availability status
     */
    @JsonProperty("is_serviceable")
    private boolean isServiceable;
    
    /**
     * Distance to nearest polygon edge in meters (if outside)
     */
    @JsonProperty("distance_to_boundary_meters")
    private Double distanceToBoundaryMeters;
    
    /**
     * Message about serviceability
     */
    private String message;
}
