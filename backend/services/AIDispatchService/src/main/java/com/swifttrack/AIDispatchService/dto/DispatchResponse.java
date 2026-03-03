package com.swifttrack.AIDispatchService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dispatch response containing the selected driver.
 * Matches the exact JSON output format required from the LLM.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchResponse {

    @JsonProperty("driver_id")
    private String driverId;

    private double confidence;
    private String reason;
    private boolean fallback;

    @JsonProperty("latency_ms")
    private long latencyMs;
}
