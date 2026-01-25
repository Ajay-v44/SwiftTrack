package com.swifttrack.OrderService.services;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.swifttrack.events.OrderCreatedEvent;
import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.OrderService.models.OrderAiFeature;
import com.swifttrack.OrderService.repositories.OrderAiFeatureRepository;
import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.events.DriverAssignedEvent;

import org.springframework.transaction.annotation.Transactional;
import com.swifttrack.OrderService.repositories.OrderRepository;
import com.swifttrack.OrderService.models.Order;

@Service
public class OrderEventConsumer {
    OrderAiFeatureRepository orderAiFeatureRepository;
    MapInterface mapInterface;
    OrderRepository orderRepository;

    OrderEventConsumer(OrderAiFeatureRepository orderAiFeatureRepository, MapInterface mapInterface,
            OrderRepository orderRepository) {
        this.orderAiFeatureRepository = orderAiFeatureRepository;
        this.mapInterface = mapInterface;
        this.orderRepository = orderRepository;
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
        } catch (Exception e) {
            System.err.println("Error saving AI features: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "driver-assigned", groupId = "order-service-group")
    public void handleDriverAssigned(DriverAssignedEvent event) {
        System.out.println("Driver Assigned for Order: " + event.getOrderId() + ", Driver: " + event.getDriverName());
        // Update order status or notify user
    }
}
