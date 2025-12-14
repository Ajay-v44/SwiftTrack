package com.swifttrack.map.service;

import com.swifttrack.map.config.CacheConfig;
import com.swifttrack.map.dto.*;
import com.swifttrack.map.dto.request.EtaRequest;
import com.swifttrack.map.exception.RoutingException;
import com.swifttrack.map.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Service for ETA (Estimated Time of Arrival) calculations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtaService {
    
    private final RoutingService routingService;
    private final MatrixService matrixService;
    
    /**
     * Calculate ETA between two points
     * Results are cached for 10 minutes
     */
    @Cacheable(value = CacheConfig.ETA_CACHE, key = "T(com.swifttrack.map.util.CacheKeyGenerator).forEta(#origin, #destination, #mode.name())")
    public EtaResponse calculateEta(Coordinates origin, Coordinates destination, TravelMode mode) {
        log.info("Calculating ETA: {} -> {} (mode={})", origin, destination, mode);
        
        validateCoordinates(origin, "Origin");
        validateCoordinates(destination, "Destination");
        
        // Get route to calculate accurate ETA
        RouteResponse route = routingService.getDirections(origin, destination, mode);
        
        if (route == null || route.getStatus() != RouteResponse.RouteStatus.OK) {
            return EtaResponse.builder()
                .status(EtaResponse.EtaStatus.NOT_FOUND)
                .errorMessage(route != null ? route.getErrorMessage() : "Route not found")
                .origin(origin)
                .destination(destination)
                .travelMode(mode)
                .build();
        }
        
        // Apply traffic multiplier
        double baseDuration = route.getDurationSeconds();
        double trafficMultiplier = getTrafficMultiplier(mode);
        long adjustedDuration = Math.round(baseDuration * trafficMultiplier);
        
        // Calculate ETA range
        long etaMin = Math.round(baseDuration * 0.9); // 10% faster
        long etaMax = Math.round(baseDuration * trafficMultiplier * 1.2); // 20% slower with traffic
        
        // Calculate arrival time
        Instant departureTime = Instant.now();
        Instant arrivalTime = departureTime.plusSeconds(adjustedDuration);
        
        return EtaResponse.builder()
            .status(EtaResponse.EtaStatus.OK)
            .origin(origin)
            .destination(destination)
            .travelMode(mode)
            .durationSeconds(adjustedDuration)
            .durationText(GeoUtils.formatDuration(adjustedDuration))
            .distanceMeters(route.getDistanceMeters())
            .distanceText(route.getDistanceText())
            .departureTime(departureTime)
            .estimatedArrivalTime(arrivalTime)
            .trafficCondition(getTrafficCondition())
            .confidence(calculateConfidence(mode))
            .etaMinSeconds(etaMin)
            .etaMaxSeconds(etaMax)
            .build();
    }
    
    /**
     * Calculate ETA from request DTO
     */
    public EtaResponse calculateEta(EtaRequest request) {
        TravelMode mode = request.getMode() != null ? request.getMode() : TravelMode.DRIVING;
        
        EtaResponse response = calculateEta(request.getOrigin(), request.getDestination(), mode);
        
        // Adjust for custom departure time
        if (request.getDepartureTime() != null && response.getStatus() == EtaResponse.EtaStatus.OK) {
            response.setDepartureTime(request.getDepartureTime());
            response.setEstimatedArrivalTime(
                request.getDepartureTime().plusSeconds(response.getDurationSeconds())
            );
        }
        
        // Add pickup time if applicable
        if (Boolean.TRUE.equals(request.getIncludePickupTime()) && 
            response.getStatus() == EtaResponse.EtaStatus.OK) {
            
            Long pickupTime = request.getPickupTimeSeconds();
            if (pickupTime == null) {
                pickupTime = getDefaultPickupTime(mode);
            }
            
            response.setPickupTimeSeconds(pickupTime);
            response.setIncludesPickupTime(true);
            response.setDurationSeconds(response.getDurationSeconds() + pickupTime);
            response.setDurationText(GeoUtils.formatDuration(response.getDurationSeconds()));
            
            if (response.getEstimatedArrivalTime() != null) {
                response.setEstimatedArrivalTime(
                    response.getEstimatedArrivalTime().plusSeconds(pickupTime)
                );
            }
        }
        
        // Store provider ID if provided
        if (request.getProviderId() != null) {
            response.setProviderId(request.getProviderId());
        }
        
        return response;
    }
    
    /**
     * Calculate ETA for delivery mode with multiple stops
     */
    public EtaResponse calculateDeliveryEta(Coordinates providerLocation, 
                                            Coordinates pickupLocation,
                                            Coordinates deliveryLocation) {
        log.info("Calculating delivery ETA: provider={} -> pickup={} -> delivery={}", 
                 providerLocation, pickupLocation, deliveryLocation);
        
        // Calculate time to pickup
        EtaResponse toPickup = calculateEta(providerLocation, pickupLocation, TravelMode.DELIVERY);
        if (toPickup.getStatus() != EtaResponse.EtaStatus.OK) {
            return toPickup;
        }
        
        // Calculate time from pickup to delivery
        EtaResponse toDelivery = calculateEta(pickupLocation, deliveryLocation, TravelMode.DELIVERY);
        if (toDelivery.getStatus() != EtaResponse.EtaStatus.OK) {
            return toDelivery;
        }
        
        // Add pickup time
        long pickupTimeSeconds = getDefaultPickupTime(TravelMode.DELIVERY);
        
        // Total ETA
        long totalDuration = toPickup.getDurationSeconds() + pickupTimeSeconds + toDelivery.getDurationSeconds();
        double totalDistance = toPickup.getDistanceMeters() + toDelivery.getDistanceMeters();
        
        Instant departureTime = Instant.now();
        Instant arrivalTime = departureTime.plusSeconds(totalDuration);
        
        return EtaResponse.builder()
            .status(EtaResponse.EtaStatus.OK)
            .origin(providerLocation)
            .destination(deliveryLocation)
            .travelMode(TravelMode.DELIVERY)
            .durationSeconds(totalDuration)
            .durationText(GeoUtils.formatDuration(totalDuration))
            .distanceMeters(totalDistance)
            .distanceText(GeoUtils.formatDistance(totalDistance))
            .departureTime(departureTime)
            .estimatedArrivalTime(arrivalTime)
            .trafficCondition(getTrafficCondition())
            .confidence(0.75)
            .pickupTimeSeconds(pickupTimeSeconds)
            .includesPickupTime(true)
            .etaMinSeconds(Math.round(totalDuration * 0.85))
            .etaMaxSeconds(Math.round(totalDuration * 1.3))
            .build();
    }
    
    /**
     * Quick ETA estimation based on straight-line distance (fallback)
     */
    public EtaResponse estimateEtaQuick(Coordinates origin, Coordinates destination, TravelMode mode) {
        double distance = GeoUtils.distance(origin, destination);
        double durationSeconds = distance / mode.getAverageSpeedMps();
        
        // Apply road factor (roads are rarely straight)
        double roadFactor = 1.4; // Roads are typically 40% longer than straight line
        double adjustedDistance = distance * roadFactor;
        double adjustedDuration = durationSeconds * roadFactor * mode.getTrafficMultiplier();
        
        return EtaResponse.builder()
            .status(EtaResponse.EtaStatus.OK)
            .origin(origin)
            .destination(destination)
            .travelMode(mode)
            .durationSeconds((long) adjustedDuration)
            .durationText(GeoUtils.formatDuration(adjustedDuration))
            .distanceMeters(adjustedDistance)
            .distanceText(GeoUtils.formatDistance(adjustedDistance))
            .departureTime(Instant.now())
            .estimatedArrivalTime(Instant.now().plusSeconds((long) adjustedDuration))
            .trafficCondition(EtaResponse.TrafficCondition.UNKNOWN)
            .confidence(0.5) // Lower confidence for estimation
            .build();
    }
    
    /**
     * Get traffic multiplier based on time of day
     */
    private double getTrafficMultiplier(TravelMode mode) {
        if (mode == TravelMode.WALKING || mode == TravelMode.BIKE) {
            return 1.0; // No traffic impact
        }
        
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        int hour = now.getHour();
        
        // Peak hours: 8-10 AM and 5-8 PM
        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) {
            return 1.5; // 50% longer during peak
        }
        // Moderate traffic
        else if ((hour >= 7 && hour < 8) || (hour > 10 && hour < 17) || (hour > 20 && hour <= 22)) {
            return 1.2;
        }
        // Light traffic (night)
        else {
            return 1.0;
        }
    }
    
    /**
     * Get current traffic condition based on time
     */
    private EtaResponse.TrafficCondition getTrafficCondition() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        int hour = now.getHour();
        
        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) {
            return EtaResponse.TrafficCondition.HEAVY;
        } else if ((hour >= 7 && hour < 8) || (hour > 10 && hour < 17)) {
            return EtaResponse.TrafficCondition.NORMAL;
        } else {
            return EtaResponse.TrafficCondition.LIGHT;
        }
    }
    
    /**
     * Calculate confidence based on mode and conditions
     */
    private double calculateConfidence(TravelMode mode) {
        // Walking and biking are more predictable
        return switch (mode) {
            case WALKING -> 0.95;
            case BIKE -> 0.90;
            case DRIVING -> 0.80;
            case DELIVERY -> 0.70;
        };
    }
    
    /**
     * Get default pickup time for different modes
     */
    private long getDefaultPickupTime(TravelMode mode) {
        return switch (mode) {
            case DELIVERY -> 300; // 5 minutes
            case DRIVING -> 60;   // 1 minute
            default -> 0;
        };
    }
    
    /**
     * Validate coordinates
     */
    private void validateCoordinates(Coordinates coords, String name) {
        if (coords == null) {
            throw new RoutingException(name + " coordinates cannot be null");
        }
        if (!coords.isValid()) {
            throw new RoutingException(name + " has invalid coordinate values");
        }
    }
}
