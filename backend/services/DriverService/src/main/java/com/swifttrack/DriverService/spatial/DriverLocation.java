package com.swifttrack.DriverService.spatial;

import com.swifttrack.DriverService.enums.DriverType;

/**
 * Immutable driver location snapshot for lock-free spatial reads.
 * Now includes tenantId and driverType for multi-tenant isolation.
 */
public record DriverLocation(
        String driverId,
        double latitude,
        double longitude,
        String tenantId,
        DriverType driverType) {

    /**
     * Returns the category key used to partition Redis GEO sets and KD-trees.
     * <ul>
     * <li>{@code "platform"} for platform-wide drivers.</li>
     * <li>{@code "tenant:{tenantId}"} for tenant-scoped drivers.</li>
     * </ul>
     */
    public String categoryKey() {
        return driverType == DriverType.PLATFORM_DRIVER
                ? "platform"
                : "tenant:" + tenantId;
    }
}
