# SwiftTrack Kafka Integration

This document details the Kafka integration for asynchronous communication between services in the SwiftTrack backend.

## Infrastructure

A `docker-compose-kafka.yml` file is provided in `deployment/` to spin up Zookeeper and Kafka.

To start Kafka:
```bash
docker-compose -f deployment/docker-compose-kafka.yml up -d
```

## Topics & Events

### `order-created`
*   **Producer**: OrderService
*   **Trigger**: When an order is successfully created (in `createOrder`).
*   **Consumers**:
    *   **OrderService**: Triggers AI/ML analysis (stubbed in `OrderEventConsumer`).
    *   **ProviderService**: Receives order details and initiates driver assignment (stubbed in `ProviderEventConsumer`).

### `driver-assigned`
*   **Producer**: ProviderService
*   **Trigger**: When a driver is assigned to an order (currently mocked in `ProviderEventConsumer`).
*   **Consumer**:
    *   **OrderService**: Receives driver assignment details (stubbed in `OrderEventConsumer`).

## Code Changes

### OrderService
*   Added `spring-kafka` dependency in `pom.xml`.
*   Configured Kafka Producer/Consumer in `application.yaml`.
*   Modified `OrderServices` to publish `OrderCreatedEvent`.
*   Added `OrderEventConsumer` to listen to `order-created` and `driver-assigned`.

### ProviderService
*   Added `spring-kafka` dependency in `pom.xml`.
*   Configured Kafka Producer/Consumer in `application.yaml`.
*   Added `ProviderEventConsumer` to listen to `order-created` and publish `driver-assigned`.

### Common
*   Added `OrderCreatedEvent` and `DriverAssignedEvent` classes in `com.swifttrack.events`.

## Verification
1.  Start Kafka containers.
2.  Start `OrderService` and `ProviderService`.
3.  Create an order via API.
4.  Observe logs:
    *   Time 1: `OrderService` sends `order-created`.
    *   Time 2: `OrderService` consumer logs "AI Features Logic Triggered".
    *   Time 3: `ProviderService` consumer logs "Provider Service Received..." and sends `driver-assigned`.
    *   Time 4: `OrderService` consumer logs "Driver Assigned...".
