package com.swifttrack.map.service;

import com.swifttrack.map.client.GraphHopperClient;
import com.swifttrack.map.client.OsrmClient;
import com.swifttrack.map.config.CacheConfig;
import com.swifttrack.map.config.MapServiceProperties;
import com.swifttrack.map.dto.*;
import com.swifttrack.map.dto.request.DirectionsRequest;
import com.swifttrack.map.exception.RoutingException;
import com.swifttrack.map.util.CacheKeyGenerator;
import com.swifttrack.map.util.PolylineUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service for routing and directions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {
    
    private final OsrmClient osrmClient;
    private final GraphHopperClient graphHopperClient;
    private final MapServiceProperties properties;
    
    /**
     * Get directions between two points
     * Results are cached for 1 hour
     */
    @Cacheable(value = CacheConfig.ROUTE_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forRoute(#origin, #destination, #mode.name())")
    public RouteResponse getDirections(Coordinates origin, Coordinates destination, TravelMode mode) {
        log.info("Calculating directions: {} -> {} (mode={})", origin, destination, mode);
        
        validateCoordinates(origin, "Origin");
        validateCoordinates(destination, "Destination");
        
        RouteResponse response = getRoutingClient()
            .flatMap(client -> {
                if (client.equals("graphhopper") && graphHopperClient.isEnabled()) {
                    return graphHopperClient.getRoute(origin, destination, mode);
                }
                return osrmClient.getRoute(origin, destination, mode);
            })
            .doOnSuccess(r -> log.debug("Route calculated: {} km, {} min", 
                r.getDistanceMeters() / 1000, r.getDurationSeconds() / 60))
            .block();
        
        // Add formatted text
        if (response != null && response.getStatus() == RouteResponse.RouteStatus.OK) {
            response.setDistanceText(response.getFormattedDistance());
            response.setDurationText(response.getFormattedDuration());
        }
        
        return response;
    }
    
    /**
     * Get directions from request DTO
     */
    public RouteResponse getDirections(DirectionsRequest request) {
        TravelMode mode = request.getMode() != null ? request.getMode() : TravelMode.DRIVING;
        
        RouteResponse response = getDirections(request.getOrigin(), request.getDestination(), mode);
        
        // Handle geometry options
        if (response != null && response.getStatus() == RouteResponse.RouteStatus.OK) {
            // Decode geometry if requested
            if (Boolean.TRUE.equals(request.getGeometry()) && response.getEncodedPolyline() != null) {
                response.setGeometry(PolylineUtils.decode(response.getEncodedPolyline()));
            }
            
            // Remove steps if not requested
            if (Boolean.FALSE.equals(request.getSteps())) {
                response.setSteps(null);
            }
        }
        
        return response;
    }
    
    /**
     * Async directions
     */
    public Mono<RouteResponse> getDirectionsAsync(Coordinates origin, Coordinates destination, TravelMode mode) {
        log.debug("Async directions: {} -> {} (mode={})", origin, destination, mode);
        
        try {
            validateCoordinates(origin, "Origin");
            validateCoordinates(destination, "Destination");
        } catch (RoutingException e) {
            return Mono.error(e);
        }
        
        return getRoutingClient()
            .flatMap(client -> {
                if (client.equals("graphhopper") && graphHopperClient.isEnabled()) {
                    return graphHopperClient.getRoute(origin, destination, mode);
                }
                return osrmClient.getRoute(origin, destination, mode);
            });
    }
    
    /**
     * Get the shortest path (minimum distance)
     */
    public RouteResponse getShortestPath(Coordinates origin, Coordinates destination, TravelMode mode) {
        // OSRM already returns the shortest path by default
        return getDirections(origin, destination, mode);
    }
    
    /**
     * Get route with waypoints
     */
    public RouteResponse getDirectionsWithWaypoints(Coordinates origin, List<Coordinates> waypoints, 
                                                    Coordinates destination, TravelMode mode) {
        log.info("Calculating route with {} waypoints", waypoints != null ? waypoints.size() : 0);
        
        if (waypoints == null || waypoints.isEmpty()) {
            return getDirections(origin, destination, mode);
        }
        
        // Calculate segments and combine
        RouteResponse combined = RouteResponse.builder()
            .status(RouteResponse.RouteStatus.OK)
            .origin(origin)
            .destination(destination)
            .travelMode(mode)
            .distanceMeters(0.0)
            .durationSeconds(0.0)
            .build();
        
        Coordinates current = origin;
        StringBuilder combinedPolyline = new StringBuilder();
        
        for (Coordinates waypoint : waypoints) {
            RouteResponse segment = getDirections(current, waypoint, mode);
            if (segment.getStatus() != RouteResponse.RouteStatus.OK) {
                return segment; // Return error
            }
            combined.setDistanceMeters(combined.getDistanceMeters() + segment.getDistanceMeters());
            combined.setDurationSeconds(combined.getDurationSeconds() + segment.getDurationSeconds());
            if (segment.getEncodedPolyline() != null) {
                combinedPolyline.append(segment.getEncodedPolyline());
            }
            current = waypoint;
        }
        
        // Final segment to destination
        RouteResponse finalSegment = getDirections(current, destination, mode);
        if (finalSegment.getStatus() != RouteResponse.RouteStatus.OK) {
            return finalSegment;
        }
        combined.setDistanceMeters(combined.getDistanceMeters() + finalSegment.getDistanceMeters());
        combined.setDurationSeconds(combined.getDurationSeconds() + finalSegment.getDurationSeconds());
        
        combined.setWaypoints(waypoints);
        combined.setDistanceText(combined.getFormattedDistance());
        combined.setDurationText(combined.getFormattedDuration());
        
        return combined;
    }
    
    /**
     * Get the active routing client name
     */
    private Mono<String> getRoutingClient() {
        String engine = properties.getRoutingEngine();
        if ("graphhopper".equalsIgnoreCase(engine) && graphHopperClient.isEnabled()) {
            return Mono.just("graphhopper");
        }
        return Mono.just("osrm");
    }
    
    /**
     * Validate coordinates
     */
    private void validateCoordinates(Coordinates coords, String name) {
        if (coords == null) {
            throw new RoutingException(name + " coordinates cannot be null");
        }
        if (coords.getLatitude() == null || coords.getLongitude() == null) {
            throw new RoutingException(name + " must have valid latitude and longitude");
        }
        if (!coords.isValid()) {
            throw new RoutingException(name + " has invalid coordinate values");
        }
    }
}
