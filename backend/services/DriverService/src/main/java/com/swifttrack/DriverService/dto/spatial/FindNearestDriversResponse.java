package com.swifttrack.DriverService.dto.spatial;

import java.util.List;

public record FindNearestDriversResponse(List<String> candidateDrivers) {
}
