package com.swifttrack.dto.driverDto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.enums.DriverOnlineStatus;

public record GetTenantDrivers(
        UUID driverId,
        String name,
        String email,
        String phone,
        String vehicleType,
        String vehicleNumber,
        String driverLicensNumber,
        DriverOnlineStatus status,
        LocalDateTime lastSeenAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
