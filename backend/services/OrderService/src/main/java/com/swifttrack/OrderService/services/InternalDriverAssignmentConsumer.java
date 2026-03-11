package com.swifttrack.OrderService.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.DriverInterface;
import com.swifttrack.OrderService.dto.AssignNearestDriversRequest;
import com.swifttrack.OrderService.dto.AssignNearestDriversResponse;
import com.swifttrack.OrderService.models.Order;
import com.swifttrack.OrderService.models.enums.OrderStatus;
import com.swifttrack.OrderService.repositories.OrderRepository;
import com.swifttrack.events.InternalDriverAssignmentEvent;

@Service
public class InternalDriverAssignmentConsumer {
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long RETRY_DELAY_SECONDS = 30;

    private final DriverInterface driverInterface;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InternalDriverAssignmentConsumer(DriverInterface driverInterface, OrderRepository orderRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.driverInterface = driverInterface;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-driver-assignment", groupId = "order-service-group", properties = {
            "spring.json.value.default.type=com.swifttrack.events.InternalDriverAssignmentEvent" })
    public void handleInternalDriverAssignment(InternalDriverAssignmentEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getOrderStatus() == OrderStatus.ASSIGNED || event.getPickupLat() == null
                || event.getPickupLng() == null) {
            System.out.println("Order not found or already assigned or pickup location is null");
            return;
        }

        boolean tenantDriver = "TENANT_DRIVERS".equalsIgnoreCase(event.getSelectedType());
        AssignNearestDriversRequest request = new AssignNearestDriversRequest(
                event.getPickupLat(),
                event.getPickupLng(),
                event.getOrderId(),
                tenantDriver ? "TENANT_DRIVER" : "PLATFORM_DRIVER",
                tenantDriver ? event.getTenantId() : null);

        AssignNearestDriversResponse response = driverInterface.assignNearestDriversInternal(request).getBody();
        if (response != null && response.assigned() && response.assignedDriverId() != null) {
            order.setAssignedDriverId(response.assignedDriverId());
            order.setSelectedProviderCode(response.assignedDriverId().toString());
            order.setOrderStatus(OrderStatus.ASSIGNED);
            orderRepository.save(order);
            return;
        }

        if (event.getAttempt() < MAX_RETRY_ATTEMPTS) {
            InternalDriverAssignmentEvent retryEvent = InternalDriverAssignmentEvent.builder()
                    .orderId(event.getOrderId())
                    .tenantId(event.getTenantId())
                    .selectedType(event.getSelectedType())
                    .pickupLat(event.getPickupLat())
                    .pickupLng(event.getPickupLng())
                    .attempt(event.getAttempt() + 1)
                    .build();

            CompletableFuture.runAsync(
                    () -> kafkaTemplate.send("order-driver-assignment", retryEvent),
                    CompletableFuture.delayedExecutor(RETRY_DELAY_SECONDS, TimeUnit.SECONDS));
        }
    }
}
