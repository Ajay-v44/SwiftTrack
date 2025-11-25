# **SwiftTrack – Database Architecture & Storage Layer Overview**

This document outlines all databases and storage systems required for SwiftTrack, along with their specific purposes in the platform. SwiftTrack is a real-time, event-driven, multi-tenant SaaS delivery orchestration system, and therefore relies on multiple specialized data stores to achieve scalability, reliability, and low latency.

---

# ## **1. PostgreSQL – Primary Relational Database (OLTP)**

PostgreSQL acts as the **core transactional database** for almost all microservices. It stores structured, relational, and ACID-compliant data.

### **Purpose:**

* Store tenant businesses & their settings
* Store users, roles, permissions (Auth Service)
* Store drivers & verification metadata
* Store orders & delivery lifecycle events
* Store 3rd-party provider configurations
* Store wallets, billing, invoices
* Store webhook configurations
* Provide multi-tenant isolation

### **Used By:**

* Auth Service
* Tenant Service
* Driver Service
* Provider Management Service
* Order Service
* Billing Service
* Dashboard Service

PostgreSQL is chosen for:

* Strong ACID guarantees
* JSONB support for flexible metadata
* Partitioning capabilities for high-scale order data

---

# ## **2. Redis – High-Speed Realtime Data Store**

Redis is used as a **low-latency, in-memory database** for real-time operations.

### **Purpose:**

* Store driver live locations (GPS)
* Store order live locations
* Maintain active WebSocket sessions
* Cache frequently accessed data
* Rate limiting & throttling
* Distributed locks for assignment engine
* Geo indexing for nearby driver search (Redis GEOSEARCH)

### **Used By:**

* Tracking Service (WebSockets)
* Assignment Service
* Location Service
* Notification Service
* API Gateway (optional rate limiting)

Redis ensures **sub-50ms latency** for live delivery updates.

---

# ## **3. Kafka – Event Streaming Backbone**

Kafka is the backbone of SwiftTrack’s **event-driven architecture**. It decouples microservices and enables scalable workflows.

### **Purpose:**

* Publish order events (order.created, order.updated)
* Stream driver location updates
* Stream provider callbacks
* Push notifications for analytics
* Feed AI/ML models with historical data
* Event logs for replay & auditing

### **Used By:**

* Order Service
* Delivery Orchestration
* Assignment Service
* Tracking Service
* Analytics Service
* Notification Service

Kafka ensures:

* High throughput event handling
* Horizontal scalability
* Replayability for debugging & analytics

---

# ## **4. MinIO / AWS S3 – Object Storage**

Used for storing large or unstructured files.

### **Purpose:**

* Driver KYC documents (license, ID proof)
* Provider documents
* Proof-of-delivery images
* Invoice PDFs
* Logs & files for analytics

### **Used By:**

* Driver Service
* Tenant Service
* Billing Service

Object storage provides durability and low-cost data retention.

---

# ## **5. ClickHouse (Optional, Phase 2) – Analytics Database (OLAP)**

ClickHouse is an OLAP engine optimized for **large-scale analytical queries**.

### **Purpose:**

* Store historical order data
* Run analytical dashboards (SLA, delay %, zone-wise metrics)
* Feed AI dispatch algorithms
* Provide tenant analytics & insights

### **Used By:**

* Analytics Service
* AI Dispatch Service
* Dashboard Service

This is optional for MVP but essential for scale.

---

# ## **6. Optional Storage Enhancements (Future-Ready)**

### **A. ElasticSearch (Search & GeoQueries)**

* Quick driver search
* Dashboard search for orders/users/drivers
* Full-text search on metadata

### **B. TimescaleDB (Time-series extension for Postgres)**

* If not using ClickHouse
* Good for tracking historical locations and statuses

### **C. Neo4j (Route/Graph Modeling)**

* Optional for advanced routing/graph-based optimization

---

# ## **7. Summary Table**

| Storage               | Purpose                              | Used By                                |
| --------------------- | ------------------------------------ | -------------------------------------- |
| PostgreSQL            | Structured transactional data        | Auth, Tenant, Drivers, Orders, Billing |
| Redis                 | Real-time cache, GEO, live locations | Tracking, Assignment, API Gateway      |
| Kafka                 | Event streaming backbone             | Orchestration, Tracking, Analytics     |
| S3/MinIO              | File storage for KYC, docs, receipts | Driver, Tenant, Billing                |
| ClickHouse (optional) | Analytics, AI data, dashboards       | Analytics, AI Dispatch                 |

---

# ## **8. Why These Databases Are Required**

SwiftTrack is designed to behave like modern high-scale logistics platforms (Uber, DoorDash, Swiggy). Each database type is chosen to optimize a specific workload:

* **Postgres** → Trust, correctness, relational constraints
* **Redis** → Real-time speed (tracking, GEO)
* **Kafka** → Scalability, async events, decoupling services
* **S3** → Cheap file storage
* **ClickHouse** → Large-scale analytics

This combination ensures the platform can scale smoothly to millions of deliveries.

---
This document will be part of the central project architecture section in SwiftTrack.
