package com.swifttrack.dto.driverDto;

import java.math.BigDecimal;

public record DriverLocationUpdateDto(
        BigDecimal latitude,
        BigDecimal longitude) {
}
