# SwiftTrack Driver Service

## Overview

The **Driver Service** is a core microservice in the SwiftTrack ecosystem responsible for managing the lifecycle, availability, and assignments of drivers. It supports both **Tenant Fleet Drivers** (private fleets owned by businesses) and **Marketplace Drivers** (freelance gig workers).

This service handles:
- Driver onboarding and KYC management.
- Real-time location tracking (latitude/longitude updates).
- Online/Offline status toggling.
- Order assignment and acceptance/rejection workflows.
- Performance tracking (ratings, trip counts).

---

## Architecture

The Driver Service is built using **Java (Spring Boot)** and follows a domain-driven design.

### Tech Stack
- **Database**: PostgreSQL (for persistent driver data, vehicle details, and assignment history).
- **Caching**: Redis (for real-time geospatial queries and driver availability status).
- **Messaging**: Kafka (for publishing location updates `driver.location.updates` and assignment events).
- **Migration**: Liquibase (schema version control).
- **Discovery**: Eureka Client (service registration).

---

## Database Design

### 1. `drivers` Table
Stores the core profile of a driver.

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID | Primary Key |
| `tenant_id` | UUID | The business this driver belongs to (if private fleet) |
| `user_id` | UUID | Link to Auth Service user (for login) |
| `name` | VARCHAR | Driver's full name |
| `phone_number` | VARCHAR | Contact number |
| `status` | ENUM | `ACTIVE`, `BUSY`, `INACTIVE`, `SUSPENDED` |
| `is_online` | BOOLEAN | Toggle for availability |
| `current_latitude` | DECIMAL | Last known GPS latitude |
| `current_longitude` | DECIMAL | Last known GPS longitude |
| `rating` | DOUBLE | Aggregate driver score (1-5) |

### 2. `vehicles` Table
Stores details about the vehicle used by the driver.

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID | Primary Key |
| `driver_id` | UUID | Foreign Key to `drivers` |
| `type` | VARCHAR | `BIKE`, `VAN`, `TRUCK`, `SCOOTER` |
| `license_plate` | VARCHAR | Registration number |
| `capacity_kg` | DECIMAL | Max load capacity |

### 3. `driver_assignments` Table
Tracks every order offered to a driver and their response.

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID | Primary Key |
| `driver_id` | UUID | Foreign Key to `drivers` |
| `order_id` | UUID | Global Order ID |
| `status` | ENUM | `ASSIGNED`, `ACCEPTED`, `REJECTED`, `COMPLETED` |
| `assigned_at` | TIMESTAMP | When the system offered the order |
| `accepted_at` | TIMESTAMP | When driver accepted |
| `rejection_reason` | VARCHAR | Reason code if rejected |

### 4. `driver_documents` Table
Stores verification proofs.

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID | Primary Key |
| `driver_id` | UUID | FK |
| `document_type` | VARCHAR | `LICENSE`, `ADHAAR`, `RC` |
| `document_url` | VARCHAR | S3/MinIO URL |
| `is_verified` | BOOLEAN | Verification status |

---

## Key Workflows

### 1. Driver Onboarding
1. Tenant Admin adds a driver via Dashboard.
2. `POST /api/drivers` creates a `Driver` record linked to `tenant_id`.
3. Account details are sent to Auth Service to create credentials.

### 2. Going Online
1. Driver toggles "Go Online" in the mobile app.
2. `POST /api/drivers/{id}/status` updates `is_online = true`.
3. Service updates Redis set of available drivers.

### 3. Location Tracking
1. Mobile app pushes GPS coordinates every 5-10 seconds.
2. `PUT /api/drivers/{id}/location` receives the payload.
3. Updates `current_latitude` and `current_longitude` in DB.
4. Publishes event `driver.location.updates` to Kafka for the Order Service / Tracking Service to consume.
5. Updates Redis Geo index for proximity searches.

### 4. Order Assignment
1. Assignment Engine (in Order Service) selects a driver.
2. Calls `POST /api/drivers/{id}/assignments`.
3. `DriverAssignment` record created with status `ASSIGNED`.
4. Driver receives push notification.
5. Driver Accepts:
   - Status -> `ACCEPTED`.
   - Driver Status -> `BUSY`.
   - Event `driver.assignment.accepted` published.
6. Driver Rejects:
   - Status -> `REJECTED`.
   - Assignment Engine looks for next driver.

---

## API Endpoints (Planned)

### Driver Management
- `POST /api/drivers` - Create new driver
- `GET /api/drivers/{id}` - Get driver profile
- `GET /api/drivers?tenantId={uuid}` - List tenant drivers

### Operations
- `PATCH /api/drivers/{id}/status` - Toggle Online/Offline
- `PUT /api/drivers/{id}/location` - Update GPS coordinates

### Assignments
- `POST /api/drivers/{id}/assignments` - Assign order (Internal/Admin)
- `POST /api/assignments/{assignmentId}/respond` - Accept/Reject (Driver App)
- `GET /api/drivers/{id}/assignments/history` - View past earnings/jobs
