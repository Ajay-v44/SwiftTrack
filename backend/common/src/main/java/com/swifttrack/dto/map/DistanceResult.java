package com.swifttrack.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Result of a straight-line distance calculation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Coordinates origin;
    private Coordinates destination;

    @JsonProperty("distance_meters")
    private double distanceMeters;

    @JsonProperty("distance_text")
    private String distanceText;
}
