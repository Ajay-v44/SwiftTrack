package com.swifttrack.services.notification;

import com.swifttrack.events.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumer {

    private final FirebaseService firebaseService;

    public NotificationEventConsumer(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @KafkaListener(topics = "order-created", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("NotificationService: processing OrderCreatedEvent for order: " + event.getOrderId());
        if (event.getTenantId() != null) {
            String topic = "tenant_" + event.getTenantId();
            String title = "New Order Created";
            String body = "Order " + event.getOrderId() + " has been successfully created. Amount: " + event.getAmount();
            firebaseService.sendNotificationToTopic(topic, title, body);
        }
    }

    @KafkaListener(topics = "driver-assigned", groupId = "notification-service-group")
    public void handleDriverAssigned(DriverAssignedEvent event) {
        System.out.println("NotificationService: processing DriverAssignedEvent for order: " + event.getOrderId());
        // Notify Driver
        if (event.getDriverId() != null) {
            firebaseService.sendNotificationToUser(event.getDriverId().toString(), 
                "New Ride Assigned", "You have been assigned order " + event.getOrderId());
        }
        // TODO: Notify tenant as well if tenantId is available in DriverAssignedEvent
    }

    @KafkaListener(topics = "order-delivered", groupId = "notification-service-group")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        System.out.println("NotificationService: processing OrderDeliveredEvent for order: " + event.getOrderId());
        if (event.getTenantId() != null) {
            String topic = "tenant_" + event.getTenantId();
            firebaseService.sendNotificationToTopic(topic, "Order Delivered", 
                "Order " + event.getOrderId() + " was successfully delivered by " + event.getDriverName() + ".");
        }
    }



    @KafkaListener(topics = "driver-canceled", groupId = "notification-service-group", properties = {"spring.json.value.default.type=com.swifttrack.events.DriverCanceledEvent"})
    public void handleDriverCanceled(DriverCanceledEvent event) {
        if (event.getDriverId() != null) {
            firebaseService.sendNotificationToUser(event.getDriverId().toString(), 
                "Assignment Canceled", "Your assignment to order " + event.getOrderId() + " was canceled.");
        }
    }
}
