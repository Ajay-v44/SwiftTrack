package com.swifttrack.ProviderService.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.swifttrack.events.OrderCreatedEvent;
import com.swifttrack.events.DriverAssignedEvent;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ProviderEventConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProviderEventConsumer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-created", groupId = "provider-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("Provider Service Received OrderCreatedEvent: " + event.getOrderId());
        // Logic to assign driver
        assignDriver(event);

        // Trigger simulation
        CompletableFuture.runAsync(() -> driverLocationUpdate(event));
    }

    private void assignDriver(OrderCreatedEvent event) {
        // Mock logic to assign a driver
        System.out.println("Assigning driver for order: " + event.getOrderId());

        DriverAssignedEvent driverEvent = DriverAssignedEvent.builder()
                .orderId(event.getOrderId())
                .driverName("John Doe")
                .driverPhone("1234567890")
                .vehicleNumber("KA-01-AB-1234")
                .providerCode(event.getProviderCode())
                .build();

        kafkaTemplate.send("driver-assigned", driverEvent);
        System.out.println("Driver Assigned Event Sent for Order: " + event.getOrderId());
    }

    private void driverLocationUpdate(OrderCreatedEvent event) {
        try {
            // 1. Wait 10 seconds after assignment
            TimeUnit.SECONDS.sleep(60);

            // Send PICKED_UP
            sendUpdate(event.getOrderId(), "PICKED_UP", event.getPickupLat(), event.getPickupLng());
            System.out.println("Sent PICKED_UP for order: " + event.getOrderId());

            // 2. Wait 5 seconds
            TimeUnit.SECONDS.sleep(60);

            // 3. IN_TRANSIT simulation (Pickup -> Dropoff)
            double startLat = event.getPickupLat();
            double startLng = event.getPickupLng();
            double endLat = event.getDropoffLat();
            double endLng = event.getDropoffLng();

            int steps = 3; // 10 updates
            for (int i = 1; i <= steps; i++) {
                double fraction = (double) i / steps;
                double currentLat = startLat + (endLat - startLat) * fraction;
                double currentLng = startLng + (endLng - startLng) * fraction;

                sendUpdate(event.getOrderId(), "IN_TRANSIT", currentLat, currentLng);
                System.out.println("Sent IN_TRANSIT step " + i + " for order: " + event.getOrderId());

                // Wait 1 second between updates
                TimeUnit.SECONDS.sleep(60);
            }

            // 4. Wait 5 seconds
            TimeUnit.SECONDS.sleep(60);

            // Send DELIVERED
            sendUpdate(event.getOrderId(), "DELIVERED", event.getDropoffLat(), event.getDropoffLng());
            System.out.println("Sent DELIVERED for order: " + event.getOrderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Driver simulation interrupted for order: " + event.getOrderId());
        } catch (Exception e) {
            System.err.println("Error in driver simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendUpdate(UUID orderId, String status, Double lat, Double lng) {
        com.swifttrack.events.DriverLocationUpdates update = com.swifttrack.events.DriverLocationUpdates.builder()
                .orderId(orderId)
                .status(status)
                .latitude(lat != null ? BigDecimal.valueOf(lat) : null)
                .longitude(lng != null ? BigDecimal.valueOf(lng) : null)
                .build();
        kafkaTemplate.send("driver-location-updates", update);
    }
}
