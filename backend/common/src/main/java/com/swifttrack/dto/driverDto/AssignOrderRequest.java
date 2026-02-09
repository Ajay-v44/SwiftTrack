package com.swifttrack.dto.driverDto;

import java.util.UUID;

public record AssignOrderRequest(
                UUID driverId,
                UUID orderId) {
}
