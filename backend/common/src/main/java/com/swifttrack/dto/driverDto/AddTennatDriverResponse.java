package com.swifttrack.dto.driverDto;

import java.util.UUID;

public record AddTennatDriverResponse(
        UUID userId,
        UUID tenantId,
        String message) {

}
