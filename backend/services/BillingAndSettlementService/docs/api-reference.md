# API Reference

**Base URL:** `http://localhost:8002` (direct) or via API Gateway  
**Auth:** All write endpoints require `token` header (JWT token)  
**Content-Type:** `application/json`

---

## Health Check

### `GET /api/billing/health`

Returns service health status.

**Response:**
```
200 OK
"BillingAndSettlementService is running"
```

---

## 📊 Account Endpoints

### `POST /api/accounts` — Create Account

Creates a ledger account for a user entity.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT authentication token |
| `userId` | `UUID` | Query | ✅ | The entity to create an account for |
| `accountType` | `string` | Query | ✅ | `TENANT`, `PROVIDER`, `DRIVER`, or `PLATFORM` |

**Response:** `200 OK` — Returns the created `Account` object

**Idempotent:** Yes — if account already exists for the same `userId` + `accountType`, returns the existing one.

---

### `GET /api/accounts/{accountId}` — Get Account

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `accountId` | `UUID` | Path | ✅ | Account ID |

**Response:** `200 OK` — Account object or `404 Not Found`

---

### `GET /api/accounts/user/{userId}` — Get Accounts by User

Returns all accounts belonging to a user.

**Response:** `200 OK` — Array of Account objects

---

### `GET /api/accounts/user/{userId}/type/{accountType}` — Get Account by User and Type

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `userId` | `UUID` | Path | ✅ | User ID |
| `accountType` | `string` | Path | ✅ | `TENANT`, `PROVIDER`, `DRIVER`, `PLATFORM` |

**Response:** `200 OK` — Account object or `404 Not Found`

---

### `GET /api/accounts/type/{accountType}` — Get All Accounts by Type

**Response:** `200 OK` — Array of Account objects of the specified type

---

### `GET /api/accounts/{accountId}/transactions` — Get Account Transactions

Returns all ledger transactions for an account, ordered by most recent first.

**Response:** `200 OK` — Array of `LedgerTransaction` objects

---

### `POST /api/accounts/{accountId}/reconcile` — Reconcile Balance

Recalculates the account balance from all ledger transactions and corrects if mismatched.

**Response:** `200 OK`
```
"Balance is correct"
// or
"Balance was corrected from ledger transactions"
```

---

## 💰 Billing Endpoints

### `POST /api/billing/process/external-provider` — Process External Provider Billing

Creates a pricing snapshot and all ledger entries for an order fulfilled by an external logistics provider.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token of the person processing |
| `orderId` | `UUID` | Query | ✅ | Order ID |
| `tenantId` | `UUID` | Query | ✅ | Tenant being charged |
| `providerId` | `UUID` | Query | ✅ | Provider being paid |
| `providerCost` | `BigDecimal` | Query | ✅ | Cost quoted by provider |
| `platformMargin` | `BigDecimal` | Query | ✅ | SwiftTrack's margin |

**Ledger entries created:**

| # | Account | Type | Amount | Description |
|---|---------|------|--------|-------------|
| 1 | Tenant | DEBIT | providerCost + platformMargin | Order charge |
| 2 | Provider | CREDIT | providerCost | Provider payout |
| 3 | Platform | CREDIT | platformMargin | Platform margin |

**Response:** `200 OK` — `PricingSnapshot` object

---

### `POST /api/billing/process/tenant-driver` — Process Tenant Driver Billing

Creates billing entries for an order fulfilled by a tenant's own driver.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token |
| `orderId` | `UUID` | Query | ✅ | Order ID |
| `tenantId` | `UUID` | Query | ✅ | Tenant being charged |
| `driverId` | `UUID` | Query | ✅ | Driver being paid |
| `driverCost` | `BigDecimal` | Query | ✅ | Driver's cost |
| `platformMargin` | `BigDecimal` | Query | ✅ | SwiftTrack's margin |

**Ledger entries created:**

| # | Account | Type | Amount |
|---|---------|------|--------|
| 1 | Tenant | DEBIT | driverCost + platformMargin |
| 2 | Driver | CREDIT | driverCost |
| 3 | Platform | CREDIT | platformMargin |

**Response:** `200 OK` — `PricingSnapshot` object

---

### `POST /api/billing/process/gig-driver` — Process Gig Driver Billing

Creates billing entries for an order fulfilled by a SwiftTrack gig driver.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token |
| `orderId` | `UUID` | Query | ✅ | Order ID |
| `tenantId` | `UUID` | Query | ✅ | Tenant being charged |
| `driverId` | `UUID` | Query | ✅ | Gig driver being paid |
| `driverEarning` | `BigDecimal` | Query | ✅ | Driver's earning |
| `platformCommission` | `BigDecimal` | Query | ✅ | Platform commission |

**Ledger entries created:**

| # | Account | Type | Amount |
|---|---------|------|--------|
| 1 | Tenant | DEBIT | driverEarning + platformCommission |
| 2 | Driver | CREDIT | driverEarning |
| 3 | Platform | CREDIT | platformCommission |

**Response:** `200 OK` — `PricingSnapshot` object

---

### `GET /api/billing/pricing/{orderId}` — Get Pricing Snapshot

Returns the immutable pricing breakdown for a specific order.

**Response:** `200 OK` — `PricingSnapshot` object or `404 Not Found`

---

### `GET /api/billing/ledger/order/{orderId}` — Get Order Transactions

Returns all ledger transactions related to a specific order.

**Response:** `200 OK` — Array of `LedgerTransaction` objects

---

## 🏦 Settlement Endpoints

### `POST /api/settlements/initiate` — Initiate Settlement

Starts a payout settlement, deducting the amount from the account balance.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token of the initiator |
| `accountId` | `UUID` | Query | ✅ | Account to settle |
| `amount` | `BigDecimal` | Query | ✅ | Payout amount |

**Validations:**
- Account must exist and have sufficient balance
- Creates a DEBIT ledger entry on the account

**Response:** `200 OK` — `Settlement` object (status: `PENDING`)

---

### `PUT /api/settlements/{settlementId}/processing` — Mark Processing

Updates settlement status to `PROCESSING` when the payment gateway transfer begins.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `settlementId` | `UUID` | Path | ✅ | Settlement ID |
| `externalReference` | `string` | Query | ✅ | Payment gateway reference |

**Response:** `200 OK` — Updated `Settlement` object

---

### `PUT /api/settlements/{settlementId}/settled` — Mark Settled

Updates settlement status to `SETTLED` when the payout is confirmed.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `settlementId` | `UUID` | Path | ✅ | Settlement ID |
| `externalReference` | `string` | Query | ❌ | Optional payment gateway reference |

**Response:** `200 OK` — Updated `Settlement` object

---

### `PUT /api/settlements/{settlementId}/failed` — Mark Failed

Marks settlement as `FAILED` and **automatically reverses** the debit by crediting the amount back.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token |
| `settlementId` | `UUID` | Path | ✅ | Settlement ID |

**Auto-reversal:** A CREDIT entry with reference type `ADJUSTMENT` is created to restore the balance.

**Response:** `200 OK` — Updated `Settlement` object

---

### `GET /api/settlements/account/{accountId}` — Get Settlements by Account

**Response:** `200 OK` — Array of `Settlement` objects

---

### `GET /api/settlements/pending` — Get Pending Settlements

**Response:** `200 OK` — Array of `Settlement` objects with status `PENDING`

---

### `GET /api/settlements/{settlementId}/transactions` — Get Settlement Transactions

Returns all ledger transactions linked to a settlement.

**Response:** `200 OK` — Array of `SettlementTransaction` objects

---

## ⚙️ Margin Config Endpoints

### `POST /api/margin-config` — Create Margin Config

Creates a new margin configuration rule.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token of the person configuring |
| `userId` | `UUID` | Query | ✅ | Target entity being configured |
| `organizationType` | `string` | Query | ✅ | `SWIFTTRACK` or `TENANT` |
| `marginType` | `string` | Query | ✅ | `FLAT` or `PERCENTAGE` |
| `key` | `string` | Query | ✅ | Config key (e.g., `base_margin`) |
| `value` | `BigDecimal` | Query | ✅ | Config value |

**Response:** `200 OK` — `MarginConfig` object

---

### `GET /api/margin-config/user/{userId}` — Get Active Configs by User

**Response:** `200 OK` — Array of active `MarginConfig` objects

---

### `GET /api/margin-config/user/{userId}/type/{organizationType}` — Get Filtered Configs

**Response:** `200 OK` — Array of active `MarginConfig` objects filtered by org type

---

### `GET /api/margin-config/platform` — Get Platform Configs

Returns all active margin configs for `OrganizationType.SWIFTTRACK`.

**Response:** `200 OK` — Array of `MarginConfig` objects

---

### `DELETE /api/margin-config/{configId}` — Deactivate Config

Soft-deletes a margin config by setting `is_active = false`.

| Parameter | Type | In | Required | Description |
|-----------|------|-----|----------|-------------|
| `token` | `string` | Header | ✅ | JWT token |
| `configId` | `UUID` | Path | ✅ | Config ID to deactivate |

**Response:** `200 OK` — `"Config deactivated"`
