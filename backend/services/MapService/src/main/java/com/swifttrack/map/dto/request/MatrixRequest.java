package com.swifttrack.map.dto.request;

import com.swifttrack.map.dto.Coordinates;
import com.swifttrack.map.dto.TravelMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Distance Matrix API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixRequest {
    
    @NotEmpty(message = "At least one origin is required")
    @Size(max = 50, message = "Maximum 50 origins allowed")
    @Valid
    private List<Coordinates> origins;
    
    @NotEmpty(message = "At least one destination is required")
    @Size(max = 50, message = "Maximum 50 destinations allowed")
    @Valid
    private List<Coordinates> destinations;
    
    /**
     * Travel mode (defaults to DRIVING)
     */
    @Builder.Default
    private TravelMode mode = TravelMode.DRIVING;
    
    /**
     * Whether to return distance values
     */
    @Builder.Default
    private Boolean includeDistance = true;
    
    /**
     * Whether to return duration values
     */
    @Builder.Default
    private Boolean includeDuration = true;
}
