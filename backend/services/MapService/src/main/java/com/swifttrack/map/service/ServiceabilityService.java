package com.swifttrack.map.service;

import com.swifttrack.map.dto.Coordinates;
import com.swifttrack.map.dto.request.AreaCheckRequest;
import com.swifttrack.map.dto.response.AreaCheckResponse;
import com.swifttrack.map.exception.MapServiceException;
import com.swifttrack.map.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for checking if points are within service areas (polygons)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceabilityService {
    
    /**
     * Check if a point is inside a polygon (service area)
     */
    public AreaCheckResponse checkServiceability(Coordinates point, List<Coordinates> polygon) {
        log.info("Checking serviceability for point: {}", point);
        
        validateInput(point, polygon);
        
        boolean isInside = GeoUtils.isPointInsidePolygon(point, polygon);
        
        AreaCheckResponse.AreaCheckResponseBuilder builder = AreaCheckResponse.builder()
            .point(point)
            .isInside(isInside)
            .isServiceable(isInside);
        
        if (isInside) {
            builder.message("Point is within the service area");
        } else {
            // Calculate distance to boundary
            double distance = GeoUtils.distanceToPolygonBoundary(point, polygon);
            builder.distanceToBoundaryMeters(distance);
            builder.message("Point is outside the service area. Distance to boundary: " + 
                          GeoUtils.formatDistance(distance));
        }
        
        return builder.build();
    }
    
    /**
     * Check from request DTO
     */
    public AreaCheckResponse checkServiceability(AreaCheckRequest request) {
        return checkServiceability(request.getPoint(), request.getPolygon());
    }
    
    /**
     * Simple check without additional info
     */
    public boolean isPointServiceable(Coordinates point, List<Coordinates> polygon) {
        validateInput(point, polygon);
        return GeoUtils.isPointInsidePolygon(point, polygon);
    }
    
    /**
     * Check if point is within a circular service area
     */
    public AreaCheckResponse checkServiceabilityRadius(Coordinates point, 
                                                        Coordinates center, 
                                                        double radiusMeters) {
        log.debug("Checking if point {} is within {} meters of {}", point, radiusMeters, center);
        
        if (point == null || !point.isValid()) {
            throw new MapServiceException("Invalid point coordinates");
        }
        if (center == null || !center.isValid()) {
            throw new MapServiceException("Invalid center coordinates");
        }
        if (radiusMeters <= 0) {
            throw new MapServiceException("Radius must be positive");
        }
        
        double distance = GeoUtils.distance(point, center);
        boolean isInside = distance <= radiusMeters;
        
        return AreaCheckResponse.builder()
            .point(point)
            .isInside(isInside)
            .isServiceable(isInside)
            .distanceToBoundaryMeters(isInside ? 0 : distance - radiusMeters)
            .message(isInside 
                ? "Point is within the service radius" 
                : "Point is " + GeoUtils.formatDistance(distance - radiusMeters) + " outside the service radius")
            .build();
    }
    
    /**
     * Check if multiple points are serviceable
     */
    public List<AreaCheckResponse> checkMultiplePoints(List<Coordinates> points, 
                                                       List<Coordinates> polygon) {
        if (points == null || points.isEmpty()) {
            throw new MapServiceException("Points list cannot be empty");
        }
        
        return points.stream()
            .map(point -> {
                try {
                    return checkServiceability(point, polygon);
                } catch (Exception e) {
                    log.warn("Failed to check point {}: {}", point, e.getMessage());
                    return AreaCheckResponse.builder()
                        .point(point)
                        .isInside(false)
                        .isServiceable(false)
                        .message("Error checking point: " + e.getMessage())
                        .build();
                }
            })
            .toList();
    }
    
    /**
     * Check if a route is entirely within a service area
     */
    public boolean isRouteServiceable(List<Coordinates> routePoints, List<Coordinates> polygon) {
        if (routePoints == null || routePoints.isEmpty()) {
            return false;
        }
        
        validatePolygon(polygon);
        
        return routePoints.stream()
            .allMatch(point -> point != null && 
                              point.isValid() && 
                              GeoUtils.isPointInsidePolygon(point, polygon));
    }
    
    /**
     * Get the percentage of a route that is serviceable
     */
    public double getRouteServiceabilityPercentage(List<Coordinates> routePoints, 
                                                   List<Coordinates> polygon) {
        if (routePoints == null || routePoints.isEmpty()) {
            return 0;
        }
        
        validatePolygon(polygon);
        
        long serviceablePoints = routePoints.stream()
            .filter(point -> point != null && 
                            point.isValid() && 
                            GeoUtils.isPointInsidePolygon(point, polygon))
            .count();
        
        return (double) serviceablePoints / routePoints.size() * 100;
    }
    
    /**
     * Find the first point where a route exits the service area
     */
    public Coordinates findServiceAreaExit(List<Coordinates> routePoints, 
                                           List<Coordinates> polygon) {
        if (routePoints == null || routePoints.isEmpty()) {
            return null;
        }
        
        validatePolygon(polygon);
        
        boolean wasInside = false;
        
        for (Coordinates point : routePoints) {
            if (point == null || !point.isValid()) continue;
            
            boolean isInside = GeoUtils.isPointInsidePolygon(point, polygon);
            
            if (wasInside && !isInside) {
                return point; // First point outside after being inside
            }
            
            wasInside = isInside;
        }
        
        return null; // Route never exits the service area
    }
    
    /**
     * Validate input coordinates and polygon
     */
    private void validateInput(Coordinates point, List<Coordinates> polygon) {
        if (point == null || !point.isValid()) {
            throw new MapServiceException("Invalid point coordinates");
        }
        validatePolygon(polygon);
    }
    
    /**
     * Validate polygon
     */
    private void validatePolygon(List<Coordinates> polygon) {
        if (polygon == null || polygon.size() < 3) {
            throw new MapServiceException("Polygon must have at least 3 points");
        }
        
        for (int i = 0; i < polygon.size(); i++) {
            if (polygon.get(i) == null || !polygon.get(i).isValid()) {
                throw new MapServiceException("Invalid polygon point at index " + i);
            }
        }
    }
}
