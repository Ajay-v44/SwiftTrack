package com.swifttrack.ProviderService.adapters.porter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PorterQuoteResponse {
    @JsonProperty("vehicles")
    private List<Vehicle> vehicles;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Vehicle {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("fare")
        private Fare fare;
        
        @JsonProperty("eta")
        private Eta eta;
        
        // Additional fields that may be present
        @JsonProperty("capacity")
        private Object capacity;
        
        @JsonProperty("size")
        private Object size;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fare {
        @JsonProperty("minor_amount")
        private Integer minorAmount;
        
        @JsonProperty("currency")
        private String currency;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Eta {
        @JsonProperty("value")
        private Double value;
        
        @JsonProperty("unit")
        private String unit;
    }
}