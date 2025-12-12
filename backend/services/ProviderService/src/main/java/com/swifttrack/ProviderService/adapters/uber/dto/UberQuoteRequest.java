package com.swifttrack.ProviderService.adapters.uber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UberQuoteRequest {
    
    @JsonProperty("dropoff_address")
    private String dropoffAddress;
    
    @JsonProperty("pickup_address")
    private String pickupAddress;
    
    @JsonProperty("pickup_latitude")
    private Double pickupLatitude;
    
    @JsonProperty("pickup_longitude")
    private Double pickupLongitude;
    
    @JsonProperty("dropoff_latitude")
    private Double dropoffLatitude;
    
    @JsonProperty("dropoff_longitude")
    private Double dropoffLongitude;
    
    @JsonProperty("pickup_ready_dt")
    private String pickupReadyDt;
    
    @JsonProperty("pickup_deadline_dt")
    private String pickupDeadlineDt;
    
    @JsonProperty("dropoff_ready_dt")
    private String dropoffReadyDt;
    
    @JsonProperty("dropoff_deadline_dt")
    private String dropoffDeadlineDt;
    
    @JsonProperty("pickup_phone_number")
    private String pickupPhoneNumber;
    
    @JsonProperty("dropoff_phone_number")
    private String dropoffPhoneNumber;
    
    @JsonProperty("manifest_total_value")
    private Integer manifestTotalValue;
    
    @JsonProperty("external_store_id")
    private String externalStoreId;
}
