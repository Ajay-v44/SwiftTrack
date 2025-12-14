package com.swifttrack.map.controller;

import com.swifttrack.map.config.MapServiceProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health and status endpoints for Map Service
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check and service status endpoints")
public class HealthController {
    
    private final MapServiceProperties properties;
    
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Basic health check endpoint")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MapService");
        response.put("timestamp", Instant.now());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    @Operation(summary = "Service Info", description = "Get detailed service information")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SwiftTrack Map Service");
        response.put("version", "1.0.0");
        response.put("description", "Geospatial APIs powered by OpenStreetMap");
        
        // Provider info
        Map<String, Object> providers = new HashMap<>();
        
        Map<String, Object> nominatim = new HashMap<>();
        nominatim.put("url", properties.getNominatim().getEffectiveUrl());
        nominatim.put("useLocal", properties.getNominatim().isUseLocal());
        providers.put("nominatim", nominatim);
        
        Map<String, Object> osrm = new HashMap<>();
        osrm.put("url", properties.getOsrm().getEffectiveUrl());
        osrm.put("useLocal", properties.getOsrm().isUseLocal());
        providers.put("osrm", osrm);
        
        Map<String, Object> graphhopper = new HashMap<>();
        graphhopper.put("enabled", properties.getGraphhopper().isEnabled());
        graphhopper.put("url", properties.getGraphhopper().getBaseUrl());
        providers.put("graphhopper", graphhopper);
        
        response.put("providers", providers);
        response.put("routingEngine", properties.getRoutingEngine());
        
        // Capabilities
        Map<String, Boolean> capabilities = new HashMap<>();
        capabilities.put("geocoding", true);
        capabilities.put("reverseGeocoding", true);
        capabilities.put("routing", true);
        capabilities.put("distanceMatrix", true);
        capabilities.put("eta", true);
        capabilities.put("snapToRoad", true);
        capabilities.put("serviceabilityCheck", true);
        response.put("capabilities", capabilities);
        
        // Supported modes
        response.put("travelModes", new String[] {"DRIVING", "WALKING", "BIKE", "DELIVERY"});
        
        response.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ready")
    @Operation(summary = "Readiness Check", description = "Check if service is ready to handle requests")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();
        
        // In a production environment, you would check connectivity to:
        // - Redis cache
        // - Nominatim
        // - OSRM/GraphHopper
        
        boolean isReady = true;
        
        response.put("ready", isReady);
        response.put("status", isReady ? "READY" : "NOT_READY");
        response.put("timestamp", Instant.now());
        
        if (isReady) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }
}
