package com.swifttrack.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Normalized address object for SwiftTrack
 * Provides a standardized address format across all services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier from the geocoding service
     */
    @JsonProperty("place_id")
    private String placeId;

    /**
     * Full formatted address
     */
    @JsonProperty("formatted_address")
    private String formattedAddress;

    /**
     * Short display name
     */
    @JsonProperty("display_name")
    private String displayName;

    /**
     * Geographical coordinates
     */
    private Coordinates coordinates;

    /**
     * Address Components
     */
    @JsonProperty("house_number")
    private String houseNumber;

    private String street;

    private String locality;

    private String sublocality;

    private String neighborhood;

    private String city;

    private String district;

    private String state;

    @JsonProperty("state_code")
    private String stateCode;

    @JsonProperty("postal_code")
    private String postalCode;

    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    /**
     * Classification
     */
    @JsonProperty("location_type")
    private LocationClassification locationType;

    /**
     * Bounding box [south, west, north, east]
     */
    @JsonProperty("bounding_box")
    private double[] boundingBox;

    /**
     * Confidence score (0-1)
     */
    private Double confidence;

    /**
     * OSM specific data
     */
    @JsonProperty("osm_id")
    private Long osmId;

    @JsonProperty("osm_type")
    private String osmType;

    /**
     * Plus code (Open Location Code)
     */
    @JsonProperty("plus_code")
    private String plusCode;

    /**
     * Get short address for display
     */
    public String getShortAddress() {
        StringBuilder sb = new StringBuilder();
        if (houseNumber != null)
            sb.append(houseNumber).append(" ");
        if (street != null)
            sb.append(street).append(", ");
        if (locality != null)
            sb.append(locality);
        else if (city != null)
            sb.append(city);
        return sb.toString().trim().replaceAll(", $", "");
    }

    /**
     * Get city-level address
     */
    public String getCityAddress() {
        StringBuilder sb = new StringBuilder();
        if (city != null)
            sb.append(city);
        if (state != null)
            sb.append(", ").append(state);
        if (country != null)
            sb.append(", ").append(country);
        return sb.toString().trim().replaceAll("^, ", "");
    }
}
