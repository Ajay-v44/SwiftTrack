package com.swifttrack.map.dto.request;

import com.swifttrack.map.dto.Coordinates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Snap-to-Road API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapToRoadRequest {
    
    @NotEmpty(message = "At least one point is required")
    @Size(max = 100, message = "Maximum 100 points allowed")
    @Valid
    private List<Coordinates> path;
    
    /**
     * Radius around each point to search for roads (in meters)
     */
    @Builder.Default
    private Double radiusMeters = 50.0;
    
    /**
     * Whether to interpolate additional points along the snapped path
     */
    @Builder.Default
    private Boolean interpolate = false;
}
