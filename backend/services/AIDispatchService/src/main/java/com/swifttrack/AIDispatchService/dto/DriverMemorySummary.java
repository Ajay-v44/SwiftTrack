package com.swifttrack.AIDispatchService.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single memory summary retrieved via pgvector similarity search.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverMemorySummary {

    private UUID memoryId;
    private UUID driverId;
    private String summary;
    private double similarityScore;
}
