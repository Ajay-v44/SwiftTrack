package com.swifttrack.DriverService.dto;

import com.swifttrack.enums.VehicleType;

public record RegisterDriver(
        String name,
        String password,
        String email,
        String mobile,
        VehicleType vehicleType,
        String vehicleNumber,
        String driverLicensNumber) {

}
