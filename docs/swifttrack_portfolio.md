# 🚀 SwiftTrack — Portfolio Analysis

---

## 📝 Short Description

> **SwiftTrack** is an **AI-driven, plug-and-play delivery orchestration SaaS platform** that unifies and automates the entire delivery ecosystem for businesses. It enables companies to seamlessly integrate their applications and offload the complexity of managing multiple delivery providers, driver fleets, and real-time tracking — all through a single, intelligent platform.

> SwiftTrack goes far beyond a simple mapping or CRUD system. It acts as a **delivery infrastructure layer** — using ML-powered dispatch decisions, RAG-based driver memory, real-time KD-Tree spatial indexing, Kafka-backed event streaming, and an LLM-powered agentic dashboard — to intelligently route, assign, and track every delivery in milliseconds. Built for e-commerce, hyperlocal, healthcare, and on-demand businesses, it replaces the need to manage dozens of delivery vendor integrations and fleet tools separately.

---

## 🌟 10 Key Features for Portfolio

---

### 1. 🤖 [AI] LLM-Powered Driver Dispatch Engine
**The most unique feature of SwiftTrack.** Built as a standalone `AIDispatchService` (Spring Boot microservice), this system receives a shortlist of candidate drivers and runs a **9-step AI pipeline** to select the optimal driver:

- Fetches **versioned prompts** from LangSmith prompt registry
- Retrieves driver profiles (acceptance rate, cancellation rate, SLA adherence, rating, idle time) from Supabase PostgreSQL
- Performs **pgvector cosine similarity search** to retrieve the driver's most relevant behavioral memory embeddings (RAG)
- Assembles full context and calls a **local LLM (Qwen 2.5 3B via Ollama)** for the final decision
- Returns a decision with `confidence score`, `reason`, and `fallback` flag
- Complete **Langfuse observability** traces every step of the pipeline asynchronously
- Hard **400ms pipeline budget** with deterministic fallback if LLM fails

> **Stack:** Spring AI 1.0.0 · Ollama · Qwen 2.5:3b-instruct · LangSmith · Langfuse · pgvector · Supabase PostgreSQL

---

### 2. 🤖 [AI/ML] Automated Provider Assignment with Daily-Retrained ML Model
SwiftTrack includes a **FastAPI ML microservice** that predicts the `success_probability` of assigning an order to a specific delivery provider (Porter, Dunzo, Shadowfax, Rapido, Uber, etc.):

- Model: **Random Forest Classifier** (Scikit-Learn)
- Features: `distance_km`, `traffic_level`, `is_peak_hour`, `provider_load`, `provider_code` (one-hot encoded)
- **Immutable Model Pattern**: Every night at 12 AM, GitHub Actions retrains the model on production data, bakes the `.pkl` into a Docker image, tags it with a build ID, and pushes it to **GHCR**
- Production container pulls the `latest` image with no external runtime dependency
- Automatic **synthetic data fallback** if production data is insufficient

> **Stack:** Python · FastAPI · Scikit-Learn · GitHub Actions (CI/CD) · GitHub Container Registry · Docker

---

### 3. 🤖 [AI] Driver Behavioral Memory Embeddings (RAG Preparation)
The Driver Service continuously builds a **"memory" of each driver's behavioral patterns** and stores them as vector embeddings in Supabase:

- After every order completion, cancellation, or rating update, a **behavioral summary** is auto-generated (e.g., *"Driver consistently delivers within 15 mins. High acceptance during peak hours."*)
- This summary is embedded using **Ollama's `nomic-embed-text` model** (768-dimensional vectors)
- Stored in a `driver_memory` table with a pgvector index (ivfflat) for fast cosine similarity search
- A **daily cron job (2 AM IST)** generates aggregated daily memory summaries for all drivers
- These memories are directly consumed by the AI Dispatch pipeline (Feature #1) to give the LLM rich context about driver behavior

> **Stack:** Spring AI · Ollama (nomic-embed-text) · pgvector (Supabase PostgreSQL) · Spring Scheduler

---

### 4. 🗺️ [AI-Assisted] KD-Tree + Redis GEO Spatial Driver Dispatch
A custom, high-performance **spatial indexing system** for finding and dispatching the nearest drivers in real time:

- Drivers publish live GPS coordinates; the service writes to both **Redis GEO** (coarse 5km radius filter) and an **in-memory ConcurrentHashMap**
- A background thread rebuilds a **KD-Tree every 2 seconds** (when dirty) using `AtomicReference` for lock-free reads
- KD-Tree uses **Haversine distance** for precise ranking with O(log n) average search complexity
- Top 15 nearest drivers are sent to the AI Dispatch Service in batches of 5
- The AI-selected driver is automatically assigned to the order; if assignment fails, the next batch is attempted

> **Stack:** Java · Custom KD-Tree · Redis GEO · Spring Boot · OpenFeign (to AIDispatchService)

---

### 5. ⚡ Event-Driven Microservices Architecture with Kafka
The entire platform is built around an **event bus** using Apache Kafka as the backbone:

- **Topics:** `order-created`, `driver-assigned`, `driver-location-updates`, `driver-performance`, `driver-canceled`
- `OrderService` publishes `OrderCreatedEvent`; `ProviderService` consumes it, assigns a driver, and emits `DriverAssignedEvent`
- Driver performance events (`ORDER_COMPLETED`, `ORDER_CANCELLED`, `RATING_UPDATED`) trigger the embedding pipeline
- Fully decoupled services with independent scaling, audit trail replayability, and no tight coupling

> **Stack:** Apache Kafka · Spring Kafka · Docker Compose (Zookeeper + Kafka + Redis)

---

### 6. 🔄 Distributed Caching with Redis (Cache-Aside Pattern)
To reduce database load and achieve sub-50ms latency for real-time operations:

- **Cache-Aside** pattern: `DriverService` checks Redis before calling `OrderService` for order data
- `OrderService` manages **cache eviction** (`@CacheEvict`) on order status changes, driver assignment, cancellation, and location updates
- Shared Redis instance with **`GenericJackson2JsonRedisSerializer`** for cross-service compatibility (Java objects serialized as JSON)
- Also used for **Redis GEO spatial index** (nearby driver queries), **active WebSocket sessions**, and **rate limiting**

> **Stack:** Redis · Spring Boot Data Redis · `@Cacheable` / `@CacheEvict`

---

### 7. 🏢 Multi-Tenant SaaS Architecture with RBAC
SwiftTrack is designed as a **true multi-tenant platform** powering businesses of any type:

- Each tenant (e-commerce, grocery, pharmacy, B2B) gets isolated data with their own drivers, providers, configs, and billing accounts
- Full **Role-Based Access Control (RBAC)**: `SUPER_ADMIN`, `SYSTEM_ADMIN`, `ADMIN_USER`, `TENANT_ADMIN`, `DISPATCHER`, `SUPPORT`, `FINANCE`, `DRIVER`, `CONSUMER`
- Tenants can configure **custom SLAs**, geofences, notification channels, and delivery provider preferences
- **AuthService** issues JWTs with embedded role + tenant context; every service validates via Feign client
- **Liquibase schema isolation**: each service owns its schema (`auth`, `driver`, `order`, `admin`, `billing`)

> **Stack:** Spring Boot · Spring Cloud Netflix Eureka · Spring Cloud OpenFeign · PostgreSQL (schema-per-service) · Liquibase

---

### 8. 🛡️ AdminService — Centralized Secure Administration Portal
A dedicated `AdminService` acts as a **single, secure gateway** for all administrative operations across the entire platform:

- Aggregates APIs from all downstream services (Auth, Driver, Order, Provider, Billing, AI Dispatch) via **Feign clients**
- Two-tier security: `requireAdmin()` and `requireSuperAdmin()` guards on every endpoint
- **Asynchronous audit logging** (`@Async`) — every admin action is immutably recorded without adding API latency
- Full CRUD for: user management, driver approval/rejection, provider configuration, billing settlement, wallet top-ups, margin configs
- **AI Dispatch control panel**: health monitoring and manual dispatch trigger for SUPER_ADMIN

> **Stack:** Java 25 · Spring Boot 3.5.8 · OpenFeign · PostgreSQL (admin schema) · Liquibase · Spring Async · SpringDoc OpenAPI

---

### 9. 📡 Async Parallel Order Quote Generation with CompletableFuture
High-performance order quoting with **parallel I/O** instead of sequential blocking calls:

- A single quote request fans out 4 concurrent calls: `reverseGeocode(dropoff)`, `reverseGeocode(pickup)`, `calculateDistance()`, `getTenantProviders()`
- Uses `CompletableFuture.supplyAsync()` + `CompletableFuture.allOf()` with strict **5-second timeout**
- Reduces quote response time from ~800ms (sequential) to ~200ms (parallel bottleneck)
- Fail-fast error handling: any single failure aborts the entire quote gracefully

> **Stack:** Java CompletableFuture · Spring Boot · OpenFeign (inter-service HTTP)

---

### 10. 🔧 MCP Gateway — AI Agent Developer Tools
SwiftTrack exposes an **MCP (Model Context Protocol) Gateway** built in Python, enabling LLM agents (like Claude, GPT, etc.) to interact with the SwiftTrack platform programmatically:

- Built as a standalone Python server with `tools/`, `core/`, and `utils/` modules
- LLM agents can create orders, check delivery status, assign providers, and run analytics — through natural language → tool calls
- Combined with the **AI Chatbot** and **Agentic Dashboard**, tenants can manage deliveries via text/voice prompts like:
  - *"Show me all failed deliveries today"*
  - *"Assign this order to Porter"*
  - *"What's the average delivery cost this week?"*

> **Stack:** Python · MCP (Model Context Protocol) · FastAPI · OpenAI/Anthropic-compatible tool schema

---

## 🛠️ Full Technology & Stack Reference

### Backend Microservices
| Service | Port | Language | Purpose |
|---|---|---|---|
| `AuthService` | 8001 | Java 25 / Spring Boot 3.5.8 | JWT auth, RBAC, user/tenant/driver registration |
| `TenantService` | 8002 | Java 25 / Spring Boot 3.5.8 | Tenant onboarding, settings, plan management |
| `ProviderService` | 8003 | Java 25 / Spring Boot 3.5.8 | 3rd-party delivery provider adapters |
| `OrderService` | 8004 | Java 25 / Spring Boot 3.5.8 | Order lifecycle, status machine, quote generation |
| `MapService` | 8005 | Java 25 / Spring Boot 3.5.8 | Geocoding, distance, routing |
| `DriverService` | 8007 | Java 25 / Spring Boot 3.5.8 | Driver onboarding, live tracking, KD-Tree dispatch |
| `BillingAndSettlementService` | 8008 | Java 25 / Spring Boot 3.5.8 | Wallets, invoices, settlements, margin configs |
| `AdminService` | 8009 | Java 25 / Spring Boot 3.5.8 | Centralized admin portal, audit logs |
| `AIDispatchService` | 8010 | Java 25 / Spring Boot 3.5.8 | LLM-powered driver selection pipeline |
| `EurekaServer` | 8761 | Java / Spring Cloud | Service registry & discovery |
| `GateWay` | 8080 | Java / Spring Cloud Gateway | API gateway, routing, load balancing |

### AI/ML Services
| Service | Language | Purpose |
|---|---|---|
| ML Assignment Service | Python / FastAPI | Provider success probability prediction (Random Forest) |
| MCP Gateway | Python | LLM agent tools for SwiftTrack operations |

### Core Java/Spring Technologies
- **Java 25** — latest LTS features (records, sealed classes, virtual threads)
- **Spring Boot 3.5.8** — application framework
- **Spring Cloud Netflix Eureka** — service discovery
- **Spring Cloud OpenFeign** — declarative HTTP clients (inter-service communication)
- **Spring Cloud Gateway** — API gateway
- **Spring AI 1.0.0** — LLM inference + embedding integration
- **Spring Kafka** — event-driven communication
- **Spring Data JPA / Hibernate** — ORM with PostgreSQL
- **Spring Data Redis** — caching, GEO, session management
- **Spring Async** — non-blocking operation execution
- **Liquibase** — database schema version control & migrations
- **SpringDoc OpenAPI (Swagger)** — auto-generated API documentation
- **MapStruct** — type-safe DTO mapping
- **Lombok** — boilerplate elimination

### AI / ML Technologies
- **Ollama** — local LLM inference runtime (no cloud API dependency)
- **Qwen2.5:3b-instruct** — chat model for driver dispatch decisions
- **nomic-embed-text** — embedding model (768-dim vectors for driver memory)
- **LangSmith** — versioned prompt registry with caching
- **Langfuse** — full LLM pipeline observability (traces, spans, generations)
- **Spring AI** — bridge between Spring and Ollama inference
- **pgvector** — PostgreSQL vector similarity search extension
- **Scikit-Learn** — Random Forest ML model for provider assignment
- **FastAPI (Python)** — ML inference serving

### Storage & Data Layer
| Technology | Role |
|---|---|
| **PostgreSQL (Supabase)** | Primary OLTP database — all services, schemas isolated per service |
| **pgvector** | Vector similarity search for driver memory embeddings |
| **Redis** | Real-time cache, GEO spatial index, WebSocket sessions, rate limiting |
| **Apache Kafka** | Event streaming backbone (order events, location updates, performance events) |
| **MinIO / AWS S3** | Object storage for KYC documents, delivery proofs, invoice PDFs |
| **ClickHouse** | (Phase 2) OLAP analytics database for BI dashboards |

### Infrastructure & DevOps
| Technology | Role |
|---|---|
| **Docker** | Containerization of all services |
| **Docker Compose** | Local dev orchestration (Kafka, Zookeeper, Redis, services) |
| **GitHub Actions** | CI/CD pipeline — ML model retraining, Docker build & push |
| **GitHub Container Registry (GHCR)** | Docker image registry |
| **Kubernetes** | Production deployment & horizontal scaling |
| **OpenTelemetry** | Distributed tracing and observability |
| **Maven** | Java project build management (multi-module) |

### Developer Tools
| Technology | Role |
|---|---|
| **MCP (Model Context Protocol)** | LLM agent integration API |
| **SDK** (JS, Java, Android) | Client SDKs for tenant integration |
| **Swagger / OpenAPI** | All services expose documented REST APIs |

---

## 🎯 What Makes SwiftTrack Portfolio-Worthy

### Unique Architectural Decisions
- **Immutable ML Model Pattern** — model is baked into Docker image at build time; zero runtime model store dependency
- **Self-Similarity RAG** — driver's own latest embedding is used as the query vector to find most representative memories (no order context needed at dispatch time)
- **400ms LLM budget** — real production-grade constraint: if the LLM pipeline exceeds budget, a deterministic fallback instantly kicks in
- **Lock-free KD-Tree** — `AtomicReference<KDTree>` allows reads without locking while background thread rebuilds; true concurrent spatial indexing
- **Schema-per-service in PostgreSQL** — each microservice owns its schema for isolation without running separate DB instances

### Scale & Production Readiness
- Designed to handle **millions of deliveries** with event-driven horizontal scaling
- Sub-50ms Redis cache responses for live tracking
- Non-blocking audit logging that never adds latency to admin API responses
- All AI pipeline calls are traced end-to-end via Langfuse with input/output metadata

### Responsible AI Design
- **Driver Reliability Index (DRI)** — behavior-first assignment (not just nearest-driver)
- **Fairness guardrails** — no protected attributes in ML inputs
- **Human-in-the-loop** — dispatchers can override AI decisions
- **Explainability** — every dispatch decision includes a `reason` field traceable in Langfuse
- **Deterministic fallback** — system never fails due to LLM unavailability
- **AI Governance Layer** — model registry, risk classification, shadow rollout before production

---

## 📊 Microservice Count Summary

| Domain | Services |
|---|---|
| Tenant & SaaS Domain | AuthService, TenantService, AdminService |
| Delivery Orchestration | OrderService, ProviderService, MapService |
| Driver Domain | DriverService, BillingAndSettlementService |
| AI/ML Intelligence | AIDispatchService, ML Assignment (FastAPI) |
| Infrastructure | EurekaServer, GateWay |
| Developer Tools | MCP Gateway (Python) |
| **Total** | **13 services** |

---

*Built with Java 25 · Spring Boot 3.5.8 · Spring AI 1.0.0 · Python · FastAPI · Apache Kafka · Redis · PostgreSQL · pgvector · Ollama · LangSmith · Langfuse · Docker · Kubernetes · GitHub Actions*
