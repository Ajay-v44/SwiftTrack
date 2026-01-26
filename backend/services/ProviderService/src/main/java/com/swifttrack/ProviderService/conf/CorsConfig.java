package com.swifttrack.ProviderService.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOriginPatterns("*") // Allow all origins (using patterns for flexibility)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Standard HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true) // Allow credentials (cookies, auth headers)
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}
