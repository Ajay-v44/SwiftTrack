package com.swifttrack.map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SwiftTrack Map Service Application
 * 
 * This microservice provides comprehensive geospatial APIs powered by OpenStreetMap:
 * - Geocoding (Forward & Reverse)
 * - Routing & Directions
 * - Distance Matrix calculations
 * - ETA estimation
 * - Snap-to-road functionality
 * - Serviceability area checks
 * 
 * Uses:
 * - Nominatim for geocoding
 * - OSRM / GraphHopper for routing
 * - Redis for caching
 */
@SpringBootApplication(scanBasePackages = {
    "com.swifttrack.map",
    "com.swifttrack.http"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.swifttrack.FeignClient")
@EnableCaching
@EnableAsync
public class MapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapServiceApplication.class, args);
    }
}
