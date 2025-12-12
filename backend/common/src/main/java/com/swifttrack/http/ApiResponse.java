package com.swifttrack.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Generic wrapper for API responses from external services.
 * Encapsulates the response body, status code, and headers.
 *
 * @param <T> The type of the response body
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * The response body deserialized to the specified type.
     */
    private T data;

    /**
     * HTTP status code of the response.
     */
    private int statusCode;

    /**
     * HTTP status code object for more detailed status info.
     */
    private HttpStatusCode httpStatus;

    /**
     * Response headers.
     */
    private HttpHeaders headers;

    /**
     * Indicates if the request was successful (2xx status).
     */
    private boolean success;

    /**
     * Error message if the request failed.
     */
    private String errorMessage;

    /**
     * Check if the response indicates success (2xx status code).
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Check if the response indicates a client error (4xx status code).
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Check if the response indicates a server error (5xx status code).
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    /**
     * Factory method to create a successful response.
     */
    public static <T> ApiResponse<T> success(T data, int statusCode, HttpHeaders headers) {
        return ApiResponse.<T>builder()
                .data(data)
                .statusCode(statusCode)
                .headers(headers)
                .success(true)
                .build();
    }

    /**
     * Factory method to create an error response.
     */
    public static <T> ApiResponse<T> error(String errorMessage, int statusCode) {
        return ApiResponse.<T>builder()
                .errorMessage(errorMessage)
                .statusCode(statusCode)
                .success(false)
                .build();
    }
}
