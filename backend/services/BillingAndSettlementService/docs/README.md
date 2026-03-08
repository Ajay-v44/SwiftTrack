# BillingAndSettlementService — Documentation

Welcome to the documentation for **BillingAndSettlementService**, SwiftTrack's production-grade financial microservice.

## 📖 Table of Contents

| Document | Description |
|----------|-------------|
| [Architecture Overview](./architecture.md) | System design, ledger model, and service architecture |
| [Database Schema](./database-schema.md) | Tables, columns, constraints, indexes, and ER diagram |
| [API Reference](./api-reference.md) | All REST endpoints with request/response formats |
| [Financial Flows](./financial-flows.md) | End-to-end billing scenarios with ledger entry examples |
| [Security & Auditability](./security-and-auditability.md) | Token auth, audit trails, transaction safety, and idempotency |
| [Configuration & Deployment](./configuration.md) | Environment variables, ports, service discovery, and caching |

## 🚀 Quick Start

```bash
# From the BillingAndSettlementService directory
mvn spring-boot:run
```

The service starts on **port 8002** and registers with Eureka for auto-discovery via the API Gateway.

## 🏗️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 25 |
| Framework | Spring Boot 3.5.8 |
| Database | PostgreSQL (schema: `billing`) |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Liquibase |
| Service Discovery | Spring Cloud Eureka |
| Inter-service Auth | Feign Client → AuthService |
| Caching | Redis |
| Event Streaming | Apache Kafka |
| API Docs | Springdoc OpenAPI (Swagger UI) |
