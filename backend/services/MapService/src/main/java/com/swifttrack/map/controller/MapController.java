package com.swifttrack.map.controller;

import com.swifttrack.map.dto.*;
import com.swifttrack.map.dto.request.*;
import com.swifttrack.map.dto.response.ApiResponse;
import com.swifttrack.map.dto.response.AreaCheckResponse;
import com.swifttrack.map.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Main Map Service REST Controller
 * 
 * Provides comprehensive geospatial APIs powered by OpenStreetMap
 */
@Slf4j
@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Tag(name = "Map Service", description = "Geospatial APIs for geocoding, routing, and navigation")
public class MapController {
    
    private final GeocodingService geocodingService;
    private final RoutingService routingService;
    private final MatrixService matrixService;
    private final EtaService etaService;
    private final SnapToRoadService snapToRoadService;
    private final ServiceabilityService serviceabilityService;
    
    // ==================== GEOCODING ====================
    
    @GetMapping("/reverse")
    @Operation(summary = "Reverse Geocoding", 
               description = "Convert latitude/longitude coordinates to a human-readable address")
    public ResponseEntity<ApiResponse<NormalizedLocation>> reverseGeocode(
            @Parameter(description = "Latitude", example = "12.9716") @RequestParam double lat,
            @Parameter(description = "Longitude", example = "77.5946") @RequestParam double lng) {
        
        log.info("Reverse geocode request: lat={}, lng={}", lat, lng);
        NormalizedLocation result = geocodingService.reverseGeocode(lat, lng);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Forward Geocoding", 
               description = "Convert an address or place name to coordinates")
    public ResponseEntity<ApiResponse<List<NormalizedLocation>>> search(
            @Parameter(description = "Address or place to search", example = "MG Road, Bangalore") 
            @RequestParam String query,
            @Parameter(description = "Maximum number of results (1-50)") 
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Search request: query={}, limit={}", query, limit);
        List<NormalizedLocation> results = geocodingService.search(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    // ==================== ROUTING ====================
    
    @PostMapping("/directions")
    @Operation(summary = "Get Directions", 
               description = "Calculate route with turn-by-turn directions between two points")
    public ResponseEntity<ApiResponse<RouteResponse>> getDirections(
            @Valid @RequestBody DirectionsRequest request) {
        
        log.info("Directions request: {} -> {} (mode={})", 
                 request.getOrigin(), request.getDestination(), request.getMode());
        RouteResponse result = routingService.getDirections(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/directions")
    @Operation(summary = "Get Directions (GET)", 
               description = "Simple directions query via GET parameters")
    public ResponseEntity<ApiResponse<RouteResponse>> getDirectionsGet(
            @Parameter(description = "Origin latitude") @RequestParam("origin_lat") double originLat,
            @Parameter(description = "Origin longitude") @RequestParam("origin_lng") double originLng,
            @Parameter(description = "Destination latitude") @RequestParam("dest_lat") double destLat,
            @Parameter(description = "Destination longitude") @RequestParam("dest_lng") double destLng,
            @Parameter(description = "Travel mode") @RequestParam(defaultValue = "DRIVING") String mode) {
        
        Coordinates origin = Coordinates.builder()
            .latitude(originLat).longitude(originLng).build();
        Coordinates destination = Coordinates.builder()
            .latitude(destLat).longitude(destLng).build();
        TravelMode travelMode = TravelMode.fromString(mode);
        
        RouteResponse result = routingService.getDirections(origin, destination, travelMode);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // ==================== DISTANCE MATRIX ====================
    
    @PostMapping("/matrix")
    @Operation(summary = "Distance Matrix", 
               description = "Calculate distances and durations between multiple origin-destination pairs")
    public ResponseEntity<ApiResponse<MatrixResponse>> calculateMatrix(
            @Valid @RequestBody MatrixRequest request) {
        
        log.info("Matrix request: {} origins x {} destinations", 
                 request.getOrigins().size(), request.getDestinations().size());
        MatrixResponse result = matrixService.calculateMatrix(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // ==================== ETA ====================
    
    @PostMapping("/eta")
    @Operation(summary = "ETA Calculation", 
               description = "Calculate estimated time of arrival with traffic considerations")
    public ResponseEntity<ApiResponse<EtaResponse>> calculateEta(
            @Valid @RequestBody EtaRequest request) {
        
        log.info("ETA request: {} -> {} (mode={})", 
                 request.getOrigin(), request.getDestination(), request.getMode());
        EtaResponse result = etaService.calculateEta(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/eta")
    @Operation(summary = "ETA Calculation (GET)", 
               description = "Simple ETA query via GET parameters")
    public ResponseEntity<ApiResponse<EtaResponse>> calculateEtaGet(
            @Parameter(description = "Origin latitude") @RequestParam("origin_lat") double originLat,
            @Parameter(description = "Origin longitude") @RequestParam("origin_lng") double originLng,
            @Parameter(description = "Destination latitude") @RequestParam("dest_lat") double destLat,
            @Parameter(description = "Destination longitude") @RequestParam("dest_lng") double destLng,
            @Parameter(description = "Travel mode") @RequestParam(defaultValue = "DRIVING") String mode) {
        
        Coordinates origin = Coordinates.builder()
            .latitude(originLat).longitude(originLng).build();
        Coordinates destination = Coordinates.builder()
            .latitude(destLat).longitude(destLng).build();
        TravelMode travelMode = TravelMode.fromString(mode);
        
        EtaResponse result = etaService.calculateEta(origin, destination, travelMode);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // ==================== SNAP TO ROAD ====================
    
    @PostMapping("/snap")
    @Operation(summary = "Snap to Road", 
               description = "Snap GPS coordinates to the nearest road segment")
    public ResponseEntity<ApiResponse<SnapToRoadResponse>> snapToRoad(
            @Valid @RequestBody SnapToRoadRequest request) {
        
        log.info("Snap request: {} points", request.getPath().size());
        SnapToRoadResponse result = snapToRoadService.snap(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/snap")
    @Operation(summary = "Snap Single Point", 
               description = "Snap a single GPS coordinate to the nearest road")
    public ResponseEntity<ApiResponse<SnappedPoint>> snapPoint(
            @Parameter(description = "Latitude") @RequestParam double lat,
            @Parameter(description = "Longitude") @RequestParam double lng,
            @Parameter(description = "Search radius in meters") @RequestParam(defaultValue = "50") double radius) {
        
        Coordinates point = Coordinates.builder().latitude(lat).longitude(lng).build();
        SnappedPoint result = snapToRoadService.snapPoint(point, radius);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // ==================== SERVICEABILITY ====================
    
    @PostMapping("/inside-area")
    @Operation(summary = "Serviceability Check", 
               description = "Check if a point is inside a defined service area polygon")
    public ResponseEntity<ApiResponse<AreaCheckResponse>> checkServiceability(
            @Valid @RequestBody AreaCheckRequest request) {
        
        log.info("Area check request: point={}", request.getPoint());
        AreaCheckResponse result = serviceabilityService.checkServiceability(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/inside-area")
    @Operation(summary = "Radius Check", 
               description = "Check if a point is within a circular service area")
    public ResponseEntity<ApiResponse<AreaCheckResponse>> checkRadiusServiceability(
            @Parameter(description = "Point latitude") @RequestParam("lat") double lat,
            @Parameter(description = "Point longitude") @RequestParam("lng") double lng,
            @Parameter(description = "Center latitude") @RequestParam("center_lat") double centerLat,
            @Parameter(description = "Center longitude") @RequestParam("center_lng") double centerLng,
            @Parameter(description = "Radius in meters") @RequestParam double radius) {
        
        Coordinates point = Coordinates.builder().latitude(lat).longitude(lng).build();
        Coordinates center = Coordinates.builder().latitude(centerLat).longitude(centerLng).build();
        
        AreaCheckResponse result = serviceabilityService.checkServiceabilityRadius(point, center, radius);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // ==================== UTILITY ====================
    
    @GetMapping("/distance")
    @Operation(summary = "Calculate Distance", 
               description = "Calculate straight-line distance between two points (Haversine)")
    public ResponseEntity<ApiResponse<DistanceResult>> calculateDistance(
            @Parameter(description = "Origin latitude") @RequestParam("origin_lat") double originLat,
            @Parameter(description = "Origin longitude") @RequestParam("origin_lng") double originLng,
            @Parameter(description = "Destination latitude") @RequestParam("dest_lat") double destLat,
            @Parameter(description = "Destination longitude") @RequestParam("dest_lng") double destLng) {
        
        Coordinates origin = Coordinates.builder()
            .latitude(originLat).longitude(originLng).build();
        Coordinates destination = Coordinates.builder()
            .latitude(destLat).longitude(destLng).build();
        
        double distance = matrixService.calculateStraightLineDistance(origin, destination);
        
        DistanceResult result = new DistanceResult(
            origin, destination, distance, 
            com.swifttrack.map.util.GeoUtils.formatDistance(distance)
        );
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Simple DTO for distance result
     */
    public record DistanceResult(
        Coordinates origin, 
        Coordinates destination, 
        double distanceMeters, 
        String distanceText
    ) {}
}
