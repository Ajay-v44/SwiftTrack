package com.swifttrack.dto.driverDto;

import com.swifttrack.enums.DriverOnlineStatus;

public record UpdateDriverStatusRequest(
                DriverOnlineStatus status) {
}
