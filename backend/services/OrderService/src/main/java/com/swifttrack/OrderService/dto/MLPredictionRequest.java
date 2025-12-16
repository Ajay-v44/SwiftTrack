package com.swifttrack.OrderService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MLPredictionRequest {
    @JsonProperty("providers")
    private List<ModelQuoteInput> providers;
}
