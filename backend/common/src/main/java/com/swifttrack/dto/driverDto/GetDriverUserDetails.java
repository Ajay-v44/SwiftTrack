package com.swifttrack.dto.driverDto;

import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.DriverOnlineStatus;
import com.swifttrack.enums.VehicleType;

public record GetDriverUserDetails(

        TokenResponse user,
        VehicleType vehicleType,
        String vehicleNumber,
        String driverLicenseNumber,
        DriverOnlineStatus status) {

}
