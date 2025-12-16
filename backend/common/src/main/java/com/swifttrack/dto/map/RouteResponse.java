package com.swifttrack.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for routing/directions API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Route status
     */
    private RouteStatus status;

    /**
     * Error message if status is not OK
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * Origin point
     */
    private Coordinates origin;

    /**
     * Destination point
     */
    private Coordinates destination;

    /**
     * Travel mode used
     */
    @JsonProperty("travel_mode")
    private TravelMode travelMode;

    /**
     * Total distance in meters
     */
    @JsonProperty("distance_meters")
    private Double distanceMeters;

    /**
     * Total distance display text
     */
    @JsonProperty("distance_text")
    private String distanceText;

    /**
     * Total duration in seconds
     */
    @JsonProperty("duration_seconds")
    private Double durationSeconds;

    /**
     * Duration display text
     */
    @JsonProperty("duration_text")
    private String durationText;

    /**
     * Encoded polyline of the route
     */
    @JsonProperty("encoded_polyline")
    private String encodedPolyline;

    /**
     * Decoded route geometry as coordinate pairs
     */
    private List<Coordinates> geometry;

    /**
     * Turn-by-turn instructions
     */
    private List<StepInstruction> steps;

    /**
     * Route summary (major road names)
     */
    private String summary;

    /**
     * Waypoints along the route
     */
    private List<Coordinates> waypoints;

    /**
     * Estimated arrival time (if departure time is known)
     */
    @JsonProperty("estimated_arrival")
    private Instant estimatedArrival;

    /**
     * Alternative routes if available
     */
    private List<RouteResponse> alternatives;

    /**
     * Get formatted distance
     */
    public String getFormattedDistance() {
        if (distanceMeters == null)
            return "Unknown";
        if (distanceMeters < 1000) {
            return String.format("%.0f m", distanceMeters);
        }
        return String.format("%.1f km", distanceMeters / 1000);
    }

    /**
     * Get formatted duration
     */
    public String getFormattedDuration() {
        if (durationSeconds == null)
            return "Unknown";
        Duration duration = Duration.ofSeconds(durationSeconds.longValue());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        }
        return String.format("%d min", minutes);
    }

    /**
     * Route calculation status
     */
    public enum RouteStatus {
        OK,
        NOT_FOUND,
        ZERO_RESULTS,
        TOO_FAR,
        INVALID_REQUEST,
        OVER_QUERY_LIMIT,
        SERVICE_UNAVAILABLE,
        UNKNOWN_ERROR
    }
}
