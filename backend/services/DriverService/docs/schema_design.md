# Driver Service Database Schema & Design

## Overview
This document outlines the database schema for the Driver Service, focusing on performance, scalability, and specific use cases like real-time location tracking and order assignment.

## Schema Definitions

### 1. drivers (Core Identity)
Rarely updated, read often. contains core driver profile.

\`\`\`sql
CREATE TABLE drivers (
    id                BIGINT PRIMARY KEY,
    tenant_id         BIGINT NOT NULL,
    name              VARCHAR(255),
    phone             VARCHAR(20),
    email             VARCHAR(255),
    vehicle_type      ENUM('BIKE','CAR','VAN','TRUCK'),
    license_number    VARCHAR(100),
    is_active         BOOLEAN DEFAULT false,
    is_verified       BOOLEAN DEFAULT false,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,

    UNIQUE (tenant_id, phone),
    -- FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
\`\`\`

### 2. driver_status (HOT Table)
Changes frequentely. Separated to avoid locking the `drivers` table.

\`\`\`sql
CREATE TABLE driver_status (
    driver_id         BIGINT PRIMARY KEY,
    tenant_id         BIGINT,
    status            ENUM('OFFLINE','ONLINE','ON_TRIP','SUSPENDED'),
    last_seen_at      TIMESTAMP,

    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);
\`\`\`

### 3. driver_location_live (HOT + Write-Heavy)
Only stores the LATEST location. Overwritten every ~2 mins or less. No history here.

\`\`\`sql
CREATE TABLE driver_location_live (
    driver_id         BIGINT PRIMARY KEY,
    tenant_id         BIGINT,
    latitude          DECIMAL(9,6),
    longitude         DECIMAL(9,6),
    updated_at        TIMESTAMP,

    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);
\`\`\`

### 4. driver_location_history (Cold / Analytics)
Async batch insert. Partition by date recommended. Retention 7-30 days.

\`\`\`sql
CREATE TABLE driver_location_history (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    driver_id         BIGINT,
    tenant_id         BIGINT,
    latitude          DECIMAL(9,6),
    longitude         DECIMAL(9,6),
    recorded_at       TIMESTAMP
);
\`\`\`

### 5. driver_order_assignment (Dispatch Logic)
Heart of dispatch. Supports reassignments.

\`\`\`sql
CREATE TABLE driver_order_assignment (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id          BIGINT,
    driver_id         BIGINT,
    tenant_id         BIGINT,
    status            ENUM('ASSIGNED','ACCEPTED','REJECTED','CANCELLED','COMPLETED'),
    assigned_at       TIMESTAMP,
    updated_at        TIMESTAMP,

    UNIQUE (order_id, driver_id)
);
\`\`\`

### 6. driver_order_cancellation (Audit)
For disputes and reasons.

\`\`\`sql
CREATE TABLE driver_order_cancellation (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id          BIGINT,
    driver_id         BIGINT,
    tenant_id         BIGINT,
    reason            VARCHAR(255),
    cancelled_at      TIMESTAMP
);
\`\`\`

### 7. driver_events (Event Sourcing Light)
For ML, monitoring, fraud detection.

\`\`\`sql
CREATE TABLE driver_events (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    driver_id         BIGINT,
    tenant_id         BIGINT,
    event_type        ENUM(
        'LOGIN','LOGOUT','ONLINE','OFFLINE',
        'ORDER_ASSIGNED','ORDER_ACCEPTED',
        'ORDER_CANCELLED'
    ),
    metadata          JSON,
    created_at        TIMESTAMP
);
\`\`\`

## Repositories & Needed Queries
These repositories should be implemented in `com.swifttrack.DriverService.repositories`.

### DriverRepository
- `Optional<Driver> findByTenantIdAndPhone(Long tenantId, String phone);`

### DriverStatusRepository
- `Optional<DriverStatus> findByDriverId(Long driverId);`
- `List<DriverStatus> findByTenantIdAndStatus(Long tenantId, DriverOnlineStatus status);`

### DriverLocationLiveRepository
- `Optional<DriverLocationLive> findByDriverId(Long driverId);`

### DriverLocationHistoryRepository
- `List<DriverLocationHistory> findByDriverIdAndRecordedAtBetween(Long driverId, LocalDateTime start, LocalDateTime end);`

### DriverOrderAssignmentRepository
- `Optional<DriverOrderAssignment> findByOrderIdAndDriverId(Long orderId, Long driverId);`

### DriverEventRepository
- `List<DriverEvent> findByDriverId(Long driverId);`

## Performance & Scaling Notes
1. **Location Updates**:
   - DO NOT write to `driver_location_history` synchronously on every update.
   - Write only to `driver_location_live`.
   - Use Async batch insert into `driver_location_history` (e.g., via Kafka/Queue).
   - `driver_location_live` should likely use a Spatial Index on (latitude, longitude) if supported and needed for "nearby drivers" queries.

2. **Order Assignment**:
   - Read from `driver_status` AND `driver_location_live` to determine eligibility.
   - Write to `driver_order_assignment`.

3. **Index Strategy**:
   - `drivers`: `(tenant_id)`, `(tenant_id, is_active)`.
   - `driver_status`: `(tenant_id, status)`, `(last_seen_at)`.
   - `driver_location_live`: `(tenant_id)`.
   - `driver_order_assignment`: `(driver_id, status)`, `(order_id)`.
