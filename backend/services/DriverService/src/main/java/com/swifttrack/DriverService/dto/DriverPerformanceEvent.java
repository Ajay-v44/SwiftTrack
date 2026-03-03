package com.swifttrack.DriverService.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event emitted when a driver's performance state changes (order completed,
 * order cancelled, rating updated). Consumed asynchronously to trigger
 * memory embedding generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPerformanceEvent {

    public enum TriggerType {
        ORDER_COMPLETED,
        ORDER_CANCELLED,
        RATING_UPDATED,
        DAILY_AGGREGATION
    }

    private UUID driverId;
    private TriggerType triggerType;
}
