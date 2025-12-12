package com.swifttrack.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Utility class for making external API calls.
 * Provides convenient methods for GET, POST, PUT, PATCH, DELETE operations.
 * 
 * Features:
 * - Automatic error handling and wrapping
 * - Support for custom headers
 * - Support for query parameters
 * - Generic type handling for responses
 * - Logging for debugging
 */
@Slf4j
@Component
public class ExternalApiClient {

    private final RestTemplate restTemplate;

    public ExternalApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Performs a GET request to the specified URL.
     *
     * @param url          The target URL
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @return ApiResponse containing the result or error
     */
    public <T> ApiResponse<T> get(String url, Class<T> responseType) {
        return get(url, null, null, responseType);
    }

    /**
     * Performs a GET request with custom headers.
     *
     * @param url          The target URL
     * @param headers      Custom HTTP headers
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @return ApiResponse containing the result or error
     */
    public <T> ApiResponse<T> get(String url, HttpHeaders headers, Class<T> responseType) {
        return get(url, headers, null, responseType);
    }

    /**
     * Performs a GET request with custom headers and query parameters.
     *
     * @param url          The target URL
     * @param headers      Custom HTTP headers
     * @param queryParams  Query parameters to append to URL
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @return ApiResponse containing the result or error
     */
    public <T> ApiResponse<T> get(String url, HttpHeaders headers,
            Map<String, String> queryParams, Class<T> responseType) {
        String finalUrl = buildUrlWithParams(url, queryParams);
        HttpEntity<?> entity = createHttpEntity(null, headers);
        return executeRequest(finalUrl, HttpMethod.GET, entity, responseType);
    }

    /**
     * Performs a GET request returning a parameterized type (e.g., List<MyObject>).
     *
     * @param url          The target URL
     * @param headers      Custom HTTP headers
     * @param responseType ParameterizedTypeReference for complex types
     * @param <T>          The response type
     * @return ApiResponse containing the result or error
     */
    public <T> ApiResponse<T> get(String url, HttpHeaders headers,
            ParameterizedTypeReference<T> responseType) {
        HttpEntity<?> entity = createHttpEntity(null, headers);
        return executeRequest(url, HttpMethod.GET, entity, responseType);
    }

    /**
     * Performs a POST request with a request body.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> post(String url, R body, Class<T> responseType) {
        return post(url, body, null, responseType);
    }

    /**
     * Performs a POST request with a request body and custom headers.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param headers      Custom HTTP headers
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> post(String url, R body, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<R> entity = createHttpEntity(body, headers);
        return executeRequest(url, HttpMethod.POST, entity, responseType);
    }

    /**
     * Performs a POST request returning a parameterized type.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param headers      Custom HTTP headers
     * @param responseType ParameterizedTypeReference for complex types
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> post(String url, R body, HttpHeaders headers,
            ParameterizedTypeReference<T> responseType) {
        HttpEntity<R> entity = createHttpEntity(body, headers);
        return executeRequest(url, HttpMethod.POST, entity, responseType);
    }

    /**
     * Performs a PUT request with a request body.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> put(String url, R body, Class<T> responseType) {
        return put(url, body, null, responseType);
    }

    /**
     * Performs a PUT request with a request body and custom headers.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param headers      Custom HTTP headers
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> put(String url, R body, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<R> entity = createHttpEntity(body, headers);
        return executeRequest(url, HttpMethod.PUT, entity, responseType);
    }

    /**
     * Performs a PATCH request with a request body.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> patch(String url, R body, Class<T> responseType) {
        return patch(url, body, null, responseType);
    }

    /**
     * Performs a PATCH request with a request body and custom headers.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param headers      Custom HTTP headers
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> patch(String url, R body, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<R> entity = createHttpEntity(body, headers);
        return executeRequest(url, HttpMethod.PATCH, entity, responseType);
    }

    /**
     * Performs a DELETE request.
     *
     * @param url          The target URL
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @return ApiResponse containing the result or error
     */
    public <T> ApiResponse<T> delete(String url, Class<T> responseType) {
        return delete(url, null, responseType);
    }

    /**
     * Performs a DELETE request with custom headers.
     *
     * @param url          The target URL
     * @param headers      Custom HTTP headers
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @return ApiResponse containing the result or error
     */
    public <T> ApiResponse<T> delete(String url, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<?> entity = createHttpEntity(null, headers);
        return executeRequest(url, HttpMethod.DELETE, entity, responseType);
    }

    /**
     * Performs a DELETE request with a request body.
     *
     * @param url          The target URL
     * @param body         The request body
     * @param headers      Custom HTTP headers
     * @param responseType The expected response type class
     * @param <T>          The response type
     * @param <R>          The request body type
     * @return ApiResponse containing the result or error
     */
    public <T, R> ApiResponse<T> deleteWithBody(String url, R body, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<R> entity = createHttpEntity(body, headers);
        return executeRequest(url, HttpMethod.DELETE, entity, responseType);
    }

    // ==================== Helper Methods ====================

    /**
     * Creates an HttpEntity with the given body and headers.
     */
    private <R> HttpEntity<R> createHttpEntity(R body, HttpHeaders customHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (customHeaders != null) {
            headers.addAll(customHeaders);
        }

        return body != null ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers);
    }

    /**
     * Builds a URL with query parameters.
     */
    private String buildUrlWithParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        queryParams.forEach(builder::queryParam);
        return builder.toUriString();
    }

    /**
     * Executes the HTTP request and handles responses/errors.
     */
    private <T> ApiResponse<T> executeRequest(String url, HttpMethod method,
            HttpEntity<?> entity, Class<T> responseType) {
        log.debug("Executing {} request to: {}", method, url);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            log.debug("Response status: {} for {} {}", response.getStatusCode(), method, url);

            return ApiResponse.<T>builder()
                    .data(response.getBody())
                    .statusCode(response.getStatusCode().value())
                    .httpStatus(response.getStatusCode())
                    .headers(response.getHeaders())
                    .success(response.getStatusCode().is2xxSuccessful())
                    .build();

        } catch (HttpClientErrorException e) {
            log.warn("Client error for {} {}: {} - {}", method, url,
                    e.getStatusCode(), e.getResponseBodyAsString());
            return ApiResponse.<T>builder()
                    .statusCode(e.getStatusCode().value())
                    .httpStatus(e.getStatusCode())
                    .success(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();

        } catch (HttpServerErrorException e) {
            log.error("Server error for {} {}: {} - {}", method, url,
                    e.getStatusCode(), e.getResponseBodyAsString());
            return ApiResponse.<T>builder()
                    .statusCode(e.getStatusCode().value())
                    .httpStatus(e.getStatusCode())
                    .success(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();

        } catch (ResourceAccessException e) {
            log.error("Connection error for {} {}: {}", method, url, e.getMessage());
            return ApiResponse.<T>builder()
                    .statusCode(0)
                    .success(false)
                    .errorMessage("Connection failed: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error for {} {}: {}", method, url, e.getMessage(), e);
            return ApiResponse.<T>builder()
                    .statusCode(0)
                    .success(false)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Executes the HTTP request for parameterized types.
     */
    private <T> ApiResponse<T> executeRequest(String url, HttpMethod method,
            HttpEntity<?> entity,
            ParameterizedTypeReference<T> responseType) {
        log.debug("Executing {} request to: {}", method, url);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            log.debug("Response status: {} for {} {}", response.getStatusCode(), method, url);

            return ApiResponse.<T>builder()
                    .data(response.getBody())
                    .statusCode(response.getStatusCode().value())
                    .httpStatus(response.getStatusCode())
                    .headers(response.getHeaders())
                    .success(response.getStatusCode().is2xxSuccessful())
                    .build();

        } catch (HttpClientErrorException e) {
            log.warn("Client error for {} {}: {} - {}", method, url,
                    e.getStatusCode(), e.getResponseBodyAsString());
            return ApiResponse.<T>builder()
                    .statusCode(e.getStatusCode().value())
                    .httpStatus(e.getStatusCode())
                    .success(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();

        } catch (HttpServerErrorException e) {
            log.error("Server error for {} {}: {} - {}", method, url,
                    e.getStatusCode(), e.getResponseBodyAsString());
            return ApiResponse.<T>builder()
                    .statusCode(e.getStatusCode().value())
                    .httpStatus(e.getStatusCode())
                    .success(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();

        } catch (ResourceAccessException e) {
            log.error("Connection error for {} {}: {}", method, url, e.getMessage());
            return ApiResponse.<T>builder()
                    .statusCode(0)
                    .success(false)
                    .errorMessage("Connection failed: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error for {} {}: {}", method, url, e.getMessage(), e);
            return ApiResponse.<T>builder()
                    .statusCode(0)
                    .success(false)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}
