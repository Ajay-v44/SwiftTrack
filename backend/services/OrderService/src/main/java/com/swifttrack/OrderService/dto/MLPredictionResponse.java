package com.swifttrack.OrderService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MLPredictionResponse {
    @JsonProperty("predictions")
    private List<Prediction> predictions;

    @JsonProperty("model_version")
    private String modelVersion;

    @Data
    public static class Prediction {
        @JsonProperty("provider")
        private String provider;

        @JsonProperty("success_probability")
        private double successProbability;
    }
}
