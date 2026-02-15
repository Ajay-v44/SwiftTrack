package com.swifttrack.dto.driverDto;

import com.swifttrack.enums.DriverOnlineStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateDriverStatusRequest(
                @Schema(description = "Driver online status", allowableValues = {
                                "OFFLINE", "ONLINE", "ON_TRIP",
                                "SUSPENDED" }) DriverOnlineStatus status) {
}
