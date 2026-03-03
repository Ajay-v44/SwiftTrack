# AI Dispatch Service — Technical Documentation

## ✅ Status: Complete & Compiled

---

## 1️⃣ Overview

The **AI Dispatch Service** (`AIDispatchService`) is a production-ready Spring Boot microservice that performs **AI-based driver selection** for SwiftTrack's logistics platform. It receives a list of candidate driver IDs (pre-filtered by a KD-tree in the Driver Service) and uses an LLM-powered pipeline to select the optimal driver for dispatch.

### Key Design Principles

| Principle | Implementation |
|---|---|
| **Stateless** | No session, no in-memory state between requests |
| **Data isolation** | LLM NEVER queries the database directly |
| **Observability** | Every pipeline step traced via Langfuse |
| **Resilient** | Deterministic fallback if LLM fails or times out |
| **Local inference** | Ollama (local SLM) — no cloud API dependency |

---

## 2️⃣ Architecture

```
                    ┌─────────────────────────────────────────────────┐
                    │            AI Dispatch Service (8010)           │
                    │                                                 │
  POST /dispatch    │  ┌──────────┐    ┌──────────┐    ┌──────────┐  │
  /assign           │  │ Controller│───▶│ Dispatch │───▶│ Prompt   │  │
  { driverIds[] }──▶│  │          │    │ Service  │    │ Assembler│  │
                    │  └──────────┘    └────┬─────┘    └────┬─────┘  │
                    │                       │               │         │
                    │         ┌─────────────┼───────────────┤         │
                    │         ▼             ▼               ▼         │
                    │  ┌──────────┐  ┌──────────┐   ┌──────────────┐ │
                    │  │ Driver   │  │ Memory   │   │  LangSmith   │ │
                    │  │ Profile  │  │ Repo     │   │  Prompt      │ │
                    │  │ Repo     │  │ (pgvec)  │   │  Fetcher     │ │
                    │  └────┬─────┘  └────┬─────┘   └──────────────┘ │
                    │       │             │                           │
                    │       ▼             ▼                           │
                    │  ┌────────────────────────┐                    │
                    │  │   Supabase PostgreSQL   │                    │
                    │  │   (pgvector enabled)    │                    │
                    │  └────────────────────────┘                    │
                    │                                                 │
                    │  ┌──────────┐         ┌──────────┐             │
                    │  │ Ollama   │         │ Langfuse │             │
                    │  │ Inference│         │ Client   │             │
                    │  │ Client   │         │ (async)  │             │
                    │  └────┬─────┘         └────┬─────┘             │
                    │       │                    │                    │
                    └───────┼────────────────────┼────────────────────┘
                            ▼                    ▼
                    ┌──────────────┐     ┌──────────────┐
                    │ Ollama       │     │ Langfuse     │
                    │ localhost:   │     │ cloud API    │
                    │ 11434        │     │              │
                    │ qwen2.5:3b   │     │              │
                    └──────────────┘     └──────────────┘
```

---

## 3️⃣ Dispatch Pipeline

The service executes a **9-step pipeline** for every dispatch request:

| Step | Operation | Data Source | Langfuse Span |
|------|-----------|-------------|---------------|
| 1 | Fetch prompts | LangSmith API (cached) | `fetch_prompts` |
| 2 | Fetch driver profiles | Supabase PostgreSQL | `fetch_driver_profiles` |
| 3 | Retrieve driver memories (RAG) | pgvector similarity search | `retrieve_driver_memory` |
| 4 | Assemble LLM context | In-memory prompt assembly | — |
| 5 | Check timeout budget | System clock | — |
| 6 | Call Ollama inference | localhost:11434 | `llm_inference` |
| 7 | Validate/fix output | Ollama (if needed) | `validation` |
| 8 | Verify driver in candidate list | In-memory check | — |
| 9 | Fallback (if all else fails) | In-memory sort | `fallback_if_triggered` |

### Timeout Budget

- **Pipeline total**: 400ms max
- **Inference target**: < 200ms
- If the pipeline exceeds the timeout before inference, fallback is triggered immediately.

---

## 4️⃣ API Reference

### `POST /dispatch/assign`

**Request:**
```json
{
  "driverIds": [
    "550e8400-e29b-41d4-a716-446655440001",
    "550e8400-e29b-41d4-a716-446655440002",
    "550e8400-e29b-41d4-a716-446655440003"
  ]
}
```

**Constraints:**
- `driverIds` must not be empty
- Maximum 5 driver IDs (additional are truncated)

**Response (Success — LLM decision):**
```json
{
  "driver_id": "550e8400-e29b-41d4-a716-446655440002",
  "confidence": 0.87,
  "reason": "Highest acceptance rate (92%) with shortest distance (1.2km) and strong SLA adherence (95%)",
  "fallback": false,
  "latency_ms": 312
}
```

**Response (Fallback — LLM failure):**
```json
{
  "driver_id": "550e8400-e29b-41d4-a716-446655440001",
  "confidence": 0.5,
  "reason": "Fallback: LLM output validation failed. Selected driver with lowest cancellation rate (0.05)",
  "fallback": true,
  "latency_ms": 45
}
```

**Error Response:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "driverIds: must not be empty",
  "timestamp": "2026-03-01T12:00:00Z"
}
```

### `GET /dispatch/health`
Returns `200 OK` with `"AI Dispatch Service is running"`.

### Swagger UI
Available at: `http://localhost:8010/swagger-ui.html`

---

## 5️⃣ Technology Stack

| Component | Technology | Purpose |
|---|---|---|
| **Runtime** | Spring Boot 3.5.8 | Application framework |
| **LLM Inference** | Ollama + Spring AI 1.0.0 | Local model inference |
| **Chat Model** | `qwen2.5:3b-instruct` | Decision-making SLM |
| **Prompt Registry** | LangSmith API | Versioned prompt storage |
| **Vector Search** | Supabase pgvector | RAG memory retrieval |
| **Observability** | Langfuse Cloud | Full pipeline tracing |
| **Service Discovery** | Netflix Eureka | Microservice registration |
| **API Documentation** | SpringDoc OpenAPI | Swagger UI |
| **HTTP Client** | OkHttp | LangSmith & Langfuse API calls |
| **Database** | Supabase PostgreSQL | Driver metrics & embeddings |

---

## 6️⃣ Project Structure

```
AIDispatchService/
├── docs/
│   └── README.md                          # This file
├── src/main/
│   ├── java/com/swifttrack/AIDispatchService/
│   │   ├── AIDispatchServiceApplication.java   # Main entry point
│   │   ├── conf/
│   │   │   ├── EnvConfiguration.java           # .env file loader
│   │   │   ├── EnvPropertySourceFactory.java   # Custom property source
│   │   │   └── OpenApiConfig.java              # Swagger configuration
│   │   ├── controllers/
│   │   │   └── DispatchController.java         # REST endpoint
│   │   ├── dto/
│   │   │   ├── DispatchRequest.java            # Input DTO (record)
│   │   │   ├── DispatchResponse.java           # Output DTO
│   │   │   ├── DriverProfile.java              # Driver metrics DTO
│   │   │   ├── DriverMemorySummary.java        # RAG memory DTO
│   │   │   └── LlmDecision.java               # LLM output DTO
│   │   ├── exception/
│   │   │   ├── DispatchTimeoutException.java   # Timeout exception
│   │   │   └── GlobalExceptionHandler.java     # Error handler
│   │   ├── langchain/
│   │   │   ├── LangSmithPromptFetcher.java     # Prompt registry client
│   │   │   ├── OllamaInferenceClient.java      # LLM inference wrapper
│   │   │   └── PromptAssembler.java            # Context assembler
│   │   ├── observability/
│   │   │   └── LangfuseClient.java             # Trace & span logging
│   │   ├── repositories/
│   │   │   ├── DriverProfileRepository.java    # SQL metrics fetch
│   │   │   └── DriverMemoryRepository.java     # pgvector RAG retrieval
│   │   └── services/
│   │       └── DispatchService.java            # Core pipeline orchestrator
│   └── resources/
│       └── application.yaml                    # Configuration
├── .env                                         # API keys & DB creds
└── pom.xml                                      # Maven dependencies
```

---

## 7️⃣ Configuration

### Environment Variables (`.env`)

| Variable | Description |
|---|---|
| `DB_HOST` | Supabase PostgreSQL host |
| `DB_PORT` | Database port (default: 5432) |
| `DB_NAME` | Database name |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `LANGSMITH_API_KEY` | LangSmith API key for prompt registry |
| `LANGFUSE_SECRET_KEY` | Langfuse secret key |
| `LANGFUSE_PUBLIC_KEY` | Langfuse public key |

### Application Properties

| Property | Default | Description |
|---|---|---|
| `server.port` | `8010` | Service port |
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama endpoint |
| `spring.ai.ollama.chat.options.model` | `qwen2.5:3b-instruct` | Chat model |
| `spring.ai.ollama.chat.options.temperature` | `0.1` | Low for determinism |
| `dispatch.max-drivers` | `5` | Max candidates per request |
| `dispatch.max-memory-per-driver` | `3` | Max RAG memories per driver |
| `dispatch.pipeline-timeout-ms` | `400` | Pipeline budget |
| `dispatch.inference-timeout-ms` | `200` | LLM inference timeout |

---

## 8️⃣ LangSmith Prompt Registry

Three prompts are registered in LangSmith:

| Prompt Name | Purpose | Variables |
|---|---|---|
| `dispatch_system_v1` | System context for the LLM | None |
| `dispatch_decision_v1` | Decision instructions with data | `{driver_profiles}`, `{driver_memories}` |
| `dispatch_validator_v1` | Fix malformed JSON output | `{raw_output}` |

### Fallback Behavior

If LangSmith is unreachable, hardcoded fallback prompts are used automatically. This ensures the service **never fails** due to prompt registry unavailability.

### Prompt Cache

- Prompts are cached in-memory after first successful fetch
- Cache is a `ConcurrentHashMap` (thread-safe)
- Cache can be invalidated programmatically via `LangSmithPromptFetcher.invalidateCache()`

---

## 9️⃣ RAG (Retrieval-Augmented Generation)

### Data Flow

```
Driver Service (embedding pipeline)
    │
    ▼
┌─────────────────────────────┐
│  driver_memory table        │
│  - id (UUID)                │
│  - driver_id (UUID)         │
│  - summary (TEXT)           │
│  - embedding (vector(768))  │
│  - created_at (TIMESTAMP)   │
└──────────┬──────────────────┘
           │
           ▼ pgvector cosine similarity
┌─────────────────────────────┐
│  DriverMemoryRepository     │
│  - findTopMemories()        │  ← Uses latest embedding as query
│  - findRecentMemories()     │  ← Fallback: by created_at
└─────────────────────────────┘
```

### Retrieval Strategy

1. **Primary**: Use the driver's **latest embedding** as the query vector for self-similarity search. Returns top N most relevant memories.
2. **Fallback**: If no embedding exists for a driver, fall back to **recency-based** retrieval (most recent memories by `created_at`).

### Why Self-Similarity?

Instead of embedding the incoming order context (which doesn't exist at dispatch time), we use the driver's latest behavioral embedding to find the most representative memories. This captures the driver's **recent behavioral pattern**.

---

## 🔟 Fallback Logic

When the LLM fails (malformed output, unknown driver ID, pipeline timeout), the service uses a **deterministic fallback**:

### Strategy: Lowest Cancellation Rate → Shortest Distance

```
1. Sort candidates by cancellation_rate ASC
2. Tie-break by distance ASC
3. Select first driver
4. Set confidence = 0.5 (profile-based) or 0.3 (emergency)
5. Set fallback = true
6. Include reason text explaining why fallback was triggered
```

### Fallback Trigger Points

| Trigger | Confidence | Strategy |
|---|---|---|
| No driver profiles found | 0.3 | First available driver ID |
| Pipeline timeout exceeded | 0.5 | Lowest cancellation rate |
| LLM returned null/empty | 0.5 | Lowest cancellation rate |
| Malformed JSON (after validation retry) | 0.5 | Lowest cancellation rate |
| LLM selected unknown driver | 0.5 | Lowest cancellation rate |
| Unhandled exception | 0.3 | First available driver ID |

---

## 1️⃣1️⃣ Langfuse Observability

Every dispatch request creates **one trace** with multiple **spans**:

```
Trace: dispatch_assign (traceId)
├── Span: fetch_prompts
├── Span: fetch_driver_profiles
├── Span: retrieve_driver_memory
├── Generation: llm_inference (includes model, latency)
├── Span: validation (only if first parse failed)
└── Span: fallback_if_triggered (only if fallback used)
```

### Key Metadata Logged

- Input driver IDs
- Number of profiles found
- Total memories retrieved
- Raw LLM output
- Inference latency
- Total pipeline latency
- Fallback reason (if any)

### Non-Blocking

All Langfuse calls are `@Async` — observability **never** blocks the dispatch pipeline.

---

## 1️⃣2️⃣ Data Flow Diagram

```
 Client (Driver Service)
    │
    │  POST /dispatch/assign { driverIds: [...] }
    │
    ▼
 DispatchController
    │
    ▼
 DispatchService.dispatch()
    │
    ├─── (1) PromptAssembler.assembleSystemPrompt()
    │         └── LangSmithPromptFetcher.fetchPrompt("dispatch_system_v1")
    │              └── HTTP GET → LangSmith API (cached)
    │
    ├─── (2) DriverProfileRepository.fetchDriverProfiles()
    │         └── JdbcTemplate → Supabase PostgreSQL
    │              └── CTE: acceptance_rate, cancellation_rate,
    │                  sla_adherence, rating, idle_time
    │
    ├─── (3) DriverMemoryRepository.findTopMemories()
    │         └── JdbcTemplate → pgvector cosine similarity
    │
    ├─── (4) PromptAssembler.assembleDecisionPrompt()
    │         └── {driver_profiles} + {driver_memories} → template
    │
    ├─── (5) Timeout check (elapsed < 400ms?)
    │
    ├─── (6) OllamaInferenceClient.infer()
    │         └── Spring AI ChatModel → Ollama localhost:11434
    │              └── Model: qwen2.5:3b-instruct (temp: 0.1)
    │
    ├─── (7) Parse JSON → LlmDecision
    │         └── If malformed: OllamaInferenceClient.validate()
    │
    ├─── (8) Verify driver_id ∈ driverIds
    │
    └─── (9) If all failed: Fallback logic
              └── lowest cancellation_rate, tie-break shortest distance
```

---

## 1️⃣3️⃣ Security Constraints

| Constraint | Implementation |
|---|---|
| **No direct LLM DB access** | All SQL in `*Repository` classes only |
| **No sensitive data in prompts** | Only aggregated metrics sent to LLM |
| **API keys not in code** | Loaded from `.env` file |
| **No internal errors exposed** | `GlobalExceptionHandler` sanitizes responses |
| **No cross-driver data leakage** | Each driver's data fetched independently |
| **Observability is async** | Langfuse failures never break dispatch |

---

## 1️⃣4️⃣ Performance Targets

| Metric | Target | Implementation |
|---|---|---|
| Max candidates | 5 drivers | `dispatch.max-drivers` config |
| Max memories per driver | 3 | `dispatch.max-memory-per-driver` config |
| Inference latency | < 200ms | Ollama local, temp=0.1, small model |
| Total pipeline latency | < 400ms | Timeout budget with early fallback |
| Prompt fetch | < 50ms | In-memory cache after first fetch |
| DB queries | < 50ms each | JdbcTemplate with parameterized SQL |

---

## 1️⃣5️⃣ Prerequisites

Before running this service, ensure:

1. **Ollama** is installed and running locally

   ```bash
   ollama serve  # Should be running on :11434
   ```

2. **Models are pulled** (do NOT re-pull):

   ```bash
   # Already pulled:
   # qwen2.5:3b-instruct  (chat)
   # nomic-embed-text      (embeddings — used by Driver Service)
   ```

3. **Supabase PostgreSQL** is accessible with:
   - `pgvector` extension enabled
   - `driver_memory` table with embeddings
   - Driver tables (`driver_vehicle_details`, `driver_status`, etc.)

4. **LangSmith** prompts are registered:
   - `dispatch_system_v1`
   - `dispatch_decision_v1`
   - `dispatch_validator_v1`
   - (Service works with fallback prompts if not registered)

5. **Eureka Server** is running (for service discovery)

---

## 1️⃣6️⃣ Running the Service

```bash
# From the project root
cd backend/services/AIDispatchService

# Compile
mvn compile

# Run
mvn spring-boot:run
```

The service will start on port **8010** and register with Eureka as `AIDispatchService`.

### Testing with cURL

```bash
curl -X POST http://localhost:8010/dispatch/assign \
  -H "Content-Type: application/json" \
  -d '{
    "driverIds": [
      "550e8400-e29b-41d4-a716-446655440001",
      "550e8400-e29b-41d4-a716-446655440002"
    ]
  }'
```

---

## 1️⃣7️⃣ Integration Points

### Upstream (Who calls this service)

| Caller | When | How |
|---|---|---|
| **Driver Service** | After KD-tree proximity filter | REST call to `POST /dispatch/assign` |
| **Order Service** | During order creation pipeline | Via Driver Service |

### Downstream (What this service calls)

| Target | Purpose | Protocol |
|---|---|---|
| **Supabase PostgreSQL** | Driver profiles + memories | JDBC |
| **Ollama** | LLM inference | HTTP (Spring AI) |
| **LangSmith** | Prompt templates | HTTP (OkHttp) |
| **Langfuse** | Observability traces | HTTP (OkHttp, async) |

---

## 1️⃣8️⃣ Driver Metrics Used

The LLM receives these pre-computed metrics per driver:

| Metric | Source | Weight |
|---|---|---|
| `distance_km` | KD-tree (pre-computed) | 25% |
| `acceptance_rate` | `driver_order_assignment` aggregation | 20% |
| `cancellation_rate` | `driver_order_cancellation` count | 20% |
| `sla_adherence` | Completed / (Completed + Accepted) ratio | 15% |
| `rating` | `driver_events` (RATING_UPDATED metadata) | 10% |
| `idle_time_minutes` | `driver_status.last_seen_at` delta | 10% |

### Memory Summaries (RAG)

In addition to metrics, the LLM receives up to 3 behavioral memory summaries per driver. These are human-readable text embeddings generated by the Driver Service's embedding pipeline.

Example memory: `"Driver consistently completes orders within 15 minutes. High acceptance rate during peak hours. Prefers short-distance deliveries."`

---

## 1️⃣9️⃣ What This Service Does NOT Do

| Concern | Handled By |
|---|---|
| Generate embeddings | Driver Service (nomic-embed-text) |
| Run KD-tree proximity search | Driver Service |
| Store dispatch results | Caller's responsibility |
| Manage driver state | Driver Service |
| Authenticate requests | API Gateway / Auth Service |
| Use LangGraph | Explicitly excluded per requirements |
| Allow LLM to query database | Prohibited — all data fetched in app layer |
