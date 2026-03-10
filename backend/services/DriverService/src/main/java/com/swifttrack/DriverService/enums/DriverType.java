package com.swifttrack.DriverService.enums;

/**
 * Differentiates between tenant-scoped drivers and platform-wide drivers.
 * <ul>
 * <li>{@code TENANT_DRIVER} – belongs to a specific tenant; invisible to other
 * tenants.</li>
 * <li>{@code PLATFORM_DRIVER} – shared pool; available to every tenant.</li>
 * </ul>
 */
public enum DriverType {
    TENANT_DRIVER,
    PLATFORM_DRIVER
}
