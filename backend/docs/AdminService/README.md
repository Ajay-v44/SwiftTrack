# SwiftTrack — AdminService

## Table of Contents

1. [Overview](#1-overview)
2. [Tech Stack](#2-tech-stack)
3. [Architecture](#3-architecture)
4. [Security Model](#4-security-model)
5. [Project Structure](#5-project-structure)
6. [Configuration & Setup](#6-configuration--setup)
7. [Database Design](#7-database-design)
8. [Feign Clients (Inter-Service Communication)](#8-feign-clients-inter-service-communication)
9. [API Reference](#9-api-reference)
   - [User Management](#91-user-management)
   - [Driver Management](#92-driver-management)
   - [Order Management](#93-order-management)
   - [Provider Management](#94-provider-management)
   - [Billing & Settlement](#95-billing--settlement)
   - [AI Dispatch](#96-ai-dispatch)
   - [Audit Logs](#97-audit-logs)
   - [Platform Overview](#98-platform-overview)
10. [Audit Logging System](#10-audit-logging-system)
11. [Running Locally](#11-running-locally)
12. [Adding to Other Services](#12-adding-admin-service-to-other-services)

---

## 1. Overview

The **AdminService** is the centralised administration portal for the SwiftTrack platform. It provides a **single, secure gateway** for SwiftTrack administrators (`SUPER_ADMIN`, `SYSTEM_ADMIN`, and `ADMIN_USER`) to manage and monitor every aspect of the platform — from user onboarding and driver verification to billing settlements, delivery provider configurations, and AI dispatch control.

Instead of having admins log into each microservice individually, AdminService **aggregates all admin-facing operations** in one place, delegating to the correct downstream service via Feign clients.

### What AdminService Does

| Domain | Capabilities |
|---|---|
| **Users** | List users, activate/deactivate, update verification status, assign admin roles |
| **Drivers** | View pending drivers, approve/reject, monitor live location & status, force-cancel assignments |
| **Orders** | View any order, check status, force-cancel problematic orders |
| **Providers** | List, create, enable/disable delivery providers, manage tenant-provider configurations |
| **Billing** | Manage accounts, top up wallets, process settlements, configure margin rules |
| **AI Dispatch** | Monitor dispatch health, manually trigger AI driver selection |
| **Audit Logs** | Full traceable history of every admin action with paginated, filterable queries |
| **Platform** | Service health checks, environment metadata |

---

## 2. Tech Stack

| Component | Technology |
|---|---|
| **Language** | Java 25 |
| **Framework** | Spring Boot 3.5.8 |
| **Service Discovery** | Spring Cloud Netflix Eureka Client |
| **Inter-Service Comms** | Spring Cloud OpenFeign |
| **Database** | PostgreSQL (schema: `admin`) |
| **DB Migrations** | Liquibase |
| **ORM** | Spring Data JPA / Hibernate |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Maven |
| **Async Processing** | Spring `@Async` (for non-blocking audit logging) |
| **Caching** | Spring Cache (simple in-memory, upgradeable to Redis) |

---

## 3. Architecture

AdminService sits **between the admin portal (UI or Postman) and all downstream services**. It:

1. Receives all admin API requests with a `token` header
2. Validates the token against **AuthService** using a Feign client
3. Checks the caller has an admin role (`SUPER_ADMIN`, `SYSTEM_ADMIN`, or `ADMIN_USER`)
4. Delegates the actual operation to the correct downstream service
5. **Asynchronously** writes an audit log entry to its own PostgreSQL schema
6. Returns the downstream response to the caller

```
┌──────────────────────────────────────────────────────────────────┐
│                        Admin Portal / Postman                    │
│                   (token: <jwt> in all headers)                  │
└───────────────────────────────┬──────────────────────────────────┘
                                │ HTTP
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│                         AdminService :8009                       │
│                                                                  │
│  ┌──────────────────────┐    ┌─────────────────────────────────┐ │
│  │     AdminGuard       │───▶│   AuthClient (Feign)            │ │
│  │  (Security Layer)    │    │   validates token via           │ │
│  └──────────────────────┘    │   AuthService                   │ │
│                              └─────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                 Admin Controllers (8 sections)              │  │
│  │  Users │ Drivers │ Orders │ Providers │ Billing │ AI │ ... │  │
│  └───────────────────────────┬────────────────────────────────┘  │
│                              │                                    │
│  ┌───────────────────────────┴──────────────────────────────┐    │
│  │               Feign Clients                               │    │
│  │  AuthClient │ DriverClient │ OrderClient │ ProviderClient │    │
│  │  BillingClient │ AIDispatchClient                         │    │
│  └───────────────────────────┬──────────────────────────────┘    │
│                              │  async (non-blocking)              │
│  ┌───────────────────────────▼──────────────────────────────┐    │
│  │        AuditService  →  AdminAuditLog (PostgreSQL)        │    │
│  └──────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
                              │ Feign HTTP calls
        ┌─────────────────────┼───────────────────────┐
        ▼                     ▼                        ▼
  AuthService          DriverService             OrderService
  ProviderService   BillingService         AIDispatchService
```

---

## 4. Security Model

**Every single endpoint** in AdminService (except `/api/admin/platform/v1/health`) requires a valid JWT token issued by AuthService. The `AdminGuard` component performs this check on every request.

### Role Hierarchy

| Role | Access Level |
|---|---|
| `SUPER_ADMIN` | Full access — including all SUPER_ADMIN-gated endpoints |
| `SYSTEM_ADMIN` | Full access to all standard admin endpoints |
| `ADMIN_USER` | Full access to all standard admin endpoints |
| Any other user type | **403 Forbidden** — access denied |

### Two-Tier Gate

The `AdminGuard` exposes two methods used across controllers:

```java
// Allows all three admin roles
adminGuard.requireAdmin(token);

// Allows ONLY SUPER_ADMIN — used for sensitive destructive operations
adminGuard.requireSuperAdmin(token);
```

### SUPER_ADMIN-only Endpoints

The following operations require `SUPER_ADMIN` because they are irreversible or high-impact:

- Assigning admin role to a user
- Creating a new delivery provider
- Enabling/Disabling a delivery provider
- Wallet top-up
- Margin config create / update / deactivate
- Manual AI dispatch trigger
- Viewing the full audit log
- Querying audit logs by admin or date range

---

## 5. Project Structure

```
AdminService/
├── pom.xml
└── src/main/
    ├── java/com/swifttrack/AdminService/
    │   ├── AdminServiceApplication.java          ← Main class
    │   ├── conf/
    │   │   ├── AdminAccessDeniedException.java   ← Custom 403 exception
    │   │   ├── GlobalExceptionHandler.java        ← @RestControllerAdvice
    │   │   └── OpenApiConfig.java                 ← Swagger / OpenAPI config
    │   ├── security/
    │   │   └── AdminGuard.java                   ← Core security component
    │   ├── clients/                               ← Feign clients per service
    │   │   ├── AuthClient.java
    │   │   ├── DriverClient.java
    │   │   ├── OrderClient.java
    │   │   ├── ProviderClient.java
    │   │   ├── BillingClient.java
    │   │   └── AIDispatchClient.java
    │   ├── controllers/                           ← 8 domain controllers
    │   │   ├── AdminUserController.java
    │   │   ├── AdminDriverController.java
    │   │   ├── AdminOrderController.java
    │   │   ├── AdminProviderController.java
    │   │   ├── AdminBillingController.java
    │   │   ├── AdminAIDispatchController.java
    │   │   ├── AdminAuditController.java
    │   │   └── AdminPlatformController.java
    │   ├── dto/                                   ← Admin-specific request DTOs
    │   │   ├── AdminUpdateUserRequest.java
    │   │   ├── AdminCancelOrderRequest.java
    │   │   ├── AdminWalletTopUpRequest.java
    │   │   └── AdminMarginConfigRequest.java
    │   ├── models/
    │   │   └── AdminAuditLog.java                ← JPA entity for audit trail
    │   ├── repositories/
    │   │   └── AdminAuditLogRepository.java
    │   └── services/
    │       └── AuditService.java                  ← @Async audit log writer
    └── resources/
        ├── application.yaml
        └── db/changelog/
            ├── db.changelog-master.yaml
            └── changes/
                ├── 001-create-admin-schema.yaml
                └── 002-create-admin-audit-log.yaml
```

---

## 6. Configuration & Setup

### Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `swifttrack` |
| `DB_USERNAME` | DB username | `postgres` |
| `DB_PASSWORD` | DB password | `yourpassword` |
| `SERVER_PORT` | HTTP port (default 8009) | `8009` |
| `EUREKA_URL` | Eureka registry URL | `http://127.0.0.1:8761/eureka/` |

### application.yaml Highlights

```yaml
spring:
  application:
    name: AdminService
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=admin,public
    hikari:
      maximum-pool-size: 5               # Conservative pool — admin is low-traffic
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true

server:
  port: ${SERVER_PORT:8009}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://127.0.0.1:8761/eureka/}
```

### Eureka Service Names Used by Feign Clients

The Feign clients resolve service names through Eureka load balancing. Make sure these services are registered:

| Feign Client | Eureka Name |
|---|---|
| `AuthClient` | `authservice` |
| `DriverClient` | `driverservice` |
| `OrderClient` | `orderservice` |
| `ProviderClient` | `providerservice` |
| `BillingClient` | `billingandsettlementservice` |
| `AIDispatchClient` | `aidispatchservice` |

---

## 7. Database Design

AdminService owns the `admin` schema in the shared PostgreSQL instance.

### `admin_audit_log` Table

Every admin action that mutates data is recorded here. Queries are fully paginated and indexed for performance.

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | UUID | NO | Primary key (auto-generated) |
| `admin_id` | UUID | NO | ID of the admin who performed the action |
| `admin_name` | VARCHAR(255) | YES | Name of the admin (denormalised for speed) |
| `admin_type` | VARCHAR(50) | YES | Role: `SUPER_ADMIN`, `SYSTEM_ADMIN`, etc. |
| `action_type` | VARCHAR(100) | NO | e.g. `DRIVER_APPROVE`, `WALLET_TOPUP`, `ORDER_CANCEL` |
| `service_domain` | VARCHAR(50) | YES | e.g. `DRIVER`, `BILLING`, `ORDER`, `USER` |
| `target_id` | UUID | YES | UUID of the entity that was acted upon |
| `target_type` | VARCHAR(100) | YES | Type of target: `USER`, `DRIVER`, `ORDER`, etc. |
| `details` | TEXT | YES | Free-text description of what changed |
| `ip_address` | VARCHAR(50) | YES | Source IP (for future request interceptor) |
| `created_at` | TIMESTAMP | NO | Auto-set on insert |

### Indexes

| Index Name | Column | Purpose |
|---|---|---|
| `idx_audit_admin_id` | `admin_id` | Filter logs by admin user |
| `idx_audit_created_at` | `created_at` | Range queries, newest-first ordering |
| `idx_audit_action_type` | `action_type` | Filter by type of action |
| `idx_audit_target_id` | `target_id` | Full action history for a target entity |

### Liquibase Migrations

| File | Description |
|---|---|
| `001-create-admin-schema.yaml` | Creates the `admin` PostgreSQL schema |
| `002-create-admin-audit-log.yaml` | Creates `admin_audit_log` table + all 4 indexes |

---

## 8. Feign Clients (Inter-Service Communication)

AdminService communicates with all downstream services exclusively via **OpenFeign** clients. Each client is defined in `com.swifttrack.AdminService.clients`.

### AuthClient

Calls `AuthService` for token validation and user management.

```java
@FeignClient(name = "authservice", url = "http://localhost:8080/authservice")
public interface AuthClient {
    ResponseEntity<TokenResponse> getUserDetails(@RequestParam String token);
    ResponseEntity<List<ListOfTenantUsers>> getTenantUsers(...);
    ResponseEntity<List<GetDriverUsers>> getDriverUsers(...);
    ResponseEntity<Message> updateUserStatusAndVerification(...);
    ResponseEntity<Message> assignAdmin(...);
}
```

### DriverClient

Calls `DriverService` for driver operations.

```java
@FeignClient(name = "driverservice", url = "http://localhost:8080/driverservice")
public interface DriverClient {
    ResponseEntity<List<GetAllDriverUser>> getDriverUsers(...);
    ResponseEntity<Message> acceptDriver(...);
    ResponseEntity<?> getDriverStatus(UUID driverId);
    ResponseEntity<?> getDriverLocation(UUID driverId);
    ResponseEntity<Boolean> isDriverAvailable(UUID driverId);
    ResponseEntity<Message> cancelAssignedOrderInternal(...);
}
```

### BillingClient

Calls `BillingAndSettlementService` for financial operations — accounts, transactions, settlements, margin configs.

### AIDispatchClient

Calls `AIDispatchService` for health monitoring and manual dispatch triggers.

---

## 9. API Reference

**Base URL:** `http://localhost:8009`  
**Swagger UI:** `http://localhost:8009/swagger-ui.html`  
**All endpoints require:** `token` header (JWT from AuthService login)

---

### 9.1 User Management

**Base Path:** `/api/admin/users`

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/list?userType={type}` | Admin | List all users filtered by `UserType` enum |
| `GET` | `/v1/drivers?status={status}` | Admin | List driver users by `VerificationStatus` |
| `POST` | `/v1/updateStatus` | Admin | Activate/deactivate a user + update verification |
| `POST` | `/v1/assignAdmin?userId={uuid}` | **SUPER_ADMIN** | Promote user to `TENANT_ADMIN` |
| `GET` | `/v1/me` | Admin | Get the calling admin's own profile |

#### `POST /v1/updateStatus` — Request Body

```json
{
  "userId": "uuid",
  "status": true,
  "verificationStatus": "APPROVED",
  "reason": "Document verified manually"
}
```

---

### 9.2 Driver Management

**Base Path:** `/api/admin/drivers`

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/list?verificationStatus={status}` | Admin | List all platform drivers by status |
| `GET` | `/v1/tenant-drivers` | Admin | List drivers linked to the admin's tenant |
| `POST` | `/v1/approve/{driverId}` | Admin | Approve driver → creates billing account |
| `GET` | `/v1/status/{driverId}` | Admin | Get live online/offline status of a driver |
| `GET` | `/v1/location/{driverId}` | Admin | Get live GPS coordinates of a driver |
| `GET` | `/v1/available/{driverId}` | Admin | Check if a driver is available for dispatch |
| `POST` | `/v1/cancelAssignment?orderId={uuid}` | Admin | Force-cancel a driver's active assignment |

#### `VerificationStatus` Enum Values
`PENDING` | `APPROVED` | `REJECTED`

---

### 9.3 Order Management

**Base Path:** `/api/admin/orders`

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/{orderId}` | Admin | Fetch full details of any order |
| `GET` | `/v1/{orderId}/status` | Admin | Get current status string of an order |
| `POST` | `/v1/cancel` | Admin | Force-cancel any order (requires reason) |

#### `POST /v1/cancel` — Request Body

```json
{
  "orderId": "uuid",
  "reason": "Customer reported fraud"
}
```

---

### 9.4 Provider Management

**Base Path:** `/api/admin/providers`

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/list` | Admin | List all delivery providers in the system |
| `GET` | `/v1/byStatus?status={bool}` | Admin | Filter providers by active/inactive |
| `POST` | `/v1/create` | **SUPER_ADMIN** | Register a new delivery provider |
| `PUT` | `/v1/updateStatus?providerId={uuid}&status={bool}` | **SUPER_ADMIN** | Enable/Disable a provider platform-wide |
| `PUT` | `/v1/tenant/setProviderStatus?providerId=&enabled=` | Admin | Enable/Disable a provider for a specific tenant |

---

### 9.5 Billing & Settlement

**Base Path:** `/api/admin/billing`

#### Accounts

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/accounts/user/{userId}` | Admin | Get billing account for any user |
| `GET` | `/v1/accounts/{accountId}/transactions` | Admin | List all ledger transactions for an account |
| `POST` | `/v1/accounts/create?userId=&accountType=` | Admin | Manually create a billing account |
| `POST` | `/v1/accounts/{accountId}/reconcile` | Admin | Verify & correct balance from ledger |

#### Wallet

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `POST` | `/v1/wallet/topup` | **SUPER_ADMIN** | Credit a user's wallet with specified amount |

#### `POST /v1/wallet/topup` — Request Body

```json
{
  "userId": "uuid",
  "amount": 500.00,
  "note": "Compensation for delayed delivery"
}
```

#### Settlements

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/settlements/pending` | Admin | List all pending settlement records |
| `GET` | `/v1/settlements/account/{accountId}` | Admin | All settlements for an account |
| `GET` | `/v1/settlements/{settlementId}/transactions` | Admin | Breakdown of a settlement |
| `PUT` | `/v1/settlements/{settlementId}/settle` | Admin | Mark settlement as `SETTLED` |
| `PUT` | `/v1/settlements/{settlementId}/fail` | Admin | Mark settlement as `FAILED` |

#### Margin Configuration

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/margin-config/platform?marginType=` | Admin | View platform-wide margin rules |
| `GET` | `/v1/margin-config/user/{userId}` | Admin | View margin config applied to a user |
| `POST` | `/v1/margin-config` | **SUPER_ADMIN** | Create a new margin/commission rule |
| `PUT` | `/v1/margin-config/{configId}` | **SUPER_ADMIN** | Update an existing margin rule |
| `PATCH` | `/v1/margin-config/{configId}/deactivate` | **SUPER_ADMIN** | Deactivate a margin rule (soft delete) |

#### `POST /v1/margin-config` — Request Body

```json
{
  "userId": "uuid-or-null-for-platform-level",
  "marginType": "PERCENTAGE",
  "marginValue": 8.5,
  "active": true
}
```

---

### 9.6 AI Dispatch

**Base Path:** `/api/admin/ai-dispatch`

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/health` | Admin | Check AI Dispatch Service health & LLM pipeline status |
| `POST` | `/v1/manual-dispatch` | **SUPER_ADMIN** | Manually invoke AI driver selection with custom input |

#### `POST /v1/manual-dispatch` — Request Body

Mirrors the `DispatchRequest` from AIDispatchService:

```json
{
  "orderId": "uuid",
  "candidateDriverIds": ["uuid1", "uuid2", "uuid3"],
  "orderDetails": { ... }
}
```

---

### 9.7 Audit Logs

**Base Path:** `/api/admin/audit`

All audit log endpoints return paginated responses. Default: `page=0, size=50`.

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/logs?page=&size=` | **SUPER_ADMIN** | All admin audit logs |
| `GET` | `/v1/logs/admin/{adminId}?page=&size=` | **SUPER_ADMIN** | Logs by specific admin |
| `GET` | `/v1/logs/action/{actionType}?page=&size=` | Admin | Logs by action type |
| `GET` | `/v1/logs/target/{targetId}?page=&size=` | Admin | Full action history on an entity |
| `GET` | `/v1/logs/domain/{domain}?page=&size=` | Admin | Logs by service domain |
| `GET` | `/v1/logs/dateRange?from=&to=` | **SUPER_ADMIN** | Logs within a time window |

#### Common `actionType` Values

| Action Type | Trigger |
|---|---|
| `USER_STATUS_UPDATE` | Admin activated/verified a user |
| `ASSIGN_ADMIN` | User promoted to TENANT_ADMIN |
| `DRIVER_APPROVE` | Driver approved by admin |
| `DRIVER_ASSIGNMENT_CANCEL` | Assignment force-cancelled |
| `ORDER_CANCEL` | Admin force-cancelled an order |
| `PROVIDER_CREATE` | New provider registered |
| `PROVIDER_STATUS_UPDATE` | Provider enabled/disabled |
| `TENANT_PROVIDER_STATUS_UPDATE` | Tenant-provider toggled |
| `ACCOUNT_CREATE` | Billing account created manually |
| `ACCOUNT_RECONCILE` | Account balance reconciled |
| `WALLET_TOPUP` | Wallet credited by admin |
| `SETTLEMENT_MARK_SETTLED` | Settlement marked as settled |
| `SETTLEMENT_MARK_FAILED` | Settlement marked as failed |
| `MARGIN_CONFIG_CREATE` | Margin rule created |
| `MARGIN_CONFIG_UPDATE` | Margin rule updated |
| `MARGIN_CONFIG_DEACTIVATE` | Margin rule deactivated |
| `AI_DISPATCH_MANUAL` | Manual AI dispatch triggered |

#### `service_domain` Values

`USER` | `DRIVER` | `ORDER` | `BILLING` | `PROVIDER` | `AI_DISPATCH`

---

### 9.8 Platform Overview

**Base Path:** `/api/admin/platform`

| Method | Endpoint | Auth Needed | Description |
|---|---|---|---|
| `GET` | `/v1/health` | **None** | AdminService self health check |
| `GET` | `/v1/services/health` | Admin | Health status of all downstream services |
| `GET` | `/v1/info` | Admin | Platform version, environment info |

#### `GET /v1/services/health` — Sample Response

```json
{
  "adminService": "UP",
  "aiDispatchService": "UP",
  "timestamp": "2026-03-15T00:01:00"
}
```

---

## 10. Audit Logging System

The audit system is a critical component of AdminService. It provides a full, tamper-evident trace of every admin action.

### How It Works

```
Admin Action
     │
     ▼
Controller validates token (AdminGuard)
     │
     ▼
Controller calls downstream service (Feign)
     │
     ▼
Controller calls auditService.log(...)  ← non-blocking @Async
     │
     ▼
Return response to caller immediately
                        (background thread writes audit log)
```

### Design Principles

- **Non-blocking** — `AuditService.log()` is annotated with `@Async`. Audit writes run on a separate thread pool, so they **never add latency** to API responses.
- **Failure-safe** — If the audit write fails (DB down, etc.), the error is logged but the response has already been sent. Business operations are not blocked by audit failures.
- **Denormalised** — Admin name and role are stored directly in the audit log to avoid expensive joins when reviewing history.
- **Paginated** — All queries return `Page<AdminAuditLog>` for memory-efficient retrieval.

### Log Structure Example

```json
{
  "id": "d4f1b2c3-...",
  "adminId": "a1b2c3d4-...",
  "adminName": "Ajay Kumar",
  "adminType": "SUPER_ADMIN",
  "actionType": "DRIVER_APPROVE",
  "serviceDomain": "DRIVER",
  "targetId": "b2c3d4e5-...",
  "targetType": "DRIVER",
  "details": "Admin approved driver: b2c3d4e5-...",
  "createdAt": "2026-03-15T00:05:30"
}
```

---

## 11. Running Locally

### Prerequisites

- Java 25 installed
- PostgreSQL running with `swifttrack` database
- Eureka Server running on port `8761`
- AuthService running (minimum requirement for token validation)

### Steps

```bash
# 1. Navigate to AdminService
cd services/AdminService

# 2. Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=swifttrack
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword

# 3. Run the service
mvn spring-boot:run
```

The service starts on **port 8009** by default.

### URLs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8009/swagger-ui.html |
| OpenAPI JSON | http://localhost:8009/v3/api-docs |
| Health Check | http://localhost:8009/api/admin/platform/v1/health |

### Getting an Admin Token

Use AuthService to log in with an admin account:

```http
POST http://localhost:8001/api/users/v1/login/emailAndPassword
Content-Type: application/json

{
  "email": "admin@swifttrack.com",
  "password": "admin123"
}
```

Use the returned JWT token as the `token` header in all admin API calls.

---

## 12. Adding Admin Service to Other Services

If another service needs to call AdminService (e.g., to verify admin access or submit audit events), create a Feign client:

```java
@FeignClient(name = "adminservice", url = "http://localhost:8080/adminservice")
public interface AdminClient {

    // Example: trigger an audit log entry from another service
    @PostMapping("/api/admin/audit/v1/external")
    ResponseEntity<Void> submitExternalAuditLog(@RequestBody AuditLogPayload payload);
}
```

Register it in the other service's Spring Boot application class:

```java
@EnableFeignClients(basePackages = "com.swifttrack.FeignClient")
```

---

## Notes

- AdminService is **stateless** (except for the audit log DB writes) — it holds no business data of its own.
- The service is designed to be **low-traffic** — admin operations are infrequent, so the Hikari pool is limited (`maximum-pool-size: 5`) to conserve DB connections.
- All response types from downstream services are returned as-is via `ResponseEntity<?>` for the generic proxy endpoints — you get the exact schema from the downstream service.
- The Swagger UI groups endpoints by controller tag for easy navigation:
  - `Admin - User Management`
  - `Admin - Driver Management`
  - `Admin - Order Management`
  - `Admin - Provider Management`
  - `Admin - Billing & Settlement`
  - `Admin - AI Dispatch`
  - `Admin - Audit Logs`
  - `Admin - Platform Overview`
