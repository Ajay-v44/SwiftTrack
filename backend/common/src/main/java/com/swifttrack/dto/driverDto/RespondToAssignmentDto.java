package com.swifttrack.dto.driverDto;

import java.util.UUID;

public record RespondToAssignmentDto(
                UUID orderId,
                boolean accept,
                String reason) {
}
