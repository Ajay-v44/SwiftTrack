package com.swifttrack.AIDispatchService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema class for structured LLM output.
 * 
 * Used by Spring AI's BeanOutputConverter to:
 * 1. Generate JSON schema sent embedded in the prompt
 * 2. Parse the LLM response directly into this record
 * 
 * The LLM must return exactly:
 * {
 *   "driver_id": "uuid-string",
 *   "confidence": 0.0-1.0,
 *   "reason": "short explanation"
 * }
 */
public record LlmDecision(
        @JsonProperty(required = true, value = "driver_id") String driverId,
        @JsonProperty(required = true, value = "confidence") double confidence,
        @JsonProperty(required = true, value = "reason") String reason) {
}
