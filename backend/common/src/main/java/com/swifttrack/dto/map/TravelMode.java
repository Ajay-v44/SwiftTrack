package com.swifttrack.dto.map;

import lombok.Getter;

/**
 * Available travel/routing modes
 */
@Getter
public enum TravelMode {

    DRIVING("car", "driving", "car", "Driving by car"),
    WALKING("foot", "walking", "foot", "Walking"),
    BIKE("bike", "cycling", "bike", "Cycling/Biking"),
    DELIVERY("car", "driving", "car", "Delivery vehicle mode");

    private final String osrmProfile;
    private final String graphhopperProfile;
    private final String nominatimProfile;
    private final String description;

    TravelMode(String osrmProfile, String graphhopperProfile, String nominatimProfile, String description) {
        this.osrmProfile = osrmProfile;
        this.graphhopperProfile = graphhopperProfile;
        this.nominatimProfile = nominatimProfile;
        this.description = description;
    }

    /**
     * Parse travel mode from string (case-insensitive)
     */
    public static TravelMode fromString(String mode) {
        if (mode == null || mode.isBlank()) {
            return DRIVING; // Default
        }

        try {
            return TravelMode.valueOf(mode.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Try matching by profile
            for (TravelMode tm : values()) {
                if (tm.osrmProfile.equalsIgnoreCase(mode) ||
                        tm.graphhopperProfile.equalsIgnoreCase(mode)) {
                    return tm;
                }
            }
            return DRIVING; // Default fallback
        }
    }

    /**
     * Get average speed in meters per second for ETA estimation
     */
    public double getAverageSpeedMps() {
        return switch (this) {
            case DRIVING -> 8.33; // ~30 km/h (urban average)
            case WALKING -> 1.39; // ~5 km/h
            case BIKE -> 4.17; // ~15 km/h
            case DELIVERY -> 6.94; // ~25 km/h (slower due to stops)
        };
    }

    /**
     * Get estimated time multiplier for traffic conditions
     */
    public double getTrafficMultiplier() {
        return switch (this) {
            case DRIVING -> 1.3; // 30% buffer for traffic
            case WALKING -> 1.0; // No traffic impact
            case BIKE -> 1.1; // Slight buffer
            case DELIVERY -> 1.5; // Higher buffer for stops
        };
    }
}
