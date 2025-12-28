package com.swifttrack.dto.map;

import lombok.Getter;

/**
 * Classification of location types based on OSM data
 */
@Getter
public enum LocationClassification {

    // Address Types
    HOUSE("house", "Specific building address"),
    BUILDING("building", "Named building"),
    STREET_ADDRESS("street_address", "Street address without specific building"),
    STREET("street", "Street/Road"),
    INTERSECTION("intersection", "Road intersection"),

    // Area Types
    NEIGHBORHOOD("neighborhood", "Local neighborhood"),
    SUBURB("suburb", "Suburb/Sublocality"),
    LOCALITY("locality", "Town/Village"),
    CITY("city", "City"),
    DISTRICT("district", "District/County"),
    STATE("state", "State/Province"),
    COUNTRY("country", "Country"),

    // POI Types
    POI("poi", "Point of Interest"),
    BUSINESS("business", "Business/Shop"),
    RESTAURANT("restaurant", "Restaurant/Cafe"),
    HOSPITAL("hospital", "Hospital/Medical"),
    SCHOOL("school", "School/Educational"),
    TRANSPORT("transport", "Transport hub"),
    LANDMARK("landmark", "Landmark"),

    // Special Types
    POSTAL_CODE("postal_code", "Postal code area"),
    APPROXIMATE("approximate", "Approximate location"),
    UNKNOWN("unknown", "Unknown classification");

    private final String code;
    private final String description;

    LocationClassification(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Parse from OSM type/class
     */
    public static LocationClassification fromOsmClass(String osmClass, String osmType) {
        if (osmClass == null)
            return UNKNOWN;

        return switch (osmClass.toLowerCase()) {
            case "building", "house" -> HOUSE;
            case "highway" -> STREET;
            case "place" -> parsePlaceType(osmType);
            case "amenity" -> parseAmenityType(osmType);
            case "shop" -> BUSINESS;
            case "tourism" -> LANDMARK;
            case "boundary" -> parseBoundaryType(osmType);
            default -> UNKNOWN;
        };
    }

    private static LocationClassification parsePlaceType(String type) {
        if (type == null)
            return LOCALITY;
        return switch (type.toLowerCase()) {
            case "house" -> HOUSE;
            case "neighbourhood", "neighborhood" -> NEIGHBORHOOD;
            case "suburb" -> SUBURB;
            case "village", "town" -> LOCALITY;
            case "city" -> CITY;
            case "county", "district" -> DISTRICT;
            case "state", "province" -> STATE;
            case "country" -> COUNTRY;
            default -> LOCALITY;
        };
    }

    private static LocationClassification parseAmenityType(String type) {
        if (type == null)
            return POI;
        return switch (type.toLowerCase()) {
            case "restaurant", "cafe", "fast_food" -> RESTAURANT;
            case "hospital", "clinic", "pharmacy" -> HOSPITAL;
            case "school", "university", "college" -> SCHOOL;
            case "bus_station", "railway", "airport" -> TRANSPORT;
            default -> POI;
        };
    }

    private static LocationClassification parseBoundaryType(String type) {
        if (type == null)
            return UNKNOWN;
        return switch (type.toLowerCase()) {
            case "administrative" -> DISTRICT;
            case "postal_code" -> POSTAL_CODE;
            default -> UNKNOWN;
        };
    }
}
