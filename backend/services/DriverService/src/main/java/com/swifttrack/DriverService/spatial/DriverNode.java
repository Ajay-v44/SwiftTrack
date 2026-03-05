package com.swifttrack.DriverService.spatial;

public final class DriverNode {
    private final String driverId;
    private final double latitude;
    private final double longitude;

    private DriverNode left;
    private DriverNode right;

    public DriverNode(String driverId, double latitude, double longitude) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
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
