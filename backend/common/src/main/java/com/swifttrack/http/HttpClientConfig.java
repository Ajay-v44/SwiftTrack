package com.swifttrack.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for HTTP client utilities.
 * Provides RestTemplate beans with configurable timeouts and interceptors.
 * 
 * Services using this configuration should import it via:
 * 
 * @Import(HttpClientConfig.class) or by component scanning.
 */
@Configuration
public class HttpClientConfig {

    /**
     * Creates a default RestTemplate with standard timeouts.
     * Connect timeout: 10 seconds
     * Read timeout: 30 seconds
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Creates a RestTemplate factory for custom configurations.
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30));
    }

    /**
     * Factory method to create a RestTemplate with custom timeouts.
     */
    public static RestTemplate createRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return new RestTemplate(factory);
    }
}
