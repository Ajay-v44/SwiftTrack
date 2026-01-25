package com.swifttrack.ProviderService.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.swifttrack.events.OrderCreatedEvent;
import com.swifttrack.events.DriverAssignedEvent;
import java.util.UUID;

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
}
