# Security & Auditability

This document covers how the BillingAndSettlementService ensures financial correctness, security, and full audit trails.

---

## 1. Authentication — Token-Based Identity Resolution

### How It Works

Every write endpoint requires a `token` header containing a JWT token. The service resolves the caller's identity via the **AuthService** using a Feign Client:

```
Controller                   Service                    AuthService
    │                           │                           │
    │  @RequestHeader("token")  │                           │
    │──────────────────────────>│  resolveUserId(token)     │
    │                           │──────────────────────────>│
    │                           │  POST /api/users/v1/      │
    │                           │       getUserDetails      │
    │                           │<──────────────────────────│
    │                           │  TokenResponse {          │
    │                           │    id: UUID,              │
    │                           │    tenantId: Optional,    │
    │                           │    userType: Optional,    │
    │                           │    name, mobile, roles    │
    │                           │  }                        │
    │<──────────────────────────│                           │
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Token is resolved in the **service layer**, not the controller | Services need the user ID for audit fields |
| `userId` for target entities is passed as a parameter | The person configuring (from token) ≠ the entity being configured |
| Internal service methods accept `UUID createdBy` directly | Avoids redundant token resolution in service-to-service calls |

### Example: Margin Config

When an admin configures a margin for tenant "FastMart":
- `token` → resolves to Admin's UUID → stored as `created_by`
- `userId` param → FastMart's tenant UUID → stored as `user_id`

---

## 2. Audit Trail — Who Did What, When

### Audit Columns

Every financial table includes these audit columns:

| Column | Description | Set When |
|--------|-------------|----------|
| `created_by` | UUID of the user who created the record | On create |
| `updated_by` | UUID of the user who last modified the record | On create & update |
| `created_at` | Timestamp of creation | Auto via `@PrePersist` |
| `updated_at` | Timestamp of last update | Auto via `@PreUpdate` |

### Trace Chain

For any financial question, you can trace the full chain:

```
"Why was FastMart charged ₹138 for order-001?"

1. pricing_snapshots WHERE order_id = 'order-001'
   → providerCost=120, platformMargin=18, tenantCharge=138, source=PROVIDER

2. ledger_transactions WHERE order_id = 'order-001'
   → 3 entries: TENANT DEBIT ₹138, PROVIDER CREDIT ₹120, PLATFORM CREDIT ₹18

3. Each entry has:
   → created_by = who processed it
   → created_at = when it happened
   → idempotency_key = unique identifier
   → description = human-readable explanation
```

---

## 3. Financial Integrity

### 3.1 Append-Only Ledger

The `ledger_transactions` table is designed to be **append-only**:

| Operation | Allowed? |
|-----------|----------|
| INSERT | ✅ Yes |
| UPDATE | ⚠️ Only `updated_at` via `@PreUpdate` — no business data changes |
| DELETE | ❌ Never |

**Corrections** are always made by creating new entries with `reference_type = ADJUSTMENT`:

```
Original:    DEBIT ₹100 (reference: ORDER)
Correction:  CREDIT ₹100 (reference: ADJUSTMENT, description: "Settlement failed - reversal credit")
```

### 3.2 Idempotency

Every ledger entry has a unique `idempotency_key`:

| Scenario | Key Pattern |
|----------|------------|
| Order billing (tenant debit) | `ORDER-{orderId}-TENANT-DEBIT` |
| Order billing (provider credit) | `ORDER-{orderId}-PROVIDER-CREDIT` |
| Order billing (platform credit) | `ORDER-{orderId}-PLATFORM-CREDIT` |
| Settlement debit | `SETTLEMENT-{settlementId}-DEBIT` |
| Settlement reversal | `SETTLEMENT-{settlementId}-REVERSAL` |

**Behavior on duplicate:**
```java
if (idempotencyKey != null) {
    Optional<LedgerTransaction> existing = repository.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
        log.warn("Duplicate transaction detected. Returning existing.");
        return existing.get();  // Safe re-entry
    }
}
```

### 3.3 Balance Correctness

Account `balance` is a **cached value** that can be independently verified:

```sql
-- Verify balance for any account
SELECT 
  COALESCE(SUM(CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE 0 END), 0) -
  COALESCE(SUM(CASE WHEN transaction_type = 'DEBIT' THEN amount ELSE 0 END), 0) 
  AS calculated_balance
FROM billing.ledger_transactions 
WHERE account_id = :accountId;
```

The `POST /api/accounts/{accountId}/reconcile` endpoint automates this check.

---

## 4. Transaction Safety

### 4.1 Isolation Level

All financial operations use `SERIALIZABLE` isolation:

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public PricingSnapshot processExternalProviderOrder(...) {
    // Pricing snapshot + 3 ledger entries + 3 balance updates
    // ALL happen in one SERIALIZABLE transaction
}
```

**Why SERIALIZABLE?**
- Prevents phantom reads — no concurrent transaction can insert data that would change the result
- Prevents lost updates — no two transactions can modify the same account balance simultaneously
- Maximum safety for financial operations

### 4.2 Pessimistic Locking

Account balances are protected by `SELECT ... FOR UPDATE`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") UUID id);
```

**Usage pattern:**
1. Lock account → `findByIdForUpdate(accountId)`
2. Read current balance
3. Create ledger entry
4. Update balance
5. Commit transaction → lock released

### 4.3 Atomic Billing

Each billing operation creates **all entries or none**:

```
Transaction Begin (SERIALIZABLE)
  ├── Create PricingSnapshot
  ├── Lock Tenant Account
  ├── Create DEBIT LedgerTransaction
  ├── Update Tenant balance
  ├── Lock Provider/Driver Account
  ├── Create CREDIT LedgerTransaction
  ├── Update Provider/Driver balance
  ├── Lock Platform Account
  ├── Create CREDIT LedgerTransaction
  ├── Update Platform balance
Transaction Commit
```

If ANY step fails → entire transaction is rolled back → no partial state.

---

## 5. Data Protection

### 5.1 Sensitive Data

| Data | Protection |
|------|-----------|
| JWT tokens | Never stored — only used to resolve user identity |
| Account balances | Protected by pessimistic locks |
| Financial records | Append-only, never deleted |
| External payment refs | Stored in `settlements.external_reference` for reconciliation |

### 5.2 Constraints & Validation

| Constraint | Purpose |
|-----------|---------|
| `UNIQUE(user_id, account_type)` | One account per entity per type |
| `UNIQUE(idempotency_key)` | Prevent duplicate transactions |
| `UNIQUE(order_id)` on pricing_snapshots | One pricing record per order |
| `FK(account_id)` on ledger_transactions | Referential integrity |
| `FK(account_id)` on settlements | Referential integrity |
| `FK(settlement_id, ledger_transaction_id)` on settlement_transactions | Full traceability |
| `NUMERIC(14,2)` for money | Prevents floating-point precision issues |

---

## 6. Monitoring & Alerting Recommendations

| Check | Frequency | Action |
|-------|-----------|--------|
| Balance reconciliation | Daily | Run `POST /reconcile` for all active accounts |
| Pending settlements age | Hourly | Alert if any settlement is `PENDING` for > 24h |
| Failed settlements | Real-time | Alert on any `FAILED` settlement |
| Idempotency key conflicts | Real-time | Log warns indicate duplicate processing |
| Ledger entry count vs order count | Daily | Every completed order should have exactly 3 entries |
