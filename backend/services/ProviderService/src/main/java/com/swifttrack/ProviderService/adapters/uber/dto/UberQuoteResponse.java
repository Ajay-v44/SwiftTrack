package com.swifttrack.ProviderService.adapters.uber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UberQuoteResponse {
    
    @JsonProperty("kind")
    private String kind;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("created")
    private String created;
    
    @JsonProperty("expires")
    private String expires;
    
    @JsonProperty("fee")
    private Long fee;  // Fee in paise (1 rupee = 100 paise)
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("currency_type")
    private String currencyType;
    
    @JsonProperty("dropoff_eta")
    private String dropoffEta;
    
    @JsonProperty("duration")
    private Integer duration;  // Duration in seconds
    
    @JsonProperty("pickup_duration")
    private Integer pickupDuration;  // Pickup duration in seconds
    
    @JsonProperty("external_store_id")
    private String externalStoreId;
    
    @JsonProperty("dropoff_deadline")
    private String dropoffDeadline;
    
    /**
     * Convert fee from paise to rupees
     */
    public float getFeeInRupees() {
        if (fee == null) {
            return 0f;
        }
        return fee / 100f;
    }
}
