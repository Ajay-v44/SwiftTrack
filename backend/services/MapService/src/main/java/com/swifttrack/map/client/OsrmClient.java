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
 * Client for OSRM (Open Source Routing Machine) API
 * 
 * @see <a href="http://project-osrm.org/docs/v5.24.0/api/">OSRM API Documentation</a>
 */
@Slf4j
@Component
public class OsrmClient {
    
    private final WebClient webClient;
    private final MapServiceProperties properties;
    
    public OsrmClient(@Qualifier("osrmWebClient") WebClient webClient,
                      MapServiceProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }
    
    /**
     * Get route directions between two points
     */
    public Mono<RouteResponse> getRoute(Coordinates origin, Coordinates destination, TravelMode mode) {
        return getRoute(origin, destination, mode, true, true, false);
    }
    
    /**
     * Get route with options
     */
    public Mono<RouteResponse> getRoute(Coordinates origin, Coordinates destination, 
                                        TravelMode mode, boolean steps, boolean geometry, 
                                        boolean alternatives) {
        String profile = getProfile(mode);
        String coordinates = origin.toOsrmFormat() + ";" + destination.toOsrmFormat();
        
        log.debug("OSRM route: {} -> {}, profile={}", origin, destination, profile);
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/route/v1/{profile}/{coordinates}")
                .queryParam("overview", geometry ? "full" : "false")
                .queryParam("geometries", "polyline")
                .queryParam("steps", steps)
                .queryParam("alternatives", alternatives)
                .queryParam("annotations", "distance,duration")
                .build(profile, coordinates))
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(properties.getOsrm().getRetryAttempts(), Duration.ofMillis(500)))
            .map(response -> parseRouteResponse(response, origin, destination, mode))
            .doOnError(e -> log.error("OSRM route failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new RoutingException("Failed to calculate route: " + e.getMessage(), e)));
    }
    
    /**
     * Calculate distance matrix
     */
    public Mono<MatrixResponse> getMatrix(List<Coordinates> origins, List<Coordinates> destinations, TravelMode mode) {
        String profile = getProfile(mode);
        
        // Combine all coordinates for OSRM
        List<Coordinates> allCoords = new ArrayList<>();
        allCoords.addAll(origins);
        allCoords.addAll(destinations);
        
        String coordinates = allCoords.stream()
            .map(Coordinates::toOsrmFormat)
            .collect(Collectors.joining(";"));
        
        // Create source and destination indices
        String sources = java.util.stream.IntStream.range(0, origins.size())
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(";"));
        
        String destinations_param = java.util.stream.IntStream.range(origins.size(), allCoords.size())
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(";"));
        
        log.debug("OSRM matrix: {} origins, {} destinations, profile={}", 
                  origins.size(), destinations.size(), profile);
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/table/v1/{profile}/{coordinates}")
                .queryParam("sources", sources)
                .queryParam("destinations", destinations_param)
                .queryParam("annotations", "distance,duration")
                .build(profile, coordinates))
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(properties.getOsrm().getRetryAttempts(), Duration.ofMillis(500)))
            .map(response -> parseMatrixResponse(response, origins, destinations, mode))
            .doOnError(e -> log.error("OSRM matrix failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new RoutingException("Failed to calculate matrix: " + e.getMessage(), e)));
    }
    
    /**
     * Snap points to nearest road
     */
    public Mono<SnapToRoadResponse> snapToRoad(List<Coordinates> path, double radiusMeters) {
        String coordinates = path.stream()
            .map(Coordinates::toOsrmFormat)
            .collect(Collectors.joining(";"));
        
        String radiuses = path.stream()
            .map(c -> String.valueOf((int) radiusMeters))
            .collect(Collectors.joining(";"));
        
        log.debug("OSRM snap: {} points, radius={}", path.size(), radiusMeters);
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/match/v1/driving/{coordinates}")
                .queryParam("geometries", "polyline")
                .queryParam("overview", "full")
                .queryParam("radiuses", radiuses)
                .queryParam("gaps", "split")
                .queryParam("tidy", "true")
                .build(coordinates))
            .retrieve()
            .bodyToMono(JsonNode.class)
            .retryWhen(Retry.backoff(properties.getOsrm().getRetryAttempts(), Duration.ofMillis(500)))
            .map(response -> parseSnapResponse(response, path))
            .doOnError(e -> log.error("OSRM snap failed: {}", e.getMessage()))
            .onErrorResume(e -> Mono.error(new RoutingException("Failed to snap to road: " + e.getMessage(), e)));
    }
    
    /**
     * Get OSRM profile for travel mode
     */
    private String getProfile(TravelMode mode) {
        MapServiceProperties.ProfilesConfig profiles = properties.getOsrm().getProfiles();
        return switch (mode) {
            case DRIVING -> profiles.getDriving();
            case WALKING -> profiles.getWalking();
            case BIKE -> profiles.getBike();
            case DELIVERY -> profiles.getDelivery();
        };
    }
    
    /**
     * Parse OSRM route response
     */
    private RouteResponse parseRouteResponse(JsonNode response, Coordinates origin, 
                                             Coordinates destination, TravelMode mode) {
        String code = response.path("code").asText("");
        
        if (!"Ok".equalsIgnoreCase(code)) {
            return RouteResponse.builder()
                .status(RouteResponse.RouteStatus.NOT_FOUND)
                .errorMessage(response.path("message").asText("Route not found"))
                .origin(origin)
                .destination(destination)
                .travelMode(mode)
                .build();
        }
        
        JsonNode routes = response.path("routes");
        if (!routes.isArray() || routes.isEmpty()) {
            return RouteResponse.builder()
                .status(RouteResponse.RouteStatus.ZERO_RESULTS)
                .errorMessage("No routes found")
                .origin(origin)
                .destination(destination)
                .travelMode(mode)
                .build();
        }
        
        JsonNode mainRoute = routes.get(0);
        
        // Parse steps
        List<StepInstruction> steps = new ArrayList<>();
        JsonNode legs = mainRoute.path("legs");
        if (legs.isArray() && !legs.isEmpty()) {
            JsonNode legSteps = legs.get(0).path("steps");
            for (int i = 0; i < legSteps.size(); i++) {
                steps.add(parseStep(legSteps.get(i), i));
            }
        }
        
        // Parse alternative routes
        List<RouteResponse> alternatives = new ArrayList<>();
        for (int i = 1; i < routes.size(); i++) {
            JsonNode altRoute = routes.get(i);
            alternatives.add(RouteResponse.builder()
                .status(RouteResponse.RouteStatus.OK)
                .distanceMeters(altRoute.path("distance").asDouble())
                .durationSeconds(altRoute.path("duration").asDouble())
                .encodedPolyline(altRoute.path("geometry").asText(null))
                .build());
        }
        
        return RouteResponse.builder()
            .status(RouteResponse.RouteStatus.OK)
            .origin(origin)
            .destination(destination)
            .travelMode(mode)
            .distanceMeters(mainRoute.path("distance").asDouble())
            .durationSeconds(mainRoute.path("duration").asDouble())
            .encodedPolyline(mainRoute.path("geometry").asText(null))
            .steps(steps)
            .alternatives(alternatives.isEmpty() ? null : alternatives)
            .build();
    }
    
    /**
     * Parse a route step
     */
    private StepInstruction parseStep(JsonNode stepNode, int index) {
        JsonNode maneuver = stepNode.path("maneuver");
        JsonNode startLoc = maneuver.path("location");
        
        String maneuverType = maneuver.path("type").asText(null);
        String modifier = maneuver.path("modifier").asText(null);
        
        return StepInstruction.builder()
            .stepIndex(index)
            .instruction(stepNode.path("name").asText(""))
            .maneuver(StepInstruction.ManeuverType.fromOsrm(maneuverType, modifier))
            .maneuverModifier(modifier)
            .distanceMeters(stepNode.path("distance").asDouble())
            .durationSeconds(stepNode.path("duration").asDouble())
            .startLocation(Coordinates.builder()
                .longitude(startLoc.get(0).asDouble())
                .latitude(startLoc.get(1).asDouble())
                .build())
            .streetName(stepNode.path("name").asText(null))
            .roadRef(stepNode.path("ref").asText(null))
            .bearingBefore(maneuver.path("bearing_before").asDouble())
            .bearingAfter(maneuver.path("bearing_after").asDouble())
            .build();
    }
    
    /**
     * Parse OSRM matrix response
     */
    private MatrixResponse parseMatrixResponse(JsonNode response, List<Coordinates> origins,
                                               List<Coordinates> destinations, TravelMode mode) {
        String code = response.path("code").asText("");
        
        if (!"Ok".equalsIgnoreCase(code)) {
            return MatrixResponse.builder()
                .status(MatrixResponse.MatrixStatus.SERVICE_UNAVAILABLE)
                .errorMessage(response.path("message").asText("Matrix calculation failed"))
                .origins(origins)
                .destinations(destinations)
                .travelMode(mode)
                .build();
        }
        
        // Parse duration matrix
        List<List<Double>> durations = new ArrayList<>();
        JsonNode durationMatrix = response.path("durations");
        for (JsonNode row : durationMatrix) {
            List<Double> rowValues = new ArrayList<>();
            for (JsonNode cell : row) {
                rowValues.add(cell.isNull() ? null : cell.asDouble());
            }
            durations.add(rowValues);
        }
        
        // Parse distance matrix
        List<List<Double>> distances = new ArrayList<>();
        JsonNode distanceMatrix = response.path("distances");
        for (JsonNode row : distanceMatrix) {
            List<Double> rowValues = new ArrayList<>();
            for (JsonNode cell : row) {
                rowValues.add(cell.isNull() ? null : cell.asDouble());
            }
            distances.add(rowValues);
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
    
    /**
     * Parse OSRM snap/match response
     */
    private SnapToRoadResponse parseSnapResponse(JsonNode response, List<Coordinates> originalPath) {
        String code = response.path("code").asText("");
        
        if (!"Ok".equalsIgnoreCase(code)) {
            return SnapToRoadResponse.builder()
                .status(SnapToRoadResponse.SnapStatus.NOT_FOUND)
                .errorMessage(response.path("message").asText("Snap to road failed"))
                .originalPoints(originalPath)
                .build();
        }
        
        List<SnappedPoint> snappedPoints = new ArrayList<>();
        JsonNode tracepoints = response.path("tracepoints");
        
        for (int i = 0; i < tracepoints.size(); i++) {
            JsonNode tracepoint = tracepoints.get(i);
            Coordinates original = i < originalPath.size() ? originalPath.get(i) : null;
            
            if (tracepoint.isNull()) {
                snappedPoints.add(SnappedPoint.builder()
                    .originalIndex(i)
                    .originalLocation(original)
                    .isSnapped(false)
                    .build());
            } else {
                JsonNode location = tracepoint.path("location");
                Coordinates snapped = Coordinates.builder()
                    .longitude(location.get(0).asDouble())
                    .latitude(location.get(1).asDouble())
                    .build();
                
                double distance = original != null ? original.distanceTo(snapped) : 0;
                
                snappedPoints.add(SnappedPoint.builder()
                    .originalIndex(i)
                    .originalLocation(original)
                    .snappedLocation(snapped)
                    .snapDistanceMeters(distance)
                    .isSnapped(true)
                    .streetName(tracepoint.path("name").asText(null))
                    .waypointIndex(tracepoint.path("waypoint_index").asInt())
                    .build());
            }
        }
        
        // Get encoded polyline from matchings
        String polyline = null;
        JsonNode matchings = response.path("matchings");
        if (matchings.isArray() && !matchings.isEmpty()) {
            polyline = matchings.get(0).path("geometry").asText(null);
        }
        
        boolean allSnapped = snappedPoints.stream().allMatch(p -> Boolean.TRUE.equals(p.getIsSnapped()));
        
        return SnapToRoadResponse.builder()
            .status(allSnapped ? SnapToRoadResponse.SnapStatus.OK : SnapToRoadResponse.SnapStatus.PARTIAL)
            .originalPoints(originalPath)
            .snappedPoints(snappedPoints)
            .encodedPolyline(polyline)
            .build();
    }
}
