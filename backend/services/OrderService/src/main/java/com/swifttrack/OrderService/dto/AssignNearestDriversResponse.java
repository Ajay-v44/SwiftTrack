package com.swifttrack.OrderService.dto;

import java.util.List;
import java.util.UUID;

public record AssignNearestDriversResponse(
        List<String> candidateDrivers,
        boolean assigned,
        UUID assignedDriverId) {
}
