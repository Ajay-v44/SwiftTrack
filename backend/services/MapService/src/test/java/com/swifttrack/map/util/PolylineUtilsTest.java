package com.swifttrack.map.util;

import com.swifttrack.map.dto.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolylineUtilsTest {
    
    @Test
    void testDecodePolyline() {
        // Test with a known encoded polyline
        String encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
        
        List<Coordinates> decoded = PolylineUtils.decode(encoded);
        
        assertNotNull(decoded);
        assertFalse(decoded.isEmpty());
        assertEquals(3, decoded.size());
        
        // First point should be approximately (38.5, -120.2)
        assertEquals(38.5, decoded.get(0).getLatitude(), 0.01);
        assertEquals(-120.2, decoded.get(0).getLongitude(), 0.01);
    }
    
    @Test
    void testEncodePolyline() {
        List<Coordinates> coords = Arrays.asList(
            Coordinates.builder().latitude(38.5).longitude(-120.2).build(),
            Coordinates.builder().latitude(40.7).longitude(-120.95).build(),
            Coordinates.builder().latitude(43.252).longitude(-126.453).build()
        );
        
        String encoded = PolylineUtils.encode(coords);
        
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        
        // Decode and verify
        List<Coordinates> decoded = PolylineUtils.decode(encoded);
        assertEquals(coords.size(), decoded.size());
        
        for (int i = 0; i < coords.size(); i++) {
            assertEquals(coords.get(i).getLatitude(), decoded.get(i).getLatitude(), 0.00001);
            assertEquals(coords.get(i).getLongitude(), decoded.get(i).getLongitude(), 0.00001);
        }
    }
    
    @Test
    void testDecodeEmptyPolyline() {
        List<Coordinates> result = PolylineUtils.decode("");
        assertTrue(result.isEmpty());
        
        result = PolylineUtils.decode(null);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testEncodeEmptyList() {
        String result = PolylineUtils.encode(null);
        assertEquals("", result);
        
        result = PolylineUtils.encode(List.of());
        assertEquals("", result);
    }
    
    @Test
    void testCalculateDistance() {
        List<Coordinates> path = Arrays.asList(
            Coordinates.builder().latitude(12.9716).longitude(77.5946).build(),
            Coordinates.builder().latitude(12.9816).longitude(77.6046).build(),
            Coordinates.builder().latitude(12.9916).longitude(77.6146).build()
        );
        
        double distance = PolylineUtils.calculateDistance(path);
        
        assertTrue(distance > 0);
        // Expected approximately 3 km (rough estimate)
        assertTrue(distance > 2000 && distance < 5000);
    }
    
    @Test
    void testCalculateDistanceEmptyPath() {
        assertEquals(0, PolylineUtils.calculateDistance(null));
        assertEquals(0, PolylineUtils.calculateDistance(List.of()));
        
        List<Coordinates> singlePoint = List.of(
            Coordinates.builder().latitude(12.9716).longitude(77.5946).build()
        );
        assertEquals(0, PolylineUtils.calculateDistance(singlePoint));
    }
    
    @Test
    void testGetMidpoint() {
        List<Coordinates> path = Arrays.asList(
            Coordinates.builder().latitude(12.0).longitude(77.0).build(),
            Coordinates.builder().latitude(13.0).longitude(78.0).build()
        );
        
        Coordinates midpoint = PolylineUtils.getMidpoint(path);
        
        assertNotNull(midpoint);
        // Midpoint should be approximately at (12.5, 77.5)
        assertTrue(midpoint.getLatitude() > 12.4 && midpoint.getLatitude() < 12.6);
        assertTrue(midpoint.getLongitude() > 77.4 && midpoint.getLongitude() < 77.6);
    }
    
    @Test
    void testSimplify() {
        // Create a complex path
        List<Coordinates> path = Arrays.asList(
            Coordinates.builder().latitude(12.0).longitude(77.0).build(),
            Coordinates.builder().latitude(12.0001).longitude(77.0001).build(),
            Coordinates.builder().latitude(12.0002).longitude(77.0002).build(),
            Coordinates.builder().latitude(12.5).longitude(77.5).build(),
            Coordinates.builder().latitude(12.5001).longitude(77.5001).build(),
            Coordinates.builder().latitude(13.0).longitude(78.0).build()
        );
        
        List<Coordinates> simplified = PolylineUtils.simplify(path, 1000);
        
        assertNotNull(simplified);
        // Simplified path should have fewer points
        assertTrue(simplified.size() <= path.size());
        assertTrue(simplified.size() >= 2);
    }
}
