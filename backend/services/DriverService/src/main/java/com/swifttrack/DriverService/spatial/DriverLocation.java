package com.swifttrack.DriverService.spatial;

/**
 * Immutable driver location snapshot for lock-free spatial reads.
 */
public record DriverLocation(String driverId, double latitude, double longitude) {
}
