package com.swifttrack.ProviderService.adapters.porter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PorterGetQuoteInp {
    @JsonProperty("pickup_details")
    private PickupDetails pickupDetails;
    @JsonProperty("drop_details")
    private DropDetails dropDetails;
    @JsonProperty("customer")
    private Customer customer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropDetails {
        @JsonProperty("lat")
        private double lat;
        @JsonProperty("lng")
        private double lng;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        @JsonProperty("name")
        private String name;
        @JsonProperty("mobile")
        private Mobile mobile;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Mobile {
            @JsonProperty("country_code")
            private String country_code;
            @JsonProperty("number")
            private String number;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickupDetails {
        @JsonProperty("lat")
        private double lat;
        @JsonProperty("lng")
        private double lng;
    }
}