# SwiftTrack AI Assignment Engine

## Overview

SwiftTrack's AI Assignment Engine is the core intelligence system that determines the best logistics provider for every delivery order. It combines machine learning, LLM-based reasoning, and vectorized provider intelligence to create a next‑gen orchestration layer that far surpasses traditional routing or rules‑based systems.

This document explains:

* The AI tech stack used
* The multi‑layered AI decision architecture
* How SwiftTrack evaluates providers
* How the ML model predicts assignment success
* End‑to‑end order assignment workflow

---

## AI Tech Stack

### **1. Machine Learning Model (Predictive Core)**

SwiftTrack uses a tabular ML model (XGBoost / LightGBM / PyTorch Tabular) trained on historical order data.

**Used for:**

* ETA prediction
* Delivery success probability
* Cancellation probability
* Provider reliability forecasting

**Input Features (Examples):**

* Provider code
* Pickup & drop coordinates
* City, state, region cluster
* Order time (peak/off‑peak)
* Weather conditions
* Provider SLA performance
* Historical delay rate
* Rider availability

**Output:**

* `success_probability`
* `predicted_delivery_time`
* `cancel_probability`
* `assignment_score`

---

### **2. LangChain Reasoning Layer (LLM Agent)**

A lightweight LLM (OpenAI or HuggingFace) acts as a reasoning agent.

**Used for:**

* Interpreting ML scores
* Weighing tradeoffs (cost vs ETA vs reliability)
* Adjusting decisions based on real‑time health metrics
* Generating explanations: *"Porter is chosen due to higher reliability in Koramangala during peak hours."*

**Tools:**

* LangChain Expression Language (LCEL)
* Tools for provider health checks
* Structured output parsing

---

### **3. Provider Intelligence Vector Store (RAG Layer)**

SwiftTrack stores provider behavior data in a vector DB (ChromaDB / Pinecone / FAISS).

**What gets embedded:**

* Provider reliability logs
* SLA breach patterns
* Cancellation reasons
* Region‑based performance summaries
* Tenants’ historical provider interactions

**RAG is used to:**

* Retrieve similar past cases
* Detect region‑specific anomalies
* Influence assignment scoring
* Explain decisions

---

## AI Architecture Overview

```
               ┌─────────────────────────────┐
               │    SwiftTrack Order API     │
               └──────────────┬──────────────┘
                              │
                              ▼
               ┌─────────────────────────────┐
               │  Quote/Eligibility Filter   │
               └──────────────┬──────────────┘
                              │ providers
                              ▼
               ┌─────────────────────────────┐
               │  ML Predictive Model Layer  │
               └──────────────┬──────────────┘
                              │ predictions
                              ▼
               ┌─────────────────────────────┐
               │  LangChain Reasoning Agent  │
               └──────────────┬──────────────┘
                              │ context
                              ▼
               ┌─────────────────────────────┐
               │     RAG Intelligence Layer  │
               └──────────────┬──────────────┘
                              │ insights
                              ▼
               ┌─────────────────────────────┐
               │  Final AI Assignment Score  │
               └─────────────────────────────┘
```

---

## How SwiftTrack Assigns an Order (Step‑by‑Step)

### **Step 1: Tenant Sends an Order Request**

Order Service validates order, identifies available providers, fetches quotes, and forwards structured data to the AI assignment engine.

### **Step 2: ML Model Generates Predictions**

For each provider:

* Predicts delivery time
* Predicts success vs failure
* Estimates cancellation probability
* Generates a base assignment score

### **Step 3: LangChain Agent Adds Reasoning**

The LLM evaluates:

* ML output
* Provider health
* Tenant preferences
* Surge hour effects
* Historical reliability for the city

Outputs:

* Adjusted assignment scores
* Human‑like justification

### **Step 4: RAG Fetches Relevant Provider Intelligence**

The vector database returns:

* Provider issues in that region
* Recent downtime or delays
* Reliability summaries

These insights are merged into final scoring.

### **Step 5: Final Provider Ranking**

The engine produces:

* `selected_provider`
* Ranked alternatives with scores
* Explanation for decision-making

### Example Response:

```
{
  "selected_provider": "UBER_DIRECT",
  "score": 0.91,
  "alternatives": [
    { "provider": "PORTER", "score": 0.87 },
    { "provider": "TENANT_FLEET", "score": 0.72 }
  ],
  "reason": "UberDirect has the highest predicted reliability for Koramangala → Indiranagar route during peak hours based on past 60 days performance."
}
```

---

## Why This Makes SwiftTrack Unique

### **1. AI-Driven Dispatch — Not Rules-Based**

Most competitors compare prices. SwiftTrack uses:

* ML predictions
* LLM reasoning
* RAG intelligence

### **2. Geo‑Adaptive Assignment**

Provider reliability varies by city. SwiftTrack adapts automatically.

### **3. Explainable Decisions**

LLM-generated reasons help build trust:

> "Porter is deprioritized today due to a spike in cancellations in Whitefield region."

### **4. Continuous Learning**

Model retrains with every order outcome.

### **5. True Platform Intelligence**

SwiftTrack becomes smarter with:

* More orders
* More providers
* More regions

---

## Future Enhancements

* Reinforcement Learning for dynamic provider incentives
* Provider embeddings for similarity matching
* Weather-aware ETA prediction
* Driver density heatmaps
* Multi-leg routing (hub-and-spoke)

---

## Conclusion

SwiftTrack is not just a logistics router — it is an intelligent orchestration engine powered by:

* ML prediction models
* LLM reasoning (LangChain)
* RAG provider intelligence

This AI foundation is what makes SwiftTrack stand far above classical delivery management systems and positions it as a standout portfolio and production-ready architecture.
