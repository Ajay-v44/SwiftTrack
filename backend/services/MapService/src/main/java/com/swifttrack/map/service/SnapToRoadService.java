package com.swifttrack.map.service;

import com.swifttrack.map.client.OsrmClient;
import com.swifttrack.map.config.CacheConfig;
import com.swifttrack.map.config.MapServiceProperties;
import com.swifttrack.map.dto.*;
import com.swifttrack.map.dto.request.SnapToRoadRequest;
import com.swifttrack.map.exception.RoutingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service for snapping GPS coordinates to road network
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapToRoadService {
    
    private final OsrmClient osrmClient;
    private final MapServiceProperties properties;
    
    /**
     * Snap coordinates to nearest road
     * Results are cached for 1 hour
     */
    @Cacheable(value = CacheConfig.SNAP_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forSnap(#path)")
    public SnapToRoadResponse snap(List<Coordinates> path) {
        return snap(path, properties.getDefaults().getSnapRadiusMeters());
    }
    
    /**
     * Snap coordinates with custom radius
     */
    public SnapToRoadResponse snap(List<Coordinates> path, double radiusMeters) {
        log.info("Snapping {} points to road (radius={}m)", path.size(), radiusMeters);
        
        validatePath(path);
        
        return osrmClient.snapToRoad(path, radiusMeters)
            .doOnSuccess(r -> {
                long snappedCount = r.getSnappedPoints().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsSnapped()))
                    .count();
                log.debug("Snapped {} of {} points", snappedCount, path.size());
            })
            .block();
    }
    
    /**
     * Snap from request DTO
     */
    public SnapToRoadResponse snap(SnapToRoadRequest request) {
        Double radius = request.getRadiusMeters();
        if (radius == null || radius <= 0) {
            radius = properties.getDefaults().getSnapRadiusMeters();
        }
        return snap(request.getPath(), radius);
    }
    
    /**
     * Async snap operation
     */
    public Mono<SnapToRoadResponse> snapAsync(List<Coordinates> path, double radiusMeters) {
        log.debug("Async snapping {} points", path.size());
        
        try {
            validatePath(path);
        } catch (RoutingException e) {
            return Mono.error(e);
        }
        
        return osrmClient.snapToRoad(path, radiusMeters);
    }
    
    /**
     * Snap a single point to nearest road
     */
    public SnappedPoint snapPoint(Coordinates point) {
        return snapPoint(point, properties.getDefaults().getSnapRadiusMeters());
    }
    
    /**
     * Snap a single point with custom radius
     */
    public SnappedPoint snapPoint(Coordinates point, double radiusMeters) {
        if (point == null || !point.isValid()) {
            throw new RoutingException("Invalid coordinates");
        }
        
        SnapToRoadResponse response = snap(List.of(point), radiusMeters);
        
        if (response != null && 
            response.getSnappedPoints() != null && 
            !response.getSnappedPoints().isEmpty()) {
            return response.getSnappedPoints().get(0);
        }
        
        return SnappedPoint.builder()
            .originalLocation(point)
            .isSnapped(false)
            .build();
    }
    
    /**
     * Check if point is on a road
     */
    public boolean isOnRoad(Coordinates point) {
        return isOnRoad(point, properties.getDefaults().getSnapRadiusMeters());
    }
    
    /**
     * Check if point is on a road with custom threshold
     */
    public boolean isOnRoad(Coordinates point, double thresholdMeters) {
        SnappedPoint snapped = snapPoint(point, thresholdMeters);
        return snapped.isWithinThreshold(thresholdMeters);
    }
    
    /**
     * Get distance from point to nearest road
     */
    public Double getDistanceToRoad(Coordinates point) {
        SnappedPoint snapped = snapPoint(point, 100); // Use larger radius to find road
        if (Boolean.TRUE.equals(snapped.getIsSnapped())) {
            return snapped.getSnapDistanceMeters();
        }
        return null; // No road found within radius
    }
    
    /**
     * Get the street name at a point
     */
    public String getStreetName(Coordinates point) {
        SnappedPoint snapped = snapPoint(point);
        if (Boolean.TRUE.equals(snapped.getIsSnapped())) {
            return snapped.getStreetName();
        }
        return null;
    }
    
    /**
     * Validate path input
     */
    private void validatePath(List<Coordinates> path) {
        if (path == null || path.isEmpty()) {
            throw new RoutingException("Path cannot be empty");
        }
        if (path.size() > 100) {
            throw new RoutingException("Path cannot have more than 100 points");
        }
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i) == null || !path.get(i).isValid()) {
                throw new RoutingException("Invalid coordinate at index " + i);
            }
        }
    }
}
