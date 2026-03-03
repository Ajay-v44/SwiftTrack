package com.swifttrack.AIDispatchService.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Incoming dispatch request from the Order/Driver Service.
 * Contains the top N driver IDs pre-filtered by KD-tree proximity.
 */
public record DispatchRequest(

        @NotEmpty(message = "driverIds must not be empty")
        @Size(max = 5, message = "Maximum 5 drivers per dispatch request")
        List<UUID> driverIds

) {
}
