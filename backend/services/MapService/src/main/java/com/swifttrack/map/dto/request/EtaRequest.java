package com.swifttrack.map.dto.request;

import com.swifttrack.map.dto.Coordinates;
import com.swifttrack.map.dto.TravelMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for ETA API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtaRequest {
    
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
     * Departure time (defaults to now)
     */
    private Instant departureTime;
    
    /**
     * Provider ID for delivery mode ETA
     */
    private String providerId;
    
    /**
     * Whether to include pickup time in ETA calculation
     */
    @Builder.Default
    private Boolean includePickupTime = false;
    
    /**
     * Estimated pickup time in seconds (if known)
     */
    private Long pickupTimeSeconds;
}
