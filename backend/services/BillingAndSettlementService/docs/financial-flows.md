# Financial Flows

This document walks through the complete financial lifecycle for each delivery scenario, showing exactly what happens in the database at each step.

---

## Flow 1: External Provider Order

**Scenario:** Tenant "FastMart" places an order fulfilled by Delhivery (external provider).

### Step 1: Order Completed → Billing Triggered

```
POST /api/billing/process/external-provider
  token: <jwt_token>
  orderId: order-001
  tenantId: fastmart-uuid
  providerId: delhivery-uuid
  providerCost: 120.00
  platformMargin: 18.00
```

### Step 2: Pricing Snapshot Created

| Field | Value |
|-------|-------|
| `order_id` | `order-001` |
| `provider_cost` | `₹120.00` |
| `driver_cost` | `null` |
| `platform_margin` | `₹18.00` |
| `tenant_charge` | `₹138.00` |
| `pricing_source` | `PROVIDER` |

### Step 3: Ledger Entries Created (Atomically)

| # | Account | Type | Amount | Idempotency Key | Description |
|---|---------|------|--------|----------------|-------------|
| 1 | FastMart (TENANT) | **DEBIT** | ₹138.00 | `ORDER-order-001-TENANT-DEBIT` | Order charge for external provider delivery |
| 2 | Delhivery (PROVIDER) | **CREDIT** | ₹120.00 | `ORDER-order-001-PROVIDER-CREDIT` | Provider payout for delivery |
| 3 | SwiftTrack (PLATFORM) | **CREDIT** | ₹18.00 | `ORDER-order-001-PLATFORM-CREDIT` | Platform margin on external provider order |

### Step 4: Account Balances After

| Account | Previous Balance | Change | New Balance |
|---------|-----------------|--------|-------------|
| FastMart | ₹0.00 | -₹138.00 | **-₹138.00** (owes money) |
| Delhivery | ₹0.00 | +₹120.00 | **₹120.00** (owed money) |
| SwiftTrack | ₹0.00 | +₹18.00 | **₹18.00** (earned) |

### Verification: Money In = Money Out

```
Tenant Debit:    ₹138.00
Provider Credit: ₹120.00
Platform Credit: ₹ 18.00
─────────────────────────
TOTAL:           ₹138.00 = ₹138.00 ✅ (balanced)
```

---

## Flow 2: Tenant Driver Order

**Scenario:** Tenant "QuickBites" order fulfilled by their own driver Ramesh.

### Billing Request

```
POST /api/billing/process/tenant-driver
  token: <jwt_token>
  orderId: order-002
  tenantId: quickbites-uuid
  driverId: ramesh-uuid
  driverCost: 85.00
  platformMargin: 12.75
```

### Pricing Snapshot

| Field | Value |
|-------|-------|
| `order_id` | `order-002` |
| `provider_cost` | `null` |
| `driver_cost` | `₹85.00` |
| `platform_margin` | `₹12.75` |
| `tenant_charge` | `₹97.75` |
| `pricing_source` | `TENANT_DRIVER` |

### Ledger Entries

| # | Account | Type | Amount | Description |
|---|---------|------|--------|-------------|
| 1 | QuickBites (TENANT) | DEBIT | ₹97.75 | Order charge for tenant driver delivery |
| 2 | Ramesh (DRIVER) | CREDIT | ₹85.00 | Driver payout for tenant delivery |
| 3 | SwiftTrack (PLATFORM) | CREDIT | ₹12.75 | Platform margin on tenant driver order |

---

## Flow 3: SwiftTrack Gig Driver Order

**Scenario:** Tenant "MegaStore" order fulfilled by gig driver Priya from SwiftTrack's network.

### Billing Request

```
POST /api/billing/process/gig-driver
  token: <jwt_token>
  orderId: order-003
  tenantId: megastore-uuid
  driverId: priya-uuid
  driverEarning: 95.00
  platformCommission: 23.75
```

### Pricing Snapshot

| Field | Value |
|-------|-------|
| `order_id` | `order-003` |
| `provider_cost` | `null` |
| `driver_cost` | `₹95.00` |
| `platform_margin` | `₹23.75` |
| `tenant_charge` | `₹118.75` |
| `pricing_source` | `GIG_DRIVER` |

### Ledger Entries

| # | Account | Type | Amount | Description |
|---|---------|------|--------|-------------|
| 1 | MegaStore (TENANT) | DEBIT | ₹118.75 | Order charge for gig driver delivery |
| 2 | Priya (DRIVER) | CREDIT | ₹95.00 | Gig driver earning for delivery |
| 3 | SwiftTrack (PLATFORM) | CREDIT | ₹23.75 | Platform commission on gig driver order |

---

## Flow 4: Settlement (Payout) Lifecycle

**Scenario:** Delhivery has ₹120.00 balance and requests a payout of ₹100.00.

### Step 1: Initiate Settlement

```
POST /api/settlements/initiate
  token: <jwt_token>
  accountId: delhivery-account-uuid
  amount: 100.00
```

**What happens:**
1. Account is locked with `SELECT ... FOR UPDATE`
2. Balance check: ₹120.00 ≥ ₹100.00 ✅
3. Settlement record created (status: `PENDING`)
4. Ledger DEBIT of ₹100.00 on Delhivery's account
5. Delhivery's balance: ₹120.00 → **₹20.00**

### Step 2: Mark Processing

```
PUT /api/settlements/{settlementId}/processing
  externalReference: "RAZORPAY-TXN-ABC123"
```

Settlement status: `PENDING` → `PROCESSING`

### Step 3a: Mark Settled (Success Path)

```
PUT /api/settlements/{settlementId}/settled
  externalReference: "RAZORPAY-TXN-ABC123"
```

Settlement status: `PROCESSING` → `SETTLED` ✅

**Final state:** Delhivery balance stays at ₹20.00. The ₹100.00 was paid out.

### Step 3b: Mark Failed (Failure Path)

```
PUT /api/settlements/{settlementId}/failed
  token: <jwt_token>
```

**What happens:**
1. Settlement status: `PROCESSING` → `FAILED`
2. **Automatic reversal:** CREDIT of ₹100.00 back to Delhivery's account
3. Delhivery's balance: ₹20.00 → **₹120.00** (restored)

**Ledger entries for failed settlement:**

| # | Type | Amount | Reference | Description |
|---|------|--------|-----------|-------------|
| 1 | DEBIT | ₹100.00 | SETTLEMENT | Settlement payout initiated |
| 2 | CREDIT | ₹100.00 | ADJUSTMENT | Settlement failed - reversal credit |

---

## Flow 5: Balance Reconciliation

**Scenario:** As a safety measure, reconcile an account's cached balance against the ledger.

```
POST /api/accounts/{accountId}/reconcile
```

**What happens:**
1. Lock the account with `FOR UPDATE`
2. Execute: `SUM(CREDITS) - SUM(DEBITS)` from `ledger_transactions`
3. Compare with stored `balance`
4. If mismatch → correct the stored balance and log a warning

```
WARN: Balance mismatch for account abc-123.
      Stored=₹118.50, Calculated=₹120.00. Correcting.
```

---

## Transaction Safety Guarantees

### Atomicity
Every billing operation runs inside a single `@Transactional(isolation = Isolation.SERIALIZABLE)` transaction:
- Pricing snapshot + all 3 ledger entries + balance updates = **all or nothing**

### Idempotency
Every ledger entry has a unique `idempotency_key` derived from the order ID:
```
ORDER-{orderId}-TENANT-DEBIT
ORDER-{orderId}-PROVIDER-CREDIT
ORDER-{orderId}-PLATFORM-CREDIT
```
If the same request is processed twice, the duplicate is detected and the existing entry is returned.

### Pessimistic Locking
Account balances are protected by `SELECT ... FOR UPDATE` before any mutation:
- Prevents race conditions when concurrent transactions hit the same account
- Combined with SERIALIZABLE isolation for maximum safety

### Append-Only Ledger
- Ledger entries are **never updated or deleted**
- Corrections are made via new `ADJUSTMENT` entries
- The full financial history is always preserved
