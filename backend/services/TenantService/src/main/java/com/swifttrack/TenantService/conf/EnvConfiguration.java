package com.swifttrack.TenantService.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(
    value = "file:.env",
    factory = EnvPropertySourceFactory.class,
    ignoreResourceNotFound = true
)
public class EnvConfiguration {
}
