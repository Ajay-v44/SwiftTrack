package com.swifttrack.map.util;

import com.swifttrack.map.dto.Coordinates;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for polyline encoding/decoding
 * 
 * Uses Google Polyline Encoding Algorithm
 * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Polyline Algorithm</a>
 */
@UtilityClass
public class PolylineUtils {
    
    private static final double PRECISION = 1E5;
    
    /**
     * Decode a polyline string into a list of coordinates
     */
    public List<Coordinates> decode(String encodedPolyline) {
        List<Coordinates> coordinates = new ArrayList<>();
        
        if (encodedPolyline == null || encodedPolyline.isEmpty()) {
            return coordinates;
        }
        
        int index = 0;
        int lat = 0;
        int lng = 0;
        
        while (index < encodedPolyline.length()) {
            // Decode latitude
            int result = 0;
            int shift = 0;
            int b;
            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;
            
            // Decode longitude
            result = 0;
            shift = 0;
            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;
            
            coordinates.add(Coordinates.builder()
                .latitude(lat / PRECISION)
                .longitude(lng / PRECISION)
                .build());
        }
        
        return coordinates;
    }
    
    /**
     * Encode a list of coordinates into a polyline string
     */
    public String encode(List<Coordinates> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        int prevLat = 0;
        int prevLng = 0;
        
        for (Coordinates coord : coordinates) {
            int lat = (int) Math.round(coord.getLatitude() * PRECISION);
            int lng = (int) Math.round(coord.getLongitude() * PRECISION);
            
            result.append(encodeValue(lat - prevLat));
            result.append(encodeValue(lng - prevLng));
            
            prevLat = lat;
            prevLng = lng;
        }
        
        return result.toString();
    }
    
    /**
     * Encode a single value
     */
    private String encodeValue(int value) {
        StringBuilder encoded = new StringBuilder();
        
        value = value < 0 ? ~(value << 1) : (value << 1);
        
        while (value >= 0x20) {
            encoded.append((char) ((0x20 | (value & 0x1f)) + 63));
            value >>= 5;
        }
        encoded.append((char) (value + 63));
        
        return encoded.toString();
    }
    
    /**
     * Simplify a polyline using Douglas-Peucker algorithm
     */
    public List<Coordinates> simplify(List<Coordinates> coordinates, double toleranceMeters) {
        if (coordinates == null || coordinates.size() <= 2) {
            return coordinates;
        }
        
        return douglasPeucker(coordinates, 0, coordinates.size() - 1, toleranceMeters);
    }
    
    private List<Coordinates> douglasPeucker(List<Coordinates> points, int start, int end, double tolerance) {
        if (end <= start + 1) {
            List<Coordinates> result = new ArrayList<>();
            result.add(points.get(start));
            if (end != start) {
                result.add(points.get(end));
            }
            return result;
        }
        
        double maxDist = 0;
        int maxIndex = start;
        
        Coordinates first = points.get(start);
        Coordinates last = points.get(end);
        
        for (int i = start + 1; i < end; i++) {
            double dist = perpendicularDistance(points.get(i), first, last);
            if (dist > maxDist) {
                maxDist = dist;
                maxIndex = i;
            }
        }
        
        if (maxDist > tolerance) {
            List<Coordinates> left = douglasPeucker(points, start, maxIndex, tolerance);
            List<Coordinates> right = douglasPeucker(points, maxIndex, end, tolerance);
            
            List<Coordinates> result = new ArrayList<>(left);
            result.addAll(right.subList(1, right.size())); // Avoid duplicate point
            return result;
        } else {
            List<Coordinates> result = new ArrayList<>();
            result.add(first);
            result.add(last);
            return result;
        }
    }
    
    private double perpendicularDistance(Coordinates point, Coordinates lineStart, Coordinates lineEnd) {
        // Approximate using euclidean distance for small areas
        double x = point.getLongitude();
        double y = point.getLatitude();
        double x1 = lineStart.getLongitude();
        double y1 = lineStart.getLatitude();
        double x2 = lineEnd.getLongitude();
        double y2 = lineEnd.getLatitude();
        
        double numerator = Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1);
        double denominator = Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
        
        if (denominator == 0) return 0;
        
        // Convert to approximate meters (rough estimate)
        double distDegrees = numerator / denominator;
        return distDegrees * 111000; // ~111km per degree
    }
    
    /**
     * Calculate the total distance of a polyline
     */
    public double calculateDistance(List<Coordinates> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return 0;
        }
        
        double totalDistance = 0;
        for (int i = 1; i < coordinates.size(); i++) {
            totalDistance += GeoUtils.distance(coordinates.get(i - 1), coordinates.get(i));
        }
        
        return totalDistance;
    }
    
    /**
     * Get the midpoint of a polyline
     */
    public Coordinates getMidpoint(List<Coordinates> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }
        if (coordinates.size() == 1) {
            return coordinates.get(0);
        }
        
        double totalDistance = calculateDistance(coordinates);
        double halfDistance = totalDistance / 2;
        
        double accumulated = 0;
        for (int i = 1; i < coordinates.size(); i++) {
            double segmentDistance = GeoUtils.distance(coordinates.get(i - 1), coordinates.get(i));
            accumulated += segmentDistance;
            
            if (accumulated >= halfDistance) {
                // Interpolate
                double overshoot = accumulated - halfDistance;
                double ratio = 1 - (overshoot / segmentDistance);
                
                Coordinates start = coordinates.get(i - 1);
                Coordinates end = coordinates.get(i);
                
                return Coordinates.builder()
                    .latitude(start.getLatitude() + (end.getLatitude() - start.getLatitude()) * ratio)
                    .longitude(start.getLongitude() + (end.getLongitude() - start.getLongitude()) * ratio)
                    .build();
            }
        }
        
        return coordinates.get(coordinates.size() / 2);
    }
}
