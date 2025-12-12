package com.swifttrack.ProviderService.adapters.uber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UberQuoteResponse {
    
    @JsonProperty("quotes")
    private List<Quote> quotes;
    
    @JsonProperty("pickup_address")
    private String pickupAddress;
    
    @JsonProperty("dropoff_address")
    private String dropoffAddress;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quote {
        
        @JsonProperty("quote_id")
        private String quoteId;
        
        @JsonProperty("expires_at")
        private String expiresAt;
        
        @JsonProperty("pickup_estimate")
        private String pickupEstimate;
        
        @JsonProperty("dropoff_estimate")
        private String dropoffEstimate;
        
        @JsonProperty("fare")
        private Fare fare;
        
        @JsonProperty("service_level")
        private String serviceLevel;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fare {
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("value")
        private Integer value;
    }
}
