package com.swifttrack.map.util;

import com.swifttrack.map.dto.Coordinates;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.*;

import java.util.List;

/**
 * Utility class for geospatial calculations
 */
@UtilityClass
public class GeoUtils {
    
    private static final double EARTH_RADIUS_METERS = 6371000;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    
    /**
     * Calculate distance between two points using Haversine formula
     * 
     * @return distance in meters
     */
    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_METERS * c;
    }
    
    /**
     * Calculate distance between two Coordinates
     */
    public double distance(Coordinates p1, Coordinates p2) {
        if (p1 == null || p2 == null) return 0;
        return haversineDistance(p1.getLatitude(), p1.getLongitude(), 
                                p2.getLatitude(), p2.getLongitude());
    }
    
    /**
     * Check if a point is inside a polygon using JTS
     */
    public boolean isPointInsidePolygon(Coordinates point, List<Coordinates> polygon) {
        if (point == null || polygon == null || polygon.size() < 3) {
            return false;
        }
        
        Point jtsPoint = createPoint(point);
        Polygon jtsPolygon = createPolygon(polygon);
        
        return jtsPolygon.contains(jtsPoint);
    }
    
    /**
     * Calculate shortest distance from point to polygon boundary
     */
    public double distanceToPolygonBoundary(Coordinates point, List<Coordinates> polygon) {
        if (point == null || polygon == null || polygon.size() < 3) {
            return 0;
        }
        
        Point jtsPoint = createPoint(point);
        Polygon jtsPolygon = createPolygon(polygon);
        
        // Get distance in degrees, convert to meters
        double distanceDegrees = jtsPolygon.getBoundary().distance(jtsPoint);
        
        // Approximate conversion (at equator, 1 degree ≈ 111km)
        // For more accuracy, should use the point's latitude
        double latitudeRadians = Math.toRadians(point.getLatitude());
        double metersPerDegree = EARTH_RADIUS_METERS * Math.PI / 180 * Math.cos(latitudeRadians);
        
        return distanceDegrees * metersPerDegree;
    }
    
    /**
     * Calculate bearing from point1 to point2
     * 
     * @return bearing in degrees (0-360)
     */
    public double bearing(Coordinates from, Coordinates to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());
        
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - 
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
    
    /**
     * Calculate destination point given start point, bearing, and distance
     */
    public Coordinates destinationPoint(Coordinates start, double bearingDegrees, double distanceMeters) {
        double angularDistance = distanceMeters / EARTH_RADIUS_METERS;
        double bearingRadians = Math.toRadians(bearingDegrees);
        
        double lat1 = Math.toRadians(start.getLatitude());
        double lon1 = Math.toRadians(start.getLongitude());
        
        double lat2 = Math.asin(
            Math.sin(lat1) * Math.cos(angularDistance) +
            Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearingRadians)
        );
        
        double lon2 = lon1 + Math.atan2(
            Math.sin(bearingRadians) * Math.sin(angularDistance) * Math.cos(lat1),
            Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2)
        );
        
        return Coordinates.builder()
            .latitude(Math.toDegrees(lat2))
            .longitude(Math.toDegrees(lon2))
            .build();
    }
    
    /**
     * Calculate bounding box around a center point
     */
    public double[] boundingBox(Coordinates center, double radiusMeters) {
        Coordinates north = destinationPoint(center, 0, radiusMeters);
        Coordinates east = destinationPoint(center, 90, radiusMeters);
        Coordinates south = destinationPoint(center, 180, radiusMeters);
        Coordinates west = destinationPoint(center, 270, radiusMeters);
        
        return new double[] {
            south.getLatitude(),  // south
            west.getLongitude(),  // west
            north.getLatitude(),  // north
            east.getLongitude()   // east
        };
    }
    
    /**
     * Format distance for display
     */
    public String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0f m", meters);
        }
        return String.format("%.1f km", meters / 1000);
    }
    
    /**
     * Format duration for display
     */
    public String formatDuration(double seconds) {
        long totalSeconds = (long) seconds;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        }
        return String.format("%d min", minutes);
    }
    
    /**
     * Create JTS Point from Coordinates
     */
    private Point createPoint(Coordinates coords) {
        return GEOMETRY_FACTORY.createPoint(
            new Coordinate(coords.getLongitude(), coords.getLatitude())
        );
    }
    
    /**
     * Create JTS Polygon from list of Coordinates
     */
    private Polygon createPolygon(List<Coordinates> coords) {
        // Ensure polygon is closed
        List<Coordinates> polygonCoords = new java.util.ArrayList<>(coords);
        if (!coords.get(0).equals(coords.get(coords.size() - 1))) {
            polygonCoords.add(coords.get(0));
        }
        
        Coordinate[] jtsCoords = polygonCoords.stream()
            .map(c -> new Coordinate(c.getLongitude(), c.getLatitude()))
            .toArray(Coordinate[]::new);
        
        LinearRing ring = GEOMETRY_FACTORY.createLinearRing(jtsCoords);
        return GEOMETRY_FACTORY.createPolygon(ring);
    }
    
    /**
     * Validate coordinates are within valid range
     */
    public boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && 
               longitude >= -180 && longitude <= 180;
    }
    
    /**
     * Validate coordinates object
     */
    public boolean isValid(Coordinates coords) {
        return coords != null && 
               coords.getLatitude() != null && 
               coords.getLongitude() != null &&
               isValidCoordinates(coords.getLatitude(), coords.getLongitude());
    }
}
