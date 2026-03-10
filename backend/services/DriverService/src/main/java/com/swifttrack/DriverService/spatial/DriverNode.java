package com.swifttrack.DriverService.spatial;

import com.swifttrack.DriverService.enums.DriverType;

public final class DriverNode {
    private final String driverId;
    private final double latitude;
    private final double longitude;
    private final String tenantId;
    private final DriverType driverType;

    private DriverNode left;
    private DriverNode right;

    public DriverNode(String driverId, double latitude, double longitude,
            String tenantId, DriverType driverType) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tenantId = tenantId;
        this.driverType = driverType;
    }

    public String getDriverId() {
        return driverId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTenantId() {
        return tenantId;
    }

    public DriverType getDriverType() {
        return driverType;
    }

    public DriverNode getLeft() {
        return left;
    }

    public void setLeft(DriverNode left) {
        this.left = left;
    }

    public DriverNode getRight() {
        return right;
    }

    public void setRight(DriverNode right) {
        this.right = right;
    }
}
