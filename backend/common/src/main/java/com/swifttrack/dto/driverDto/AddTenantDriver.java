package com.swifttrack.dto.driverDto;

import com.swifttrack.enums.UserType;
import com.swifttrack.enums.VehicleType;

public record AddTenantDriver(
        String name,
        String password,
        String email,
        String mobile,
        UserType userType,
        Boolean status,
        VehicleType vehicleType,
        String vehicleNumber,
        String driverLicensNumber) {

}
