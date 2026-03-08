# Architecture Overview

## 1. What This Service Does

The **BillingAndSettlementService** is the financial backbone of the SwiftTrack logistics dispatch platform. It is responsible for:

- **Pricing** — Recording the exact cost breakdown for every order (who pays what, how much margin)
- **Ledger Accounting** — Maintaining a double-entry–style append-only ledger of all financial transactions
- **Settlement Management** — Tracking and processing payouts to providers and drivers
- **Margin Configuration** — Storing platform and tenant-level margin rules

## 2. Design Philosophy

### Ledger-Based Accounting Model

Unlike traditional invoice-based billing systems, this service uses a **ledger-based model** inspired by financial accounting principles:

```
┌─────────────────────────────────────────────────────┐
│                  GOLDEN RULE                        │
│                                                     │
│  Account Balance = SUM(Credits) - SUM(Debits)       │
│                                                     │
│  Every financial event creates ledger entries.      │
│  Entries are APPEND-ONLY — never modified/deleted.  │
│  The balance stored on the account is a cache of    │
│  the calculated balance from ledger transactions.   │
└─────────────────────────────────────────────────────┘
```

### Why Ledger-Based?

| Feature | Invoice-Based | Ledger-Based ✅ |
|---------|--------------|----------------|
| Auditability | Moderate | Full — every cent is traceable |
| Balance correctness | Derived from invoices | Mathematically provable from entries |
| Data integrity | Can be mutated | Append-only, immutable entries |
| Reconciliation | Manual checks | Automated — recalculate from transactions |
| Financial compliance | Difficult | Built-in — full audit trail |

## 3. High-Level Architecture

```
                    ┌──────────────┐
                    │  API Gateway │
                    │   (port 80)  │
                    └──────┬───────┘
                           │
                    ┌──────┴───────┐
                    │   Billing &  │
                    │  Settlement  │
                    │  (port 8002) │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────┴──────┐ ┌──┴───┐ ┌──────┴──────┐
       │ AuthService  │ │Redis │ │  PostgreSQL  │
       │ (Feign RPC)  │ │Cache │ │ billing.*    │
       └─────────────┘ └──────┘ └─────────────┘
```

### Service Dependencies

| Dependency | Purpose | Communication |
|-----------|---------|--------------|
| **AuthService** | Resolve JWT token → user identity | Feign Client (sync RPC) |
| **PostgreSQL** | Persistent storage | Spring Data JPA |
| **Redis** | Cache layer | Spring Data Redis |
| **Kafka** | Event streaming (future) | Spring Kafka |
| **Eureka** | Service discovery & registration | Spring Cloud Netflix |

## 4. Supported Delivery Sources

SwiftTrack supports 3 different delivery source types, each with a different billing flow:

| # | Source | Description | Pricing Model |
|---|--------|-------------|---------------|
| 1 | **External Provider** | Third-party logistics (e.g., Delhivery, Shiprocket) | Provider quote + platform margin |
| 2 | **Tenant Driver** | Drivers employed by the tenant | Distance × rate + platform margin |
| 3 | **SwiftTrack Gig Driver** | Gig economy drivers on SwiftTrack's platform | Base pay + per-km pay + platform commission |

## 5. Service Layer Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    CONTROLLERS                          │
│  AccountController  BillingController  SettlementCtrl   │
│  MarginConfigController                                 │
├─────────────────────────────────────────────────────────┤
│                    SERVICES                             │
│  AccountService    BillingService     SettlementService │
│  LedgerService     MarginConfigSvc   PricingSnapshotSvc│
├─────────────────────────────────────────────────────────┤
│                   REPOSITORIES                          │
│  AccountRepo    LedgerTransactionRepo   SettlementRepo │
│  MarginConfigRepo  PricingSnapshotRepo  SettlementTxRepo│
├─────────────────────────────────────────────────────────┤
│                   DATA MODELS                           │
│  Account  LedgerTransaction  MarginConfig               │
│  PricingSnapshot  Settlement  SettlementTransaction     │
├─────────────────────────────────────────────────────────┤
│                   DATABASE                              │
│  PostgreSQL — Schema: billing                           │
└─────────────────────────────────────────────────────────┘
```

### Service Responsibilities

| Service | Role |
|---------|------|
| **AccountService** | Creates/manages ledger accounts, resolves JWT tokens, reconciles balances |
| **LedgerService** | Core financial engine — records DEBIT/CREDIT with SERIALIZABLE isolation |
| **BillingService** | Orchestrates complete billing flows (3 scenarios), creates pricing snapshots + all ledger entries atomically |
| **SettlementService** | Manages payout lifecycle (PENDING → PROCESSING → SETTLED/FAILED) with automatic reversal on failure |
| **MarginConfigService** | CRUD for margin rules per tenant or platform-wide |
| **PricingSnapshotService** | Creates immutable pricing records per order |

## 6. Package Structure

```
src/main/java/com/swifttrack/
├── BillingAndSettlementService/
│   ├── BillingAndSettlementServiceApplication.java   # Main entry point
│   ├── conf/
│   │   ├── EnvConfiguration.java                     # .env file loader
│   │   ├── EnvPropertySourceFactory.java             # Custom property parser
│   │   ├── OpenApiConfig.java                        # Swagger configuration
│   │   └── RedisConfig.java                          # Redis cache configuration
│   ├── controllers/
│   │   ├── AccountController.java                    # /api/accounts
│   │   ├── BillingController.java                    # /api/billing
│   │   ├── MarginConfigController.java               # /api/margin-config
│   │   └── SettlementController.java                 # /api/settlements
│   ├── models/
│   │   ├── Account.java
│   │   ├── LedgerTransaction.java
│   │   ├── MarginConfig.java
│   │   ├── PricingSnapshot.java
│   │   ├── Settlement.java
│   │   ├── SettlementTransaction.java
│   │   └── enums/
│   │       ├── AccountType.java        (TENANT, PROVIDER, DRIVER, PLATFORM)
│   │       ├── MarginType.java         (FLAT, PERCENTAGE)
│   │       ├── OrganizationType.java   (SWIFTTRACK, TENANT)
│   │       ├── PricingSource.java      (PROVIDER, TENANT_DRIVER, GIG_DRIVER)
│   │       ├── ReferenceType.java      (ORDER, SETTLEMENT, ADJUSTMENT)
│   │       ├── SettlementStatus.java   (PENDING, PROCESSING, SETTLED, FAILED)
│   │       └── TransactionType.java    (CREDIT, DEBIT)
│   ├── repositories/
│   │   ├── AccountRepository.java
│   │   ├── LedgerTransactionRepository.java
│   │   ├── MarginConfigRepository.java
│   │   ├── PricingSnapshotRepository.java
│   │   ├── SettlementRepository.java
│   │   └── SettlementTransactionRepository.java
│   └── services/
│       ├── AccountService.java
│       ├── BillingService.java
│       ├── LedgerService.java
│       ├── MarginConfigService.java
│       ├── PricingSnapshotService.java
│       └── SettlementService.java
└── FeignClient/
    └── AuthInterface.java                             # AuthService RPC client
```
