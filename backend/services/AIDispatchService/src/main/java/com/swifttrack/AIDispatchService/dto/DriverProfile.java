package com.swifttrack.AIDispatchService.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured driver profile assembled from Supabase queries.
 * This is the application-layer data representation — never exposed to LLM as raw SQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {

    private UUID driverId;
    private BigDecimal distance;
    private double acceptanceRate;
    private double cancellationRate;
    private double slaAdherence;
    private double rating;
    private long idleTimeMinutes;
}
