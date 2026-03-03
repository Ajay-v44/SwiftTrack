package com.swifttrack.AIDispatchService.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class to load environment variables from .env file.
 * Uses @PropertySource with custom PropertySourceFactory.
 */
@Configuration
@PropertySource(value = "file:.env", factory = EnvPropertySourceFactory.class, ignoreResourceNotFound = true)
public class EnvConfiguration {
}
