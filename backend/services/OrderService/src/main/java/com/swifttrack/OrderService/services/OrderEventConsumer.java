package com.swifttrack.OrderService.services;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import com.swifttrack.events.OrderCreatedEvent;
import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.OrderService.models.OrderAiFeature;
import com.swifttrack.OrderService.models.enums.OrderStatus;
import com.swifttrack.OrderService.repositories.OrderAiFeatureRepository;
import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.events.DriverAssignedEvent;

import org.springframework.transaction.annotation.Transactional;
import com.swifttrack.OrderService.repositories.OrderRepository;
import com.swifttrack.OrderService.models.Order;
import com.swifttrack.OrderService.repositories.OrderLocationRepository;
import com.swifttrack.OrderService.models.OrderLocation;
import com.swifttrack.OrderService.models.enums.LocationType;
import com.swifttrack.events.DriverLocationUpdates;
import com.swifttrack.OrderService.repositories.OrderTrackingStateRepository;
import com.swifttrack.OrderService.repositories.OrderTrackingEventRepository;
import com.swifttrack.OrderService.models.enums.TrackingStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderEventConsumer {
    OrderAiFeatureRepository orderAiFeatureRepository;
    MapInterface mapInterface;
    OrderRepository orderRepository;
    OrderLocationRepository orderLocationRepository;
    OrderTrackingStateRepository orderTrackingStateRepository;
    OrderTrackingEventRepository orderTrackingEventRepository;

    OrderEventConsumer(OrderAiFeatureRepository orderAiFeatureRepository, MapInterface mapInterface,
            OrderRepository orderRepository, OrderLocationRepository orderLocationRepository,
            OrderTrackingStateRepository orderTrackingStateRepository,
            OrderTrackingEventRepository orderTrackingEventRepository) {
        this.orderAiFeatureRepository = orderAiFeatureRepository;
        this.mapInterface = mapInterface;
        this.orderRepository = orderRepository;
        this.orderLocationRepository = orderLocationRepository;
        this.orderTrackingStateRepository = orderTrackingStateRepository;
        this.orderTrackingEventRepository = orderTrackingEventRepository;
    }

    @KafkaListener(topics = "order-created", groupId = "order-service-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("AI Features Logic Triggered for Order: " + event.getOrderId());
        OrderAiFeature orderAiFeature = new OrderAiFeature();
        ApiResponse<DistanceResult> distanceResponse = mapInterface.calculateDistance(event.getPickupLat(),
                event.getPickupLng(), event.getDropoffLat(), event.getDropoffLng());

        // Fetch order reference to satisfy relationship
        try {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));
            orderAiFeature.setOrder(order);
            // orderId is automatically set by @MapsId via setOrder, but explicit set
            // doesn't hurt if handled in model
            orderAiFeature.setOrderId(event.getOrderId());

            BigDecimal distanceMeters = BigDecimal.valueOf(distanceResponse.getData().getDistanceMeters());
            BigDecimal distanceKm = distanceMeters.divide(BigDecimal.valueOf(1000), 2, java.math.RoundingMode.HALF_UP);
            orderAiFeature.setDistanceKm(distanceKm);

            orderAiFeatureRepository.save(orderAiFeature);

            // Save Order Locations
            saveLocation(order, LocationType.PICKUP, event.getPickupLat(), event.getPickupLng(),
                    event.getPickupCity(), event.getPickupState(), event.getPickupCountry(),
                    event.getPickupPincode(), event.getPickupLocality());

            saveLocation(order, LocationType.DROP, event.getDropoffLat(), event.getDropoffLng(),
                    event.getDropCity(), event.getDropState(), event.getDropCountry(),
                    event.getDropPincode(), event.getDropLocality());

        } catch (Exception e) {
            System.err.println("Error processing order created event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "driver-assigned", groupId = "order-service-group")
    @CacheEvict(value = { "orderStatus", "orders" }, key = "#event.orderId")
    public void handleDriverAssigned(DriverAssignedEvent event) {
        System.out.println("Driver Assigned for Order: " + event.getOrderId() + ", Driver: " + event.getDriverName());
        // Update order status and notify user
        try {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));
            order.setOrderStatus(OrderStatus.ASSIGNED);
            orderRepository.save(order);
        } catch (Exception e) {
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @KafkaListener(topics = "driver-canceled", groupId = "order-service-group")
    @CacheEvict(value = { "orderStatus", "orders" }, key = "#orderId")
    public void handleDriverCanceled(UUID orderId) {
        System.out.println("Driver Canceled for Order: " + orderId);
        // Update order status and notify user
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            order.setOrderStatus(OrderStatus.CREATED);
            orderRepository.save(order);
        } catch (Exception e) {
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "driver-location-updates", groupId = "order-service-group")
    @CacheEvict(value = { "orderStatus", "orders" }, key = "#event.orderId")
    public void handleDriverLocationUpdate(DriverLocationUpdates event) {
        System.out.println("Processing Driver Location Update for Order: " + event.getOrderId());
        try {
            // Validate Status
            TrackingStatus status;
            try {
                status = TrackingStatus.valueOf(event.getStatus());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid tracking status received: " + event.getStatus());
                return;
            }

            // 1. Update Tracking State (Real-time)
            updateTrackingState(event, status);

            // 2. Add Tracking Event (Historical Log - Only once per status type)
            addTrackingEvent(event, status);

        } catch (Exception e) {
            System.err.println("Error processing driver location update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTrackingState(DriverLocationUpdates event, TrackingStatus status) {
        try {
            // Use atomic upsert - handles both insert and update in a single query
            // This prevents race conditions when multiple threads try to insert
            // simultaneously
            int affectedRows = orderTrackingStateRepository.upsertTrackingState(
                    event.getOrderId(),
                    status.name(), // Native query needs String, not enum
                    event.getLatitude(),
                    event.getLongitude(),
                    LocalDateTime.now());

            System.out.println("Order ID: " + event.getOrderId());
            System.out.println("Status: " + status);
            System.out.println("Latitude: " + event.getLatitude());
            System.out.println("Longitude: " + event.getLongitude());
            System.out.println("Upsert affected rows: " + affectedRows);

            // Atomic Update for Order Status
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.name());
                orderRepository.updateOrderStatus(event.getOrderId(), orderStatus);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid tracking status received: " + status);
                System.err.println("Error updating order status: " + e.getMessage());
                // Ignore invalid status mapping
            }

        } catch (Exception e) {
            System.err.println("Error updating tracking state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addTrackingEvent(DriverLocationUpdates event, TrackingStatus status) {
        try {
            // Use atomic insert that checks for existence without loading Order entity
            // This avoids conflicts with OrderTrackingState due to JPA session
            int inserted = orderTrackingEventRepository.insertIfNotExists(
                    java.util.UUID.randomUUID(), // Generate new ID
                    event.getOrderId(),
                    status.name(), // Native query needs String
                    event.getLatitude(),
                    event.getLongitude(),
                    LocalDateTime.now(),
                    "Status updated to " + status,
                    LocalDateTime.now());

            if (inserted > 0) {
                System.out.println("Added new tracking event: " + status + " for order " + event.getOrderId());
            }
        } catch (Exception e) {
            System.err.println("Error adding tracking event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveLocation(Order order, LocationType type, Double lat, Double lng,
            String city, String state, String country, String pincode, String locality) {
        try {
            OrderLocation location = new OrderLocation();
            location.setOrder(order);
            location.setLocationType(type);

            if (lat != null)
                location.setLatitude(BigDecimal.valueOf(lat));
            if (lng != null)
                location.setLongitude(BigDecimal.valueOf(lng));

            location.setCity(city);
            location.setState(state);
            location.setCountry(country);
            location.setPincode(pincode);
            location.setLocality(locality);

            orderLocationRepository.save(location);
        } catch (Exception e) {
            System.err.println("Error saving " + type + " location: " + e.getMessage());
            // Consume exception to ensure other parts (like AI features) or subsequent
            // steps aren't affected by a single location failure
        }
    }
}
