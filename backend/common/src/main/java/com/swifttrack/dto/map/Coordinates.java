package com.swifttrack.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a geographical coordinate point (latitude, longitude)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Latitude is required")
    @JsonProperty("lat")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @JsonProperty("lng")
    private Double longitude;

    /**
     * Create coordinates from lat,lng string (e.g., "12.9716,77.5946")
     */
    public static Coordinates fromString(String latLng) {
        if (latLng == null || latLng.isBlank()) {
            return null;
        }
        String[] parts = latLng.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinates format. Expected: lat,lng");
        }
        return Coordinates.builder()
                .latitude(Double.parseDouble(parts[0].trim()))
                .longitude(Double.parseDouble(parts[1].trim()))
                .build();
    }

    /**
     * Convert to OSRM format (lng,lat)
     */
    public String toOsrmFormat() {
        return longitude + "," + latitude;
    }

    /**
     * Convert to standard format (lat,lng)
     */
    public String toStandardFormat() {
        return latitude + "," + longitude;
    }

    /**
     * Calculate distance to another point using Haversine formula
     */
    public double distanceTo(Coordinates other) {
        if (other == null)
            return 0;

        final double R = 6371000; // Earth's radius in meters
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLng = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in meters
    }

    /**
     * Check if coordinates are valid
     */
    @JsonIgnore
    public boolean isValid() {
        return latitude != null && longitude != null &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }
}
