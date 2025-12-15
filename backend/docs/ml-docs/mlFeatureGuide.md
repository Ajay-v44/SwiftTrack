# SwiftTrack ML Feature Builder Guide

## Purpose of This Document

This document explains **how the Provider Service (and Order Service)** should construct **ML-ready feature inputs** for the **Assignment ML Model**.

It is written from a **real-world, industry-grade perspective**, assuming:

* SwiftTrack core services are in **Java (Spring Boot)**
* ML inference runs in a **Python FastAPI microservice**
* ML models are **stateless at inference time**

This guide focuses only on **feature construction**, not ML internals.

---

## Key Principle (Read This First)

> **ML models do NOT understand business objects.**
> They only understand **numbers and categorical values**.

So the Provider Service must convert:

* orders
* providers
* locations
* runtime context

into a **flat, numeric feature vector**.

---

## High-Level Flow

```
Order Request
   ↓
Location Engine (distance, city, type)
   ↓
Provider Service (eligible providers)
   ↓
Feature Builder (per provider)
   ↓
ML Assignment Service (predict)
   ↓
Provider Ranking & Assignment
```

---

## Core Design Rule

> **One order → many providers → one feature row per provider**

The ML model does NOT choose between providers automatically.

Instead:

* SwiftTrack generates **one feature row per provider**
* ML scores each row independently
* SwiftTrack ranks providers using ML output

---

## Feature Categories

Features should be grouped into **four logical buckets**:

1. Order-derived features
2. Location & route features
3. Provider-specific runtime features
4. Time & system context features

---

## 1. Order-Derived Features

These come directly from the incoming order.

| Feature       | Type        | Description                         | How to Compute               |
| ------------- | ----------- | ----------------------------------- | ---------------------------- |
| delivery_type | categorical | HYPERLOCAL / INTRACITY / INTERSTATE | From Location Engine         |
| payment_type  | categorical | PREPAID / COD                       | Order payload                |
| cod_amount    | numeric     | Cash on delivery amount             | Order payload (0 if prepaid) |

> ⚠️ Do NOT pass order_id or tenant_id to ML

---

## 2. Location & Route Features

These are computed using the **Map / Location Engine**.

| Feature              | Type        | Description                         | Source             |
| -------------------- | ----------- | ----------------------------------- | ------------------ |
| distance_km          | numeric     | Road distance between pickup & drop | OSRM / GraphHopper |
| estimated_route_time | numeric     | Base travel time without provider   | Map service        |
| city_type            | categorical | METRO / NON_METRO                   | Derived from city  |

Optional (advanced):

* road_density_score
* area_cluster_id

---

## 3. Provider-Specific Runtime Features (MOST IMPORTANT)

These features differ **per provider** and are the main decision drivers.

| Feature                 | Type        | Description                 | How to Compute                 |
| ----------------------- | ----------- | --------------------------- | ------------------------------ |
| provider_code           | categorical | UBER / PORTER / SHADOWFAX   | Provider registry              |
| provider_load           | numeric     | Current provider load       | Active orders (rolling window) |
| provider_priority       | numeric     | Tenant preference weight    | Tenant config                  |
| supports_cod            | boolean     | Provider supports COD       | Provider config                |
| historical_success_rate | numeric     | Avg success rate (optional) | Aggregated stats               |

> Initially, historical fields can be **synthetic or defaulted**.

---

## 4. Time & System Context Features

These capture **real-world dynamics**.

| Feature       | Type    | Description          | How to Compute   |
| ------------- | ------- | -------------------- | ---------------- |
| is_peak_hour  | boolean | Peak delivery window | System clock     |
| hour_of_day   | numeric | 0–23                 | System clock     |
| day_of_week   | numeric | 0–6                  | System clock     |
| traffic_level | numeric | 1–5                  | Rule-based logic |

Example traffic heuristic:

```
if is_peak_hour and city_type == METRO → traffic_level = 4
else if is_peak_hour → traffic_level = 3
else → traffic_level = 2
```

---

## Canonical Feature Schema (ML Input Contract)

Each provider gets **one feature object**.

```json
{
  "provider": "UBER",
  "distance_km": 8.7,
  "estimated_route_time": 22,
  "traffic_level": 4,
  "is_peak_hour": true,
  "provider_load": 2,
  "provider_priority": 1.0,
  "supports_cod": true,
  "delivery_type": "HYPERLOCAL"
}
```

This schema must be:

* Stable
* Versioned
* Independent of DB schemas

---

## Java-Side Feature Builder (Conceptual)

The Provider Service should implement a **FeatureBuilder** component:

Responsibilities:

1. Iterate over eligible providers
2. Pull runtime metrics (load, config)
3. Combine with order + location context
4. Emit feature rows

Pseudo-flow:

```
for provider in eligibleProviders:
    features = buildFeatures(order, location, provider)
    sendToML(features)
```

---

## What NOT to Include in ML Features

❌ order_id
❌ tenant_id
❌ customer_id
❌ raw addresses
❌ provider internal IDs
❌ database keys

ML does not need them and they reduce generalization.

---

## How ML Output Is Used

ML returns (example):

```json
{
  "provider": "UBER",
  "success_probability": 0.91
}
```

SwiftTrack then:

1. Ranks providers by score
2. Applies governance rules
3. Assigns provider
4. Stores prediction + outcome

---

## Governance & Safety Hooks

Provider Service should enforce:

* Minimum confidence threshold
* Fallback to rule-based logic
* Manual override capability

Example:

```
if max_score < 0.6 → fallback_strategy()
```

---

## Why This Design Is Industry-Grade

* Clear separation of concerns
* ML sees only features, not business logic
* Easy to retrain models without code changes
* Supports explainability & auditing
* Scales across providers & tenants

---

## Interview-Ready Summary

> “SwiftTrack constructs ML features per provider by combining order context, geospatial metrics, provider runtime state, and temporal signals into a normalized feature vector. These features are passed to a stateless ML inference service, enabling scalable, explainable, and governed provider assignment.”

---

## Next Recommended Steps

* Version the feature schema (v1, v2)
* Add feature importance tracking
* Align features with `order_ai_features` table
* Add LangGraph-based reasoning on top of ML scores

---

**End of Document**
