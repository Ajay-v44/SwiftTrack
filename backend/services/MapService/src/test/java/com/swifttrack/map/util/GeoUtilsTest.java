package com.swifttrack.map.util;

import com.swifttrack.map.dto.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {
    
    @Test
    void testHaversineDistance() {
        // Test: Distance between two known points (Bangalore to Mumbai)
        double lat1 = 12.9716; // Bangalore
        double lon1 = 77.5946;
        double lat2 = 19.0760; // Mumbai
        double lon2 = 72.8777;
        
        double distance = GeoUtils.haversineDistance(lat1, lon1, lat2, lon2);
        
        // Expected distance is approximately 840-850 km
        assertTrue(distance > 800000 && distance < 900000, 
                   "Distance should be between 800km and 900km, got: " + distance/1000 + " km");
    }
    
    @Test
    void testDistance() {
        Coordinates p1 = Coordinates.builder().latitude(12.9716).longitude(77.5946).build();
        Coordinates p2 = Coordinates.builder().latitude(12.9816).longitude(77.6046).build();
        
        double distance = GeoUtils.distance(p1, p2);
        
        // Should be around 1.5 km
        assertTrue(distance > 1000 && distance < 2000, 
                   "Distance should be between 1km and 2km, got: " + distance + " m");
    }
    
    @Test
    void testDistanceNullInputs() {
        Coordinates p1 = Coordinates.builder().latitude(12.9716).longitude(77.5946).build();
        
        assertEquals(0, GeoUtils.distance(null, p1));
        assertEquals(0, GeoUtils.distance(p1, null));
        assertEquals(0, GeoUtils.distance(null, null));
    }
    
    @Test
    void testIsPointInsidePolygon() {
        // Create a square polygon around Bangalore center
        List<Coordinates> polygon = Arrays.asList(
            Coordinates.builder().latitude(12.95).longitude(77.55).build(),
            Coordinates.builder().latitude(12.95).longitude(77.65).build(),
            Coordinates.builder().latitude(13.05).longitude(77.65).build(),
            Coordinates.builder().latitude(13.05).longitude(77.55).build()
        );
        
        // Point inside
        Coordinates inside = Coordinates.builder().latitude(12.9716).longitude(77.5946).build();
        assertTrue(GeoUtils.isPointInsidePolygon(inside, polygon));
        
        // Point outside
        Coordinates outside = Coordinates.builder().latitude(12.8).longitude(77.5).build();
        assertFalse(GeoUtils.isPointInsidePolygon(outside, polygon));
    }
    
    @Test
    void testBearing() {
        Coordinates from = Coordinates.builder().latitude(12.9716).longitude(77.5946).build();
        Coordinates to = Coordinates.builder().latitude(12.9816).longitude(77.5946).build();
        
        double bearing = GeoUtils.bearing(from, to);
        
        // Should be approximately 0 degrees (north)
        assertTrue(bearing < 10 || bearing > 350, 
                   "Bearing should be approximately north (0 degrees), got: " + bearing);
    }
    
    @Test
    void testFormatDistance() {
        assertEquals("500 m", GeoUtils.formatDistance(500));
        assertEquals("1.0 km", GeoUtils.formatDistance(1000));
        assertEquals("10.5 km", GeoUtils.formatDistance(10500));
    }
    
    @Test
    void testFormatDuration() {
        assertEquals("5 min", GeoUtils.formatDuration(300));
        assertEquals("1 hr 30 min", GeoUtils.formatDuration(5400));
        assertEquals("2 hr 0 min", GeoUtils.formatDuration(7200));
    }
    
    @Test
    void testValidCoordinates() {
        assertTrue(GeoUtils.isValidCoordinates(12.9716, 77.5946));
        assertFalse(GeoUtils.isValidCoordinates(91, 77.5946));
        assertFalse(GeoUtils.isValidCoordinates(12.9716, 181));
        assertFalse(GeoUtils.isValidCoordinates(-91, 77.5946));
        assertFalse(GeoUtils.isValidCoordinates(12.9716, -181));
    }
    
    @Test
    void testIsValid() {
        Coordinates valid = Coordinates.builder().latitude(12.9716).longitude(77.5946).build();
        assertTrue(GeoUtils.isValid(valid));
        
        Coordinates invalidLat = Coordinates.builder().latitude(100.0).longitude(77.5946).build();
        assertFalse(GeoUtils.isValid(invalidLat));
        
        assertFalse(GeoUtils.isValid(null));
    }
    
    @Test
    void testBoundingBox() {
        Coordinates center = Coordinates.builder().latitude(12.9716).longitude(77.5946).build();
        
        double[] bbox = GeoUtils.boundingBox(center, 1000); // 1km radius
        
        assertEquals(4, bbox.length);
        assertTrue(bbox[0] < center.getLatitude()); // south
        assertTrue(bbox[1] < center.getLongitude()); // west
        assertTrue(bbox[2] > center.getLatitude()); // north
        assertTrue(bbox[3] > center.getLongitude()); // east
    }
}
