package com.swifttrack.map.service;

import com.swifttrack.map.dto.Coordinates;
import com.swifttrack.map.dto.response.AreaCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceabilityServiceTest {
    
    private ServiceabilityService service;
    private List<Coordinates> testPolygon;
    
    @BeforeEach
    void setUp() {
        service = new ServiceabilityService();
        
        // Create a square polygon around Bangalore center
        testPolygon = Arrays.asList(
            Coordinates.builder().latitude(12.9).longitude(77.5).build(),
            Coordinates.builder().latitude(12.9).longitude(77.7).build(),
            Coordinates.builder().latitude(13.1).longitude(77.7).build(),
            Coordinates.builder().latitude(13.1).longitude(77.5).build()
        );
    }
    
    @Test
    void testPointInsidePolygon() {
        Coordinates inside = Coordinates.builder()
            .latitude(13.0)
            .longitude(77.6)
            .build();
        
        AreaCheckResponse response = service.checkServiceability(inside, testPolygon);
        
        assertNotNull(response);
        assertTrue(response.isInside());
        assertTrue(response.isServiceable());
    }
    
    @Test
    void testPointOutsidePolygon() {
        Coordinates outside = Coordinates.builder()
            .latitude(12.8)
            .longitude(77.4)
            .build();
        
        AreaCheckResponse response = service.checkServiceability(outside, testPolygon);
        
        assertNotNull(response);
        assertFalse(response.isInside());
        assertFalse(response.isServiceable());
        assertNotNull(response.getDistanceToBoundaryMeters());
        assertTrue(response.getDistanceToBoundaryMeters() > 0);
    }
    
    @Test
    void testIsPointServiceable() {
        Coordinates inside = Coordinates.builder()
            .latitude(13.0)
            .longitude(77.6)
            .build();
        
        Coordinates outside = Coordinates.builder()
            .latitude(12.8)
            .longitude(77.4)
            .build();
        
        assertTrue(service.isPointServiceable(inside, testPolygon));
        assertFalse(service.isPointServiceable(outside, testPolygon));
    }
    
    @Test
    void testRadiusServiceability() {
        Coordinates center = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        Coordinates inside = Coordinates.builder()
            .latitude(12.9720)
            .longitude(77.5950)
            .build();
        
        AreaCheckResponse response = service.checkServiceabilityRadius(inside, center, 1000);
        
        assertNotNull(response);
        assertTrue(response.isInside());
        assertTrue(response.isServiceable());
    }
    
    @Test
    void testRadiusServiceabilityOutside() {
        Coordinates center = Coordinates.builder()
            .latitude(12.9716)
            .longitude(77.5946)
            .build();
        
        Coordinates outside = Coordinates.builder()
            .latitude(12.99)
            .longitude(77.62)
            .build();
        
        AreaCheckResponse response = service.checkServiceabilityRadius(outside, center, 1000);
        
        assertNotNull(response);
        assertFalse(response.isInside());
        assertFalse(response.isServiceable());
    }
    
    @Test
    void testCheckMultiplePoints() {
        List<Coordinates> points = Arrays.asList(
            Coordinates.builder().latitude(13.0).longitude(77.6).build(),
            Coordinates.builder().latitude(12.8).longitude(77.4).build()
        );
        
        List<AreaCheckResponse> responses = service.checkMultiplePoints(points, testPolygon);
        
        assertEquals(2, responses.size());
        assertTrue(responses.get(0).isInside());
        assertFalse(responses.get(1).isInside());
    }
    
    @Test
    void testIsRouteServiceable() {
        List<Coordinates> insideRoute = Arrays.asList(
            Coordinates.builder().latitude(12.95).longitude(77.55).build(),
            Coordinates.builder().latitude(13.0).longitude(77.6).build(),
            Coordinates.builder().latitude(13.05).longitude(77.65).build()
        );
        
        List<Coordinates> partiallyOutsideRoute = Arrays.asList(
            Coordinates.builder().latitude(13.0).longitude(77.6).build(),
            Coordinates.builder().latitude(12.8).longitude(77.4).build()
        );
        
        assertTrue(service.isRouteServiceable(insideRoute, testPolygon));
        assertFalse(service.isRouteServiceable(partiallyOutsideRoute, testPolygon));
    }
    
    @Test
    void testGetRouteServiceabilityPercentage() {
        List<Coordinates> partialRoute = Arrays.asList(
            Coordinates.builder().latitude(13.0).longitude(77.6).build(),
            Coordinates.builder().latitude(12.8).longitude(77.4).build()
        );
        
        double percentage = service.getRouteServiceabilityPercentage(partialRoute, testPolygon);
        
        // One point inside, one outside = 50%
        assertEquals(50.0, percentage, 0.1);
    }
}
