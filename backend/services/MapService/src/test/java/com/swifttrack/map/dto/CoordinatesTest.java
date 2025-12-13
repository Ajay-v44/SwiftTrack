package com.swifttrack.map.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {
    
    @Test
    void testFromString() {
        Coordinates coords = Coordinates.fromString("12.9716,77.5946");
        
        assertNotNull(coords);
        assertEquals(12.9716, coords.getLatitude());
        assertEquals(77.5946, coords.getLongitude());
    }
    
    @Test
    void testFromStringWithSpaces() {
        Coordinates coords = Coordinates.fromString("  12.9716 , 77.5946  ");
        
        assertNotNull(coords);
        assertEquals(12.9716, coords.getLatitude());
        assertEquals(77.5946, coords.getLongitude());
    }
    
    @Test
    void testFromStringNull() {
        assertNull(Coordinates.fromString(null));
        assertNull(Coordinates.fromString(""));
        assertNull(Coordinates.fromString("   "));
    }
    
    @Test
    void testFromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> 
            Coordinates.fromString("invalid"));
        assertThrows(IllegalArgumentException.class, () -> 
            Coordinates.fromString("12.9716"));
    }
    
    @Test
    void testToOsrmFormat() {
        Coordinates coords = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        // OSRM format is lng,lat
        assertEquals("77.5946,12.9716", coords.toOsrmFormat());
    }
    
    @Test
    void testToStandardFormat() {
        Coordinates coords = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        assertEquals("12.9716,77.5946", coords.toStandardFormat());
    }
    
    @Test
    void testDistanceTo() {
        Coordinates bangalore = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        Coordinates mumbai = Coordinates.builder()
            .latitude(19.0760)
            .longitude(72.8777)
            .build();
        
        double distance = bangalore.distanceTo(mumbai);
        
        // Distance should be approximately 840-850 km
        assertTrue(distance > 800000 && distance < 900000,
                   "Distance should be approx 840km, got: " + distance/1000 + " km");
    }
    
    @Test
    void testDistanceToNull() {
        Coordinates coords = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        assertEquals(0, coords.distanceTo(null));
    }
    
    @Test
    void testIsValid() {
        // Valid coordinates
        Coordinates valid = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        assertTrue(valid.isValid());
        
        // Invalid latitude (> 90)
        Coordinates invalidLat = Coordinates.builder()
            .latitude(100.0)
            .longitude(77.5946)
            .build();
        assertFalse(invalidLat.isValid());
        
        // Invalid longitude (> 180)
        Coordinates invalidLng = Coordinates.builder()
            .latitude(12.9716)
            .longitude(200.0)
            .build();
        assertFalse(invalidLng.isValid());
        
        // Null values
        Coordinates nullLat = Coordinates.builder()
            .longitude(77.5946)
            .build();
        assertFalse(nullLat.isValid());
    }
    
    @Test
    void testBuilder() {
        Coordinates coords = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        assertNotNull(coords);
        assertEquals(12.9716, coords.getLatitude());
        assertEquals(77.5946, coords.getLongitude());
    }
}
