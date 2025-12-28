package com.swifttrack.dto.map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response wrapper for Map Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Whether the request was successful
     */
    private boolean success;

    /**
     * Response data
     */
    private T data;

    /**
     * Error information (if any)
     */
    private ErrorInfo error;

    /**
     * Response metadata
     */
    private ResponseMetadata metadata;

    /**
     * Create a successful response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .metadata(ResponseMetadata.now())
                .build();
    }

    /**
     * Create a successful response with cache info
     */
    public static <T> ApiResponse<T> success(T data, boolean fromCache) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .metadata(ResponseMetadata.builder()
                        .timestamp(Instant.now())
                        .cached(fromCache)
                        .build())
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .metadata(ResponseMetadata.now())
                .build();
    }

    /**
     * Create an error response with details
     */
    public static <T> ApiResponse<T> error(String code, String message, String details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .metadata(ResponseMetadata.now())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        private String code;
        private String message;
        private String details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseMetadata {
        private Instant timestamp;

        @JsonProperty("processing_time_ms")
        private Long processingTimeMs;

        private Boolean cached;

        @JsonProperty("cache_key")
        private String cacheKey;

        public static ResponseMetadata now() {
            return ResponseMetadata.builder()
                    .timestamp(Instant.now())
                    .build();
        }
    }
}
