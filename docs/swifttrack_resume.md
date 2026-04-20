# SwiftTrack — Resume Entry (Full)

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 25 · Spring Boot 4 · Spring Cloud (Eureka, Gateway, OpenFeign) · Spring Security · Spring AI |
| **Databases** | PostgreSQL · pgvector (vector similarity search) |
| **Messaging / Events** | Apache Kafka (multi-topic event streaming) |
| **Schema Migrations** | Liquibase |
| **AI / ML** | Ollama (local LLM · qwen2.5:3b-instruct) · Spring AI · LangSmith (prompt registry) · Langfuse (LLM tracing) |
| **ML Microservice** | Python · scikit-learn (Random Forest) · FastAPI · Docker · GHCR |
| **MCP Server** | Python · FastMCP · `swifttrack-mcp` (auth, provider, map tools) |
| **Python SDK** | `swifttrack` on PyPI · httpx · pydantic v2 · mypy strict · ruff |
| **Frontend Web** | Next.js · TypeScript · TanStack Query · Zustand · React Hook Form + Zod |
| **Frontend Mobile** | React Native (Expo) · Expo Router · NativeWind |
| **Monorepo Tooling** | Nx (incremental build + cache-aware CI) |
| **Auth** | JWT (jjwt) · BCrypt · RBAC (8 user roles) |
| **DevOps / CI-CD** | GitHub Actions · OIDC Trusted Publishing · `pypa/gh-action-pypi-publish` · Docker Build & Push · GHCR · Maven multi-module build |
| **Observability** | Langfuse distributed tracing · Spring Boot Actuator · Slf4j structured logging |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |

---

## 📌 5 Resume Bullets

---

### 1. Microservices Architecture with Service Discovery & API Gateway

Architected a **cloud-native microservices platform** comprising 12 independently deployable Spring Boot services (Auth, Order, Driver, Billing, AIDispatch, Provider, Tenant, Map, Notification, Admin) orchestrated via **Netflix Eureka** service discovery and a **Spring Cloud Gateway** as the unified API entry point. Built a shared `common` Maven module as an internal SDK (cross-service DTOs, enums, Feign clients, custom exceptions), enforcing contract consistency. Services communicate via **OpenFeign** (sync) and **Apache Kafka** (async event streaming) across 4 dedicated topics, ensuring loose coupling and failure isolation.

**Resume bullet:**
```
Designed a 12-service Spring Boot microservices platform using Spring Cloud Gateway,
Netflix Eureka, and a shared internal Maven SDK — enabling independent deployment of Auth,
Order, Driver, Billing, AIDispatch, and Provider services with zero cross-service coupling.
```

---

### 2. Event-Driven Financial Ledger with Idempotency, SERIALIZABLE Isolation & Reconciliation

Engineered a **double-entry financial ledger** that consumes Kafka `order-delivered` events under **SERIALIZABLE transaction isolation**, atomically splitting each delivery into three ledger entries: tenant DEBIT, provider/driver CREDIT, platform margin CREDIT. All writes carry **idempotency keys** (`ORDER-{id}-TENANT-DEBIT` etc.) to guarantee exactly-once semantics on Kafka retry. Settlement lifecycle enforces a strict state machine (`PENDING → PROCESSING → SETTLED/FAILED`) with Kafka-published `settlement.initiated` events to the payment gateway and a `settlement.status.update` consumer that triggers automatic reversal on failure. Implemented a `reconcileBalance()` API that recomputes account balances from raw ledger aggregates to detect and correct partial-write anomalies.

**Resume bullet:**
```
Built an event-driven double-entry financial ledger (Kafka + PostgreSQL) with SERIALIZABLE
isolation, per-transaction idempotency keys, strict settlement state-machine transitions, and
a balance reconciliation API — managing real-money billing, payouts, and failure reversals
across tenant, driver, and platform accounts.
```

---

### 3. AI Dispatch Pipeline with LangChain, pgvector RAG & Langfuse Observability

Built an **AI driver dispatch pipeline** (AIDispatchService) using **Spring AI's ChatClient** over a locally-hosted **Ollama LLM** (qwen2.5:3b-instruct). Fetches versioned prompt templates from **LangSmith**, enriches them with driver behavioral memories via **pgvector similarity search** (RAG), and feeds structured driver profiles for ranked decision-making. `BeanOutputConverter` handles structured JSON output parsing with a **self-healing validation chain** (`dispatch_validator_v1`) for malformed LLM responses. Includes deterministic fallback (lowest cancellation rate → shortest distance). The full 8-step pipeline is traced end-to-end in **Langfuse** (trace → spans → generation) via async, non-blocking telemetry.

**Resume bullet:**
```
Engineered an AI driver dispatch pipeline using Spring AI + Ollama LLM with LangSmith
prompt versioning, pgvector RAG memory retrieval, structured output with self-healing
validation retry, deterministic fallback, and full end-to-end Langfuse distributed
tracing — achieving reliable AI assignment with zero observability overhead.
```

---

### 4. Published Python SDK + MCP Server to PyPI via OIDC CI/CD

Developed and shipped a **production-grade Python SDK** (`swifttrack`, published at [pypi.org/project/swifttrack](https://pypi.org/project/swifttrack/)) wrapping all platform REST APIs with typed Pydantic v2 models, async httpx transport, and strict mypy type safety. Automated the full publish pipeline using **GitHub Actions** with **OIDC Trusted Publishing** (no long-lived secrets — short-lived tokens via `pypa/gh-action-pypi-publish`), a matrix test run across **Python 3.9–3.13**, ruff linting, mypy type checking, Codecov coverage upload, and auto-incrementing patch versions on every `main` push. Also built a **FastMCP Model Context Protocol (MCP) server** (`swifttrack-mcp`) exposing auth, provider management, and map tools so AI agents (Claude, GPT, etc.) can natively call SwiftTrack APIs.

**Resume bullet:**
```
Built and shipped a typed Python SDK (`pip install swifttrack`) via a GitHub Actions CI/CD
pipeline using OIDC Trusted Publishing to PyPI — with matrix testing across Python 3.9–3.13,
ruff + mypy enforcement, auto-version increments, and a FastMCP server exposing platform APIs
as AI agent tools (auth, provider, map).
```

---

### 5. Automated ML Retraining with Docker + GHCR and Cross-Platform Nx Monorepo

Engineered an **ML assignment microservice** (scikit-learn Random Forest) predicting provider assignment success probabilities, following a **Training–Inference Separation** pattern: daily retraining is triggered by a `cron`-scheduled GitHub Actions workflow, trains the model with versioned run numbers (`1.0.<RUN_NUMBER>`), bakes the artifact into an **immutable Docker image**, and publishes it to **GitHub Container Registry (GHCR)** — enabling one-command rollback to any prior model. Structured the frontend as a **Nx monorepo** sharing a TypeScript API client, types, and hooks across both a Next.js web dashboard and a React Native (Expo) mobile driver app, with incremental, cache-aware CI builds.

**Resume bullet:**
```
Implemented a daily-retrained ML assignment service (Random Forest) with Training–Inference
Separation: GitHub Actions builds a versioned Docker image per run and pushes to GHCR for
one-command rollback — alongside an Nx monorepo frontend sharing TypeScript API clients
across Next.js web and React Native (Expo) mobile apps with cache-aware CI.
```

---

## 🔁 Kafka Event System Topology

```
OrderService  ──[order-delivered]──►  BillingService
                                          │
                                     Atomically:
                                     • DEBIT  Tenant Account
                                     • CREDIT Driver/Provider Account
                                     • CREDIT Platform Account
                                          │
                                     [settlement.initiated]──► Payment Gateway
                                          │
Payment Gateway ──[settlement.status.update]──► BillingService
                                                    │
                                               SETTLED → ledger DEBIT
                                               FAILED  → automatic reversal

AIDispatchService  (internal, pgvector RAG + Ollama LLM, no Kafka)
```

**Topics summary:**

| Topic | Producer | Consumer | Purpose |
|---|---|---|---|
| `order-delivered` | OrderService | BillingService | Trigger 3-way fund split |
| `settlement.initiated` | BillingService | Payment Gateway | Start bank transfer |
| `settlement.status.update` | Payment Gateway | BillingService | Mark settled / trigger reversal |

---

## 🚀 DevOps Practices Summary

| Practice | Implementation |
|---|---|
| **Secretless CI** | OIDC Trusted Publishing — no stored PyPI tokens, GitHub exchanges short-lived JWT with PyPI |
| **Environment protection** | `pypi` GitHub Environment requires branch policy (main-only) |
| **Matrix testing** | SDK tested on Python 3.9, 3.10, 3.11, 3.12, 3.13 in parallel |
| **Auto-versioning** | Patch version auto-incremented (`major.minor.patch.devN`) on every main push |
| **Artifact staging** | Build → `twine check` → install verification → upload artifact → publish |
| **ML retraining** | Cron-triggered daily retrain, versioned Docker image baked with model artifact |
| **Container registry** | GHCR (GitHub Container Registry) with semantic tagging for rollback |
| **Build caching** | Nx task graph for incremental frontend builds, pip cache in GitHub Actions |
| **Code quality gates** | ruff (lint + format), mypy strict mode, pytest with coverage → Codecov |
| **Concurrency control** | `cancel-in-progress: true` prevents duplicate workflow runs on rapid pushes |

---

## 📋 Final Resume Entry (Copy-Paste Ready)

**SwiftTrack** | Full-Stack Logistics & AI-Dispatch Platform | *Java · Spring Boot · Spring AI · Kafka · PostgreSQL · pgvector · Python · FastMCP · Next.js · React Native · Nx · GitHub Actions*

- Designed a **12-service Spring Boot microservices platform** using Spring Cloud Gateway, Netflix Eureka, and a shared internal Maven SDK, enabling independent deployment with zero cross-service coupling.
- Architected a **fully event-driven system** across 9 Kafka topics covering the entire platform lifecycle — order creation, AI dispatch assignment, driver acceptance/cancellation, real-time GPS location streaming, delivery completion, automatic billing (3-way ledger split with SERIALIZABLE isolation + idempotency keys), settlement initiation, and payment gateway callbacks — with each service reacting asynchronously and independently to domain events.
- Engineered an **AI driver dispatch pipeline** (Spring AI + Ollama LLM) with LangSmith prompt versioning, pgvector RAG memory, self-healing structured output parsing, and full **Langfuse distributed tracing** across every pipeline stage.
- Published a **typed Python SDK** (`pip install swifttrack`) and **FastMCP server** via GitHub Actions OIDC Trusted Publishing to PyPI — with matrix tests across Python 3.9–3.13, ruff/mypy enforcement, and auto-versioning on every merge.
- Implemented **daily-retrained ML assignment** (Random Forest · GHCR versioned Docker images for one-command rollback) alongside an **Nx monorepo** frontend sharing TypeScript API clients across Next.js web and React Native (Expo) driver apps with cache-aware incremental CI builds.
