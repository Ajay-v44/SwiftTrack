package com.swifttrack.ProviderService.adapters.porter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PorterCreateOrderResponse {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("status")
    private String status;
}
