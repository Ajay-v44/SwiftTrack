package com.swifttrack.dto.driverDto;

import java.util.UUID;

import com.swifttrack.enums.VehicleType;

public record GetAllDriverUser(
        UUID id,
        String name,
        String email,
        String mobile,
        VehicleType vehicleType,
        String vehicleNumber,
        String driverLicenseNumber) {

}
