package com.swifttrack.DriverService.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class to load environment variables from .env file.
 * 
 * This is the industry-standard Spring approach:
 * - Uses @PropertySource with custom PropertySourceFactory
 * - No external dependencies required
 * - Works with Spring's Environment and @Value annotations
 * - Supports fail-safe with ignoreResourceNotFound = true
 */
@Configuration
@PropertySource(value = "file:.env", factory = EnvPropertySourceFactory.class, ignoreResourceNotFound = true)
public class EnvConfiguration {
    // Configuration is loaded through @PropertySource annotation
    // No additional configuration needed here
}
