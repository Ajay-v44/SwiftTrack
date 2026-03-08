package com.swifttrack.dto.driverDto;

import com.swifttrack.enums.VehicleType;

public record AddTenantDriver(
                VehicleType vehicleType,
                String vehicleNumber,
                String driverLicensNumber) {

}
