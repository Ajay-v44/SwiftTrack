# AI Dispatch Service — Technical Documentation

## ✅ Status: Complete & Compiled (LangChain Refactored)

---

## 1️⃣ Overview

The **AI Dispatch Service** (`AIDispatchService`) is a production-ready Spring Boot microservice that performs **AI-based driver selection** for SwiftTrack's logistics platform. It receives a list of candidate driver IDs (pre-filtered by a KD-tree in the Driver Service) and uses a LangChain-powered pipeline to select the optimal driver.

### Key Design Principles

| Principle | Implementation |
|---|---|
| **LangChain-first** | Spring AI ChatClient handles prompt lifecycle, variable injection, model execution, and structured output parsing |
| **Stateless** | No session, no in-memory state between requests |
| **Data isolation** | LLM NEVER queries the database directly |
| **Observability** | Every pipeline step traced via Langfuse |
| **Resilient** | Deterministic fallback if LLM fails or times out |
| **Local inference** | Ollama (local SLM) — no cloud API dependency |

---

## 2️⃣ Architecture

```
                    ┌─────────────────────────────────────────────────────┐
                    │              AI Dispatch Service (8010)             │
                    │                                                     │
  POST /dispatch    │  ┌──────────┐    ┌─────────────────┐               │
  /assign           │  │Controller│───▶│ DispatchService  │               │
  { driverIds[] }──▶│  │          │    │ (business logic) │               │
                    │  └──────────┘    └────┬──────┬──────┘               │
                    │                       │      │                      │
                    │         ┌─────────────┤      ├──────────────┐       │
                    │         ▼             │      ▼              ▼       │
                    │  ┌──────────┐  ┌──────┴────────────┐  ┌──────────┐ │
                    │  │ Driver   │  │DispatchChain       │  │ Langfuse │ │
                    │  │ Profile  │  │Executor            │  │ Client   │ │
                    │  │ Repo     │  │(LangChain)         │  │ (async)  │ │
                    │  ├──────────┤  │                    │  └──────────┘ │
                    │  │ Driver   │  │ ┌─────────────┐   │               │
                    │  │ Memory   │  │ │ ChatClient   │   │               │
                    │  │ Repo     │  │ │ .system()    │   │               │
                    │  │ (pgvec)  │  │ │ .user(.param)│   │               │
                    │  ├──────────┤  │ │ .call()      │   │               │
                    │  │ Data     │  │ │ .entity()    │   │               │
                    │  │Serializer│  │ └──────┬───────┘   │               │
                    │  └────┬─────┘  └────────┼───────────┘               │
                    │       │                 │                            │
                    │       ▼                 ▼                            │
                    │  ┌──────────────┐  ┌──────────────┐                 │
                    │  │  Supabase    │  │ LangSmith    │                 │
                    │  │  PostgreSQL  │  │ Prompt       │                 │
                    │  │  (pgvector)  │  │ Registry     │                 │
                    │  └──────────────┘  └──────────────┘                 │
                    │                         │                            │
                    │                    ┌────┴─────┐                     │
                    │                    │ Ollama   │                     │
                    │                    │ :11434   │                     │
                    │                    │ qwen2.5  │                     │
                    │                    └──────────┘                     │
                    └─────────────────────────────────────────────────────┘
```

---

## 3️⃣ LangChain Integration (Spring AI)

### Before Refactoring (❌ Manual)

```java
// Old: Manual string replacement
String assembled = template
    .replace("{driver_profiles}", profilesJson)
    .replace("{driver_memories}", memoriesJson);

// Old: Manual model call
Prompt prompt = new Prompt(List.of(new SystemMessage(sys), new UserMessage(user)),
    OllamaOptions.builder().model("qwen2.5:3b-instruct").build());
ChatResponse response = chatModel.call(prompt);

// Old: Manual JSON parsing
String raw = response.getResult().getOutput().getText();
LlmDecision decision = objectMapper.readValue(cleaned, LlmDecision.class);
```

### After Refactoring (✅ LangChain)

```java
// New: Single ChatClient chain handles everything
LlmDecision decision = chatClient.prompt()
    .system(systemTemplate)
    .user(u -> u
        .text(decisionTemplate)
        .param("driver_profiles", profilesJson)
        .param("driver_memories", memoriesJson))
    .call()
    .entity(LlmDecision.class);
```

### What LangChain Handles

| Concern | Spring AI Abstraction |
|---|---|
| Prompt composition | `ChatClient.prompt().system().user()` |
| Variable injection | `.param("key", value)` via TemplateRenderer |
| Model execution | `.call()` backed by Ollama ChatModel |
| Structured output | `.entity(LlmDecision.class)` via BeanOutputConverter |
| JSON schema | Auto-generated from `LlmDecision` record |

---

## 4️⃣ Dispatch Pipeline

| Step | Owner | Operation | Langfuse Span |
|------|-------|-----------|---------------|
| 1 | DispatchService | Fetch driver profiles | `fetch_driver_profiles` |
| 2 | DispatchService | Retrieve driver memories (RAG) | `retrieve_driver_memory` |
| 3 | DriverDataSerializer | Serialize profiles + memories to JSON | — |
| 4 | DispatchService | Check timeout budget | — |
| 5 | **DispatchChainExecutor** | Execute LangChain dispatch chain | `llm_inference` |
| 6 | **DispatchChainExecutor** | Validation chain (if parsing failed) | `validation` |
| 7 | DispatchService | Verify driver_id ∈ candidate list | — |
| 8 | DispatchService | Fallback (if still invalid) | `fallback_if_triggered` |

### Clean Separation of Concerns

```
DispatchService          → Business logic orchestration
DispatchChainExecutor    → AI execution lifecycle (LangChain)
DriverDataSerializer     → Business data → JSON
LangSmithPromptFetcher   → Prompt registry client
LangfuseClient           → Observability infrastructure
```

---

## 5️⃣ API Reference

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

**Response (Success — LlmDecision via LangChain):**
```json
{
  "driver_id": "550e8400-e29b-41d4-a716-446655440002",
  "confidence": 0.87,
  "reason": "Highest acceptance rate with shortest distance and strong SLA adherence",
  "fallback": false,
  "latency_ms": 312
}
```

**Response (Fallback):**
```json
{
  "driver_id": "550e8400-e29b-41d4-a716-446655440001",
  "confidence": 0.5,
  "reason": "Fallback: LLM output validation failed. Selected driver with lowest cancellation rate (0.05)",
  "fallback": true,
  "latency_ms": 45
}
```

### `GET /dispatch/health`
Returns `200 OK` with `"AI Dispatch Service is running"`.

### Swagger UI
Available at: `http://localhost:8010/swagger-ui.html`

---

## 6️⃣ Project Structure

```
AIDispatchService/
├── docs/
│   └── README.md                          # This file
├── src/main/
│   ├── java/com/swifttrack/AIDispatchService/
│   │   ├── AIDispatchServiceApplication.java    # Main entry
│   │   ├── conf/
│   │   │   ├── EnvConfiguration.java            # .env loader
│   │   │   ├── EnvPropertySourceFactory.java    # Property source
│   │   │   ├── LangChainConfig.java             # ChatClient bean  ← NEW
│   │   │   └── OpenApiConfig.java               # Swagger
│   │   ├── controllers/
│   │   │   └── DispatchController.java          # REST endpoint
│   │   ├── dto/
│   │   │   ├── DispatchRequest.java             # Input DTO
│   │   │   ├── DispatchResponse.java            # Output DTO
│   │   │   ├── DriverProfile.java               # Driver metrics
│   │   │   ├── DriverMemorySummary.java         # RAG memory
│   │   │   └── LlmDecision.java                 # Schema record  ← REFACTORED
│   │   ├── exception/
│   │   │   ├── DispatchTimeoutException.java    # Timeout
│   │   │   └── GlobalExceptionHandler.java      # Error handler
│   │   ├── langchain/
│   │   │   ├── DispatchChainExecutor.java        # LangChain engine  ← NEW
│   │   │   ├── DriverDataSerializer.java         # Data serializer   ← NEW
│   │   │   └── LangSmithPromptFetcher.java       # Prompt registry
│   │   ├── observability/
│   │   │   └── LangfuseClient.java              # Trace logging
│   │   ├── repositories/
│   │   │   ├── DriverProfileRepository.java     # SQL fetch
│   │   │   └── DriverMemoryRepository.java      # pgvector RAG
│   │   └── services/
│   │       └── DispatchService.java             # Orchestrator  ← REFACTORED
│   └── resources/
│       └── application.yaml                     # Configuration
├── .env                                          # API keys
└── pom.xml                                       # Dependencies
```

### Files Removed
- ❌ `PromptAssembler.java` — replaced by `DispatchChainExecutor`
- ❌ `OllamaInferenceClient.java` — replaced by `DispatchChainExecutor`

---

## 7️⃣ LlmDecision Schema

```java
public record LlmDecision(
    @JsonProperty(required = true, value = "driver_id") String driverId,
    @JsonProperty(required = true, value = "confidence") double confidence,
    @JsonProperty(required = true, value = "reason") String reason
) {}
```

Spring AI's `BeanOutputConverter` generates a JSON schema from this record:
```json
{
  "type": "object",
  "required": ["driver_id", "confidence", "reason"],
  "properties": {
    "driver_id": { "type": "string" },
    "confidence": { "type": "number" },
    "reason": { "type": "string" }
  }
}
```

This schema is automatically embedded in the prompt to guide the LLM's output format.

---

## 8️⃣ Configuration

### Environment Variables (`.env`)

| Variable | Description |
|---|---|
| `DB_HOST` | Supabase PostgreSQL host |
| `DB_PORT` | Database port |
| `DB_NAME` | Database name |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `LANGSMITH_API_KEY` | LangSmith API key |
| `LANGFUSE_SECRET_KEY` | Langfuse secret key |
| `LANGFUSE_PUBLIC_KEY` | Langfuse public key |

### Application Properties

| Property | Default | Description |
|---|---|---|
| `server.port` | `8010` | Service port |
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama |
| `spring.ai.ollama.chat.options.model` | `qwen2.5:3b-instruct` | Chat model |
| `spring.ai.ollama.chat.options.temperature` | `0.1` | Determinism |
| `dispatch.max-drivers` | `5` | Max candidates |
| `dispatch.max-memory-per-driver` | `3` | Max RAG memories |
| `dispatch.pipeline-timeout-ms` | `400` | Pipeline budget |

---

## 9️⃣ Validation & Fallback Logic

### Validation Chain

When ChatClient's `.entity(LlmDecision.class)` fails to parse (malformed JSON):

1. `DispatchChainExecutor.executeValidationChain()` is called
2. Uses `dispatch_validator_v1` prompt with `{raw_output}` variable
3. Re-runs via LangChain with structured parsing
4. If still invalid → triggers deterministic fallback

### Fallback Strategy

| Trigger | Confidence | Strategy |
|---|---|---|
| No driver profiles found | 0.3 | First available driver ID |
| Pipeline timeout exceeded | 0.5 | Lowest cancellation rate |
| LangChain parsing failed (both passes) | 0.5 | Lowest cancellation rate |
| LLM selected unknown driver | 0.5 | Lowest cancellation rate |
| Unhandled exception | 0.3 | First available driver ID |

---

## 🔟 Langfuse Observability

Every dispatch creates **one trace** with multiple **spans**:

```
Trace: dispatch_assign
├── Span: fetch_driver_profiles
├── Span: retrieve_driver_memory
├── Generation: llm_inference (model, latency)
├── Span: validation (only if first parse failed)
└── Span: fallback_if_triggered (only if fallback)
```

All Langfuse calls are `@Async` — observability never blocks dispatch.

---

## 1️⃣1️⃣ Technology Stack

| Component | Technology | Version |
|---|---|---|
| Runtime | Spring Boot | 3.5.8 |
| AI Framework | Spring AI | 1.0.0 |
| LLM Runtime | Ollama (local) | latest |
| Chat Model | qwen2.5:3b-instruct | — |
| Prompt Registry | LangSmith API | — |
| Vector Search | Supabase pgvector | — |
| Observability | Langfuse Cloud | — |
| Service Discovery | Netflix Eureka | — |
| API Docs | SpringDoc OpenAPI | 2.8.3 |

---

## 1️⃣2️⃣ Running the Service

```bash
cd backend/services/AIDispatchService
mvn compile
mvn spring-boot:run
```

### Prerequisites

1. **Ollama** running on `localhost:11434`
2. Models `qwen2.5:3b-instruct` and `nomic-embed-text` already pulled
3. **Supabase PostgreSQL** accessible with pgvector enabled
4. **Eureka Server** running for service discovery

### Testing

```bash
curl -X POST http://localhost:8010/dispatch/assign \
  -H "Content-Type: application/json" \
  -d '{"driverIds":["550e8400-e29b-41d4-a716-446655440001","550e8400-e29b-41d4-a716-446655440002"]}'
```

---

## 1️⃣3️⃣ What This Service Does NOT Do

| Concern | Handled By |
|---|---|
| Generate embeddings | Driver Service |
| Run KD-tree search | Driver Service |
| Store dispatch results | Caller's responsibility |
| Authenticate requests | API Gateway |
| Use LangGraph | Explicitly excluded |
| Allow LLM to query DB | Prohibited |
| Manually parse JSON | Replaced by LangChain `.entity()` |
| Manually replace strings | Replaced by LangChain `.param()` |
