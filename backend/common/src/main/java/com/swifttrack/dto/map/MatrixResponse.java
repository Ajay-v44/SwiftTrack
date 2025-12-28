package com.swifttrack.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response DTO for Distance Matrix API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Status of the matrix calculation
     */
    private MatrixStatus status;

    /**
     * Error message if any
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * Origin locations
     */
    private List<Coordinates> origins;

    /**
     * Destination locations
     */
    private List<Coordinates> destinations;

    /**
     * Travel mode used
     */
    @JsonProperty("travel_mode")
    private TravelMode travelMode;

    /**
     * Matrix of distances (meters) - rows are origins, columns are destinations
     */
    private List<List<Double>> distances;

    /**
     * Matrix of durations (seconds) - rows are origins, columns are destinations
     */
    private List<List<Double>> durations;

    /**
     * Distance display texts matrix
     */
    @JsonProperty("distance_texts")
    private List<List<String>> distanceTexts;

    /**
     * Duration display texts matrix
     */
    @JsonProperty("duration_texts")
    private List<List<String>> durationTexts;

    /**
     * Individual element statuses
     */
    @JsonProperty("element_statuses")
    private List<List<ElementStatus>> elementStatuses;

    /**
     * Get distance between specific origin and destination
     */
    public Double getDistance(int originIndex, int destinationIndex) {
        if (distances == null || distances.isEmpty())
            return null;
        if (originIndex >= distances.size())
            return null;
        if (destinationIndex >= distances.get(originIndex).size())
            return null;
        return distances.get(originIndex).get(destinationIndex);
    }

    /**
     * Get duration between specific origin and destination
     */
    public Double getDuration(int originIndex, int destinationIndex) {
        if (durations == null || durations.isEmpty())
            return null;
        if (originIndex >= durations.size())
            return null;
        if (destinationIndex >= durations.get(originIndex).size())
            return null;
        return durations.get(originIndex).get(destinationIndex);
    }

    /**
     * Get the closest destination from an origin
     */
    public Integer getClosestDestination(int originIndex) {
        if (distances == null || originIndex >= distances.size())
            return null;

        List<Double> row = distances.get(originIndex);
        int minIndex = -1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < row.size(); i++) {
            Double distance = row.get(i);
            if (distance != null && distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }

        return minIndex >= 0 ? minIndex : null;
    }

    /**
     * Get the fastest destination from an origin
     */
    public Integer getFastestDestination(int originIndex) {
        if (durations == null || originIndex >= durations.size())
            return null;

        List<Double> row = durations.get(originIndex);
        int minIndex = -1;
        double minDuration = Double.MAX_VALUE;

        for (int i = 0; i < row.size(); i++) {
            Double duration = row.get(i);
            if (duration != null && duration < minDuration) {
                minDuration = duration;
                minIndex = i;
            }
        }

        return minIndex >= 0 ? minIndex : null;
    }

    /**
     * Matrix calculation status
     */
    public enum MatrixStatus {
        OK,
        INVALID_REQUEST,
        MAX_ELEMENTS_EXCEEDED,
        OVER_QUERY_LIMIT,
        SERVICE_UNAVAILABLE,
        UNKNOWN_ERROR
    }

    /**
     * Individual element status
     */
    public enum ElementStatus {
        OK,
        NOT_FOUND,
        ZERO_RESULTS,
        TOO_FAR
    }
}
