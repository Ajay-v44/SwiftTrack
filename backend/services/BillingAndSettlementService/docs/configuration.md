# Configuration & Deployment

## 1. Environment Variables

The service loads configuration from an `.env` file. All sensitive credentials must be set here.

### Required Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | — | PostgreSQL hostname |
| `DB_PORT` | — | PostgreSQL port |
| `DB_NAME` | — | Database name |
| `DB_USERNAME` | — | Database username |
| `DB_PASSWORD` | — | Database password |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8002` | Service HTTP port |
| `EUREKA_URL` | `http://127.0.0.1:8761/eureka/` | Eureka server URL |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |

### Example `.env` File

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=swifttrack
DB_USERNAME=postgres
DB_PASSWORD=your-password-here
```

---

## 2. Service Ports & Discovery

| Property | Value |
|----------|-------|
| Service Name | `BillingAndSettlementService` |
| Default Port | `8002` |
| Schema | `billing` |
| Eureka Registration | Auto via `@EnableDiscoveryClient` |
| Gateway Auto-Discovery | Yes — accessible via Gateway as `/BillingAndSettlementService/**` |

### Service Registration

The service automatically registers with Eureka Server and becomes discoverable by:
- **API Gateway** — Routes requests from `/{service-name}/**` to the service
- **Other microservices** — Can call via Feign Clients using the service name

---

## 3. Database Configuration

### Schema

All tables are created in the `billing` schema:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=billing
```

### Liquibase Migrations

Migrations are managed by Liquibase and run automatically on startup:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
    drop-first: false
```

**Migration files:**

| File | Table Created |
|------|--------------|
| `001-create-accounts-table.yaml` | `billing.accounts` |
| `002-create-ledger-transactions-table.yaml` | `billing.ledger_transactions` |
| `003-create-margin-config-table.yaml` | `billing.margin_config` |
| `004-create-pricing-snapshots-table.yaml` | `billing.pricing_snapshots` |
| `005-create-settlements-table.yaml` | `billing.settlements` |
| `006-create-settlement-transactions-table.yaml` | `billing.settlement_transactions` |

### Connection Pool

```yaml
hikari:
  maximum-pool-size: 5
```

For production, consider increasing to 10-20 based on load.

---

## 4. Redis Cache Configuration

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 60000  # 60 seconds
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

### Recommended Cache Keys (Future Implementation)

| Key Pattern | TTL | Description |
|------------|-----|-------------|
| `account:{accountId}` | 60s | Cached account details |
| `balance:{accountId}` | 30s | Cached balance (short TTL due to financial sensitivity) |
| `margin:{userId}:{orgType}` | 300s | Active margin configs |
| `pricing:{orderId}` | 3600s | Immutable pricing snapshots (long TTL, never changes) |

---

## 5. Kafka Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: billing-service-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### Planned Event Topics

| Topic | Publisher | Consumer | Purpose |
|-------|----------|----------|---------|
| `order.completed` | OrderService | BillingService | Triggers billing on order completion |
| `settlement.initiated` | BillingService | Payment Gateway | Triggers payout processing |
| `settlement.status.update` | Payment Gateway | BillingService | Updates settlement status |

---

## 6. API Documentation (Swagger)

Swagger UI is available at:

```
http://localhost:8002/swagger-ui.html
```

API docs JSON:
```
http://localhost:8002/v3/api-docs
```

---

## 7. Running the Service

### Prerequisites

1. PostgreSQL running with database created
2. Redis running (for caching)
3. Eureka Server running (port 8761)
4. AuthService running (for token resolution)

### Start Command

```bash
cd services/BillingAndSettlementService
mvn spring-boot:run
```

### Verify

```bash
# Health check
curl http://localhost:8002/api/billing/health

# Swagger UI
open http://localhost:8002/swagger-ui.html

# Eureka registration
open http://localhost:8761
```

---

## 8. Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.5.8 | Application framework |
| Spring Cloud | 2025.0.0 | Service discovery, Feign |
| Spring Data JPA | (managed) | Database ORM |
| Spring Data Redis | (managed) | Caching |
| Spring Kafka | (managed) | Event streaming |
| Liquibase | 4.29.2 | Database migrations |
| PostgreSQL Driver | 42.7.4 | Database connectivity |
| Springdoc OpenAPI | 2.8.3 | API documentation |
| Lombok | (managed) | Boilerplate reduction |
| MapStruct | 1.5.5 | Object mapping |
| SwiftTrack Common | 1.0.0-SNAPSHOT | Shared DTOs and enums |

---

## 9. Production Recommendations

| Area | Recommendation |
|------|---------------|
| **Connection Pool** | Increase `hikari.maximum-pool-size` to 15-20 |
| **Redis** | Use Redis Sentinel or Cluster for HA |
| **Database** | Enable read replicas for GET queries |
| **Monitoring** | Add Prometheus + Grafana via Spring Actuator |
| **Logging** | Push logs to ELK stack for centralized analysis |
| **Secrets** | Use HashiCorp Vault or K8s Secrets instead of `.env` |
| **Rate Limiting** | Add rate limiting on billing endpoints |
| **Circuit Breaker** | Add Resilience4j on AuthService Feign calls |
