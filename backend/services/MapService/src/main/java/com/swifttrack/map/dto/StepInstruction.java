package com.swifttrack.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Step-by-step navigation instruction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepInstruction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Step index in the route
     */
    @JsonProperty("step_index")
    private Integer stepIndex;
    
    /**
     * Navigation instruction text
     */
    private String instruction;
    
    /**
     * HTML formatted instruction
     */
    @JsonProperty("html_instruction")
    private String htmlInstruction;
    
    /**
     * Maneuver type
     */
    private ManeuverType maneuver;
    
    /**
     * Maneuver modifier (e.g., "sharp right", "slight left")
     */
    @JsonProperty("maneuver_modifier")
    private String maneuverModifier;
    
    /**
     * Distance of this step in meters
     */
    @JsonProperty("distance_meters")
    private Double distanceMeters;
    
    /**
     * Distance display text
     */
    @JsonProperty("distance_text")
    private String distanceText;
    
    /**
     * Duration of this step in seconds
     */
    @JsonProperty("duration_seconds")
    private Double durationSeconds;
    
    /**
     * Duration display text
     */
    @JsonProperty("duration_text")
    private String durationText;
    
    /**
     * Start location of this step
     */
    @JsonProperty("start_location")
    private Coordinates startLocation;
    
    /**
     * End location of this step
     */
    @JsonProperty("end_location")
    private Coordinates endLocation;
    
    /**
     * Bearing before maneuver (degrees from north)
     */
    @JsonProperty("bearing_before")
    private Double bearingBefore;
    
    /**
     * Bearing after maneuver
     */
    @JsonProperty("bearing_after")
    private Double bearingAfter;
    
    /**
     * Street name
     */
    @JsonProperty("street_name")
    private String streetName;
    
    /**
     * Road reference (e.g., NH44, SH17)
     */
    @JsonProperty("road_ref")
    private String roadRef;
    
    /**
     * Speed limit in km/h (if available)
     */
    @JsonProperty("speed_limit")
    private Integer speedLimit;
    
    /**
     * Maneuver types based on OSRM
     */
    public enum ManeuverType {
        DEPART,
        ARRIVE,
        TURN,
        NEW_NAME,
        MERGE,
        ON_RAMP,
        OFF_RAMP,
        FORK,
        END_OF_ROAD,
        CONTINUE,
        ROUNDABOUT,
        ROTARY,
        ROUNDABOUT_TURN,
        NOTIFICATION,
        EXIT_ROUNDABOUT,
        EXIT_ROTARY,
        STRAIGHT,
        SLIGHT_RIGHT,
        RIGHT,
        SHARP_RIGHT,
        UTURN,
        SHARP_LEFT,
        LEFT,
        SLIGHT_LEFT,
        UNKNOWN;
        
        public static ManeuverType fromOsrm(String type, String modifier) {
            if (type == null) return UNKNOWN;
            
            try {
                // First try direct mapping
                return ManeuverType.valueOf(type.toUpperCase().replace(" ", "_").replace("-", "_"));
            } catch (IllegalArgumentException e) {
                // Handle special cases
                if ("turn".equalsIgnoreCase(type) && modifier != null) {
                    return switch (modifier.toLowerCase()) {
                        case "straight" -> STRAIGHT;
                        case "slight right" -> SLIGHT_RIGHT;
                        case "right" -> RIGHT;
                        case "sharp right" -> SHARP_RIGHT;
                        case "uturn" -> UTURN;
                        case "sharp left" -> SHARP_LEFT;
                        case "left" -> LEFT;
                        case "slight left" -> SLIGHT_LEFT;
                        default -> TURN;
                    };
                }
                return UNKNOWN;
            }
        }
    }
}
