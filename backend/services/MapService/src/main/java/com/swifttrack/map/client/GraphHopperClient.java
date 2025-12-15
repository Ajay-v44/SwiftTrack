package com.swifttrack.map.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.swifttrack.map.config.MapServiceProperties;
import com.swifttrack.map.dto.*;
import com.swifttrack.map.exception.RoutingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client for GraphHopper Routing API (Alternative to OSRM)
 * 
 * @see <a href="https://docs.graphhopper.com/">GraphHopper API Documentation</a>
 */
@Slf4j
@Component
public class GraphHopperClient {
    
    private final WebClient webClient;
    private final MapServiceProperties properties;
    
    public GraphHopperClient(@Qualifier("graphHopperWebClient") WebClient webClient,
                             MapServiceProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }
    
    /**
     * Check if GraphHopper is enabled
     */
    public boolean isEnabled() {
        return properties.getGraphhopper().isEnabled();
    }
    
    /**
     * Get route directions between two points
     */
    public Mono<RouteResponse> getRoute(Coordinates origin, Coordinates destination, TravelMode mode) {
        if (!isEnabled()) {
            return Mono.error(new RoutingException("GraphHopper is not enabled"));
        }
        
        String profile = getProfile(mode);
        
        log.debug("GraphHopper route: {} -> {}, profile={}", origin, destination, profile);
        
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder
                    .path("/route")
                    .queryParam("point", origin.getLatitude() + "," + origin.getLongitude())
                    .queryParam("point", destination.getLatitude() + "," + destination.getLongitude())
                    .queryParam("profile", profile)
                    .queryParam("locale", properties.getDefaults().getLanguage())
                    .queryParam("calc_points", true)
                    .queryParam("instructions", true)
                    .queryParam("points_encoded", true);
                
                // Add API key if configured
                String apiKey = properties.getGraphhopper().getApiKey();
                if (apiKey != null && !apiKey.isBlank()) {
                    builder.queryParam("key", apiKey);
                }
                
                return builder.build();
            })
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
            .map(response -> parseRouteResponse(response, origin, destination, mode))
            .doOnError(e -> log.error("GraphHopper route failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new RoutingException("GraphHopper routing failed: " + e.getMessage(), e)));
    }
    
    /**
     * Calculate distance matrix
     */
    public Mono<MatrixResponse> getMatrix(List<Coordinates> origins, List<Coordinates> destinations, TravelMode mode) {
        if (!isEnabled()) {
            return Mono.error(new RoutingException("GraphHopper is not enabled"));
        }
        
        String profile = getProfile(mode);
        
        log.debug("GraphHopper matrix: {} origins, {} destinations, profile={}", 
                  origins.size(), destinations.size(), profile);
        
        // Build points parameters
        List<String> fromPoints = origins.stream()
            .map(c -> c.getLatitude() + "," + c.getLongitude())
            .collect(Collectors.toList());
        
        List<String> toPoints = destinations.stream()
            .map(c -> c.getLatitude() + "," + c.getLongitude())
            .collect(Collectors.toList());
        
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder
                    .path("/matrix")
                    .queryParam("profile", profile)
                    .queryParam("out_array", "distances")
                    .queryParam("out_array", "times");
                
                for (String point : fromPoints) {
                    builder.queryParam("from_point", point);
                }
                for (String point : toPoints) {
                    builder.queryParam("to_point", point);
                }
                
                String apiKey = properties.getGraphhopper().getApiKey();
                if (apiKey != null && !apiKey.isBlank()) {
                    builder.queryParam("key", apiKey);
                }
                
                return builder.build();
            })
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
            .map(response -> parseMatrixResponse(response, origins, destinations, mode))
            .doOnError(e -> log.error("GraphHopper matrix failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new RoutingException("GraphHopper matrix failed: " + e.getMessage(), e)));
    }
    
    /**
     * Get GraphHopper profile for travel mode
     */
    private String getProfile(TravelMode mode) {
        MapServiceProperties.ProfilesConfig profiles = properties.getGraphhopper().getProfiles();
        return switch (mode) {
            case DRIVING -> profiles.getDriving();
            case WALKING -> profiles.getWalking();
            case BIKE -> profiles.getBike();
            case DELIVERY -> profiles.getDelivery();
        };
    }
    
    /**
     * Parse GraphHopper route response
     */
    private RouteResponse parseRouteResponse(JsonNode response, Coordinates origin,
                                            Coordinates destination, TravelMode mode) {
        if (response.has("message")) {
            return RouteResponse.builder()
                .status(RouteResponse.RouteStatus.NOT_FOUND)
                .errorMessage(response.path("message").asText("Route not found"))
                .origin(origin)
                .destination(destination)
                .travelMode(mode)
                .build();
        }
        
        JsonNode paths = response.path("paths");
        if (!paths.isArray() || paths.isEmpty()) {
            return RouteResponse.builder()
                .status(RouteResponse.RouteStatus.ZERO_RESULTS)
                .errorMessage("No routes found")
                .origin(origin)
                .destination(destination)
                .travelMode(mode)
                .build();
        }
        
        JsonNode mainPath = paths.get(0);
        
        // Parse instructions
        List<StepInstruction> steps = new ArrayList<>();
        JsonNode instructions = mainPath.path("instructions");
        for (int i = 0; i < instructions.size(); i++) {
            JsonNode instr = instructions.get(i);
            steps.add(StepInstruction.builder()
                .stepIndex(i)
                .instruction(instr.path("text").asText(""))
                .distanceMeters(instr.path("distance").asDouble())
                .durationSeconds(instr.path("time").asDouble() / 1000) // GraphHopper returns ms
                .streetName(instr.path("street_name").asText(null))
                .build());
        }
        
        return RouteResponse.builder()
            .status(RouteResponse.RouteStatus.OK)
            .origin(origin)
            .destination(destination)
            .travelMode(mode)
            .distanceMeters(mainPath.path("distance").asDouble())
            .durationSeconds(mainPath.path("time").asDouble() / 1000) // GraphHopper returns ms
            .encodedPolyline(mainPath.path("points").asText(null))
            .steps(steps)
            .build();
    }
    
    /**
     * Parse GraphHopper matrix response
     */
    private MatrixResponse parseMatrixResponse(JsonNode response, List<Coordinates> origins,
                                              List<Coordinates> destinations, TravelMode mode) {
        if (response.has("message")) {
            return MatrixResponse.builder()
                .status(MatrixResponse.MatrixStatus.SERVICE_UNAVAILABLE)
                .errorMessage(response.path("message").asText("Matrix calculation failed"))
                .origins(origins)
                .destinations(destinations)
                .travelMode(mode)
                .build();
        }
        
        // Parse distances
        List<List<Double>> distances = new ArrayList<>();
        JsonNode distanceMatrix = response.path("distances");
        for (JsonNode row : distanceMatrix) {
            List<Double> rowValues = new ArrayList<>();
            for (JsonNode cell : row) {
                rowValues.add(cell.isNull() ? null : cell.asDouble());
            }
            distances.add(rowValues);
        }
        
        // Parse times (convert from ms to seconds)
        List<List<Double>> durations = new ArrayList<>();
        JsonNode timeMatrix = response.path("times");
        for (JsonNode row : timeMatrix) {
            List<Double> rowValues = new ArrayList<>();
            for (JsonNode cell : row) {
                rowValues.add(cell.isNull() ? null : cell.asDouble() / 1000);
            }
            durations.add(rowValues);
        }
        
        return MatrixResponse.builder()
            .status(MatrixResponse.MatrixStatus.OK)
            .origins(origins)
            .destinations(destinations)
            .travelMode(mode)
            .distances(distances)
            .durations(durations)
            .build();
    }
}
