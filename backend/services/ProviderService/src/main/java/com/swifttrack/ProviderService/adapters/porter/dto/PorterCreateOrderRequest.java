package com.swifttrack.ProviderService.adapters.porter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PorterCreateOrderRequest {
    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("delivery_instructions")
    private DeliveryInstructions deliveryInstructions;

    @JsonProperty("pickup_details")
    private LocationDetails pickupDetails;

    @JsonProperty("drop_details")
    private LocationDetails dropDetails;

    @JsonProperty("additional_comments")
    private String additionalComments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInstructions {
        @JsonProperty("instructions_list")
        private List<Instruction> instructionsList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Instruction {
        @JsonProperty("type")
        private String type;
        @JsonProperty("description")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDetails {
        @JsonProperty("address")
        private Address address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @JsonProperty("apartment_address")
        private String apartmentAddress;
        @JsonProperty("street_address1")
        private String streetAddress1;
        @JsonProperty("lat")
        private double lat;
        @JsonProperty("lng")
        private double lng;
        @JsonProperty("contact_details")
        private ContactDetails contactDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactDetails {
        @JsonProperty("name")
        private String name;
        @JsonProperty("phone_number")
        private String phoneNumber;
    }
}
