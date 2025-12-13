package com.swifttrack.map.service;

import com.swifttrack.map.client.GraphHopperClient;
import com.swifttrack.map.client.OsrmClient;
import com.swifttrack.map.config.CacheConfig;
import com.swifttrack.map.config.MapServiceProperties;
import com.swifttrack.map.dto.*;
import com.swifttrack.map.dto.request.MatrixRequest;
import com.swifttrack.map.exception.RoutingException;
import com.swifttrack.map.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for distance matrix calculations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatrixService {
    
    private final OsrmClient osrmClient;
    private final GraphHopperClient graphHopperClient;
    private final MapServiceProperties properties;
    
    /**
     * Calculate distance matrix between origins and destinations
     * Results are cached for 30 minutes
     */
    @Cacheable(value = CacheConfig.MATRIX_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forMatrix(#origins, #destinations, #mode.name())")
    public MatrixResponse calculateMatrix(List<Coordinates> origins, List<Coordinates> destinations, TravelMode mode) {
        log.info("Calculating matrix: {} origins x {} destinations (mode={})", 
                 origins.size(), destinations.size(), mode);
        
        validateMatrixInput(origins, destinations);
        
        MatrixResponse response = getRoutingClient()
            .flatMap(client -> {
                if (client.equals("graphhopper") && graphHopperClient.isEnabled()) {
                    return graphHopperClient.getMatrix(origins, destinations, mode);
                }
                return osrmClient.getMatrix(origins, destinations, mode);
            })
            .doOnSuccess(r -> log.debug("Matrix calculated: {} x {}", 
                r.getDistances().size(), r.getDistances().isEmpty() ? 0 : r.getDistances().get(0).size()))
            .block();
        
        // Add formatted texts
        if (response != null && response.getStatus() == MatrixResponse.MatrixStatus.OK) {
            response.setDistanceTexts(formatDistanceMatrix(response.getDistances()));
            response.setDurationTexts(formatDurationMatrix(response.getDurations()));
        }
        
        return response;
    }
    
    /**
     * Calculate matrix from request DTO
     */
    public MatrixResponse calculateMatrix(MatrixRequest request) {
        TravelMode mode = request.getMode() != null ? request.getMode() : TravelMode.DRIVING;
        return calculateMatrix(request.getOrigins(), request.getDestinations(), mode);
    }
    
    /**
     * Async matrix calculation
     */
    public Mono<MatrixResponse> calculateMatrixAsync(List<Coordinates> origins, 
                                                     List<Coordinates> destinations, 
                                                     TravelMode mode) {
        log.debug("Async matrix: {} origins x {} destinations", origins.size(), destinations.size());
        
        try {
            validateMatrixInput(origins, destinations);
        } catch (RoutingException e) {
            return Mono.error(e);
        }
        
        return getRoutingClient()
            .flatMap(client -> {
                if (client.equals("graphhopper") && graphHopperClient.isEnabled()) {
                    return graphHopperClient.getMatrix(origins, destinations, mode);
                }
                return osrmClient.getMatrix(origins, destinations, mode);
            });
    }
    
    /**
     * Calculate simple distance between two points (straight line)
     */
    public double calculateStraightLineDistance(Coordinates origin, Coordinates destination) {
        return GeoUtils.distance(origin, destination);
    }
    
    /**
     * Calculate road distance between two points
     */
    public Double calculateRoadDistance(Coordinates origin, Coordinates destination, TravelMode mode) {
        MatrixResponse matrix = calculateMatrix(
            List.of(origin), 
            List.of(destination), 
            mode
        );
        
        if (matrix != null && matrix.getStatus() == MatrixResponse.MatrixStatus.OK) {
            return matrix.getDistance(0, 0);
        }
        
        return null;
    }
    
    /**
     * Find the closest destination to an origin
     */
    public Integer findClosestDestination(Coordinates origin, List<Coordinates> destinations, TravelMode mode) {
        MatrixResponse matrix = calculateMatrix(List.of(origin), destinations, mode);
        
        if (matrix != null && matrix.getStatus() == MatrixResponse.MatrixStatus.OK) {
            return matrix.getClosestDestination(0);
        }
        
        return null;
    }
    
    /**
     * Find the fastest route to a destination
     */
    public Integer findFastestDestination(Coordinates origin, List<Coordinates> destinations, TravelMode mode) {
        MatrixResponse matrix = calculateMatrix(List.of(origin), destinations, mode);
        
        if (matrix != null && matrix.getStatus() == MatrixResponse.MatrixStatus.OK) {
            return matrix.getFastestDestination(0);
        }
        
        return null;
    }
    
    /**
     * Get the active routing client
     */
    private Mono<String> getRoutingClient() {
        String engine = properties.getRoutingEngine();
        if ("graphhopper".equalsIgnoreCase(engine) && graphHopperClient.isEnabled()) {
            return Mono.just("graphhopper");
        }
        return Mono.just("osrm");
    }
    
    /**
     * Validate matrix inputs
     */
    private void validateMatrixInput(List<Coordinates> origins, List<Coordinates> destinations) {
        if (origins == null || origins.isEmpty()) {
            throw new RoutingException("Origins list cannot be empty");
        }
        if (destinations == null || destinations.isEmpty()) {
            throw new RoutingException("Destinations list cannot be empty");
        }
        
        int maxOrigins = properties.getDefaults().getMaxMatrixOrigins();
        int maxDestinations = properties.getDefaults().getMaxMatrixDestinations();
        
        if (origins.size() > maxOrigins) {
            throw new RoutingException("Too many origins. Maximum is " + maxOrigins);
        }
        if (destinations.size() > maxDestinations) {
            throw new RoutingException("Too many destinations. Maximum is " + maxDestinations);
        }
        
        // Validate each coordinate
        for (int i = 0; i < origins.size(); i++) {
            if (origins.get(i) == null || !origins.get(i).isValid()) {
                throw new RoutingException("Invalid origin coordinate at index " + i);
            }
        }
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i) == null || !destinations.get(i).isValid()) {
                throw new RoutingException("Invalid destination coordinate at index " + i);
            }
        }
    }
    
    /**
     * Format distance matrix values
     */
    private List<List<String>> formatDistanceMatrix(List<List<Double>> distances) {
        List<List<String>> formatted = new ArrayList<>();
        if (distances == null) return formatted;
        
        for (List<Double> row : distances) {
            List<String> formattedRow = new ArrayList<>();
            for (Double distance : row) {
                formattedRow.add(distance != null ? GeoUtils.formatDistance(distance) : null);
            }
            formatted.add(formattedRow);
        }
        return formatted;
    }
    
    /**
     * Format duration matrix values
     */
    private List<List<String>> formatDurationMatrix(List<List<Double>> durations) {
        List<List<String>> formatted = new ArrayList<>();
        if (durations == null) return formatted;
        
        for (List<Double> row : durations) {
            List<String> formattedRow = new ArrayList<>();
            for (Double duration : row) {
                formattedRow.add(duration != null ? GeoUtils.formatDuration(duration) : null);
            }
            formatted.add(formattedRow);
        }
        return formatted;
    }
}
