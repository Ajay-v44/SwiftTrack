package com.swifttrack.map.dto.request;

import com.swifttrack.map.dto.Coordinates;
import com.swifttrack.map.dto.TravelMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Directions API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectionsRequest {
    
    @NotNull(message = "Origin is required")
    @Valid
    private Coordinates origin;
    
    @NotNull(message = "Destination is required")
    @Valid
    private Coordinates destination;
    
    /**
     * Travel mode (defaults to DRIVING)
     */
    @Builder.Default
    private TravelMode mode = TravelMode.DRIVING;
    
    /**
     * Whether to include alternative routes
     */
    @Builder.Default
    private Boolean alternatives = false;
    
    /**
     * Whether to include step-by-step instructions
     */
    @Builder.Default
    private Boolean steps = true;
    
    /**
     * Whether to include the full geometry
     */
    @Builder.Default
    private Boolean geometry = true;
    
    /**
     * Geometry format: "polyline" (encoded) or "geojson"
     */
    @Builder.Default
    private String geometryFormat = "polyline";
    
    /**
     * Language for instructions (ISO 639-1 code)
     */
    @Builder.Default
    private String language = "en";
}
