package com.swifttrack.ProviderService.adapters.uber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UberCreateOrderResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("status")
    private String status;
}
