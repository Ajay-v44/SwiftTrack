package com.swifttrack.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response DTO for Snap-to-Road API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapToRoadResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Status of the snap operation
     */
    private SnapStatus status;
    
    /**
     * Error message if any
     */
    @JsonProperty("error_message")
    private String errorMessage;
    
    /**
     * Original input points
     */
    @JsonProperty("original_points")
    private List<Coordinates> originalPoints;
    
    /**
     * Snapped points on the road network
     */
    @JsonProperty("snapped_points")
    private List<SnappedPoint> snappedPoints;
    
    /**
     * Encoded polyline of snapped path
     */
    @JsonProperty("encoded_polyline")
    private String encodedPolyline;
    
    /**
     * Snap status
     */
    public enum SnapStatus {
        OK,
        PARTIAL,
        NOT_FOUND,
        INVALID_REQUEST,
        SERVICE_UNAVAILABLE,
        UNKNOWN_ERROR
    }
}
