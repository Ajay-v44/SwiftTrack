# Account Management

The Account API manages wallet balances, transactions, and financial operations for users.

## Getting Your Account

```python
from uuid import UUID

# Get account for a user
account = client.accounts.get_my_account(user_id=UUID("user-uuid"))

print(f"Account ID: {account.id}")
print(f"Type: {account.account_type.value}")
print(f"Balance: ₹{account.balance}")
print(f"Currency: {account.currency}")
print(f"Active: {account.is_active}")
```

## Transaction History

```python
# Get recent transactions
transactions = client.accounts.get_transactions(
    account_id=account.id,
    limit=10,
)

for txn in transactions:
    direction = "📥" if txn.transaction_type.value == "CREDIT" else "📤"
    print(f"{direction} ₹{txn.amount} - {txn.description}")
    print(f"   Status: {txn.status.value}")
    print(f"   Date: {txn.created_at}")
    if txn.reference_id:
        print(f"   Ref: {txn.reference_id}")
    print()
```

### Pagination

```python
# Get more transactions with offset
page1 = client.accounts.get_transactions(account.id, limit=20, offset=0)
page2 = client.accounts.get_transactions(account.id, limit=20, offset=20)
```

## Creating Accounts

```python
from swifttrack.models.account import AccountType

# Create consumer account (default)
consumer_account = client.accounts.create_account(
    user_id=user_id,
    account_type=AccountType.CONSUMER,
)

# Create provider account
provider_account = client.accounts.create_account(
    user_id=user_id,
    account_type=AccountType.PROVIDER,
)

# Create driver account
driver_account = client.accounts.create_account(
    user_id=user_id,
    account_type=AccountType.DRIVER,
)
```

### Account Types

| Type | Description |
|------|-------------|
| `CONSUMER` | End user account for placing orders |
| `PROVIDER` | Delivery service provider account |
| `DRIVER` | Individual driver account |
| `TENANT` | Business/organization account |
| `PLATFORM` | Platform administrator account |

## Reconciling Balance

Verify account balance against ledger:

```python
result = client.accounts.reconcile_balance(account.id)
print(result)
# "Balance is correct" or "Balance was corrected from ledger transactions"
```

## Wallet Top-Up (Admin Only)

Add funds to a user's wallet (requires admin permissions):

```python
from decimal import Decimal

updated_account = client.accounts.top_up_wallet(
    user_id=user_id,
    amount=1000.00,
    reference="TXN-REF-12345",  # Payment reference
)

print(f"New balance: ₹{updated_account.balance}")
```

## Complete Example

```python
from swifttrack import SwiftTrackClient
from swifttrack.models.account import AccountType
from uuid import UUID

def manage_account(client: SwiftTrackClient, user_id: UUID):
    """Demonstrate account management."""
    
    # Get or create account
    try:
        account = client.accounts.get_my_account(user_id)
        print(f"💳 Account found: {account.id}")
        print(f"   Balance: ₹{account.balance}")
    except Exception:
        print("🏦 Creating new account...")
        account = client.accounts.create_account(
            user_id=user_id,
            account_type=AccountType.CONSUMER,
        )
        print(f"   Created: {account.id}")
    
    # View transactions
    print("\n📜 Recent transactions:")
    transactions = client.accounts.get_transactions(account.id, limit=5)
    
    if not transactions:
        print("   No transactions yet")
    else:
        for txn in transactions:
            emoji = "📥" if txn.transaction_type.value == "CREDIT" else "📤"
            print(f"   {emoji} ₹{txn.amount} - {txn.description}")
    
    # Reconcile balance
    print("\n🔍 Reconciling balance...")
    result = client.accounts.reconcile_balance(account.id)
    print(f"   {result}")
    
    return account

# Usage
with SwiftTrackClient() as client:
    client.login("admin@example.com", "password")
    account = manage_account(client, UUID("user-uuid"))
```

## Transaction Types

| Type | Description |
|------|-------------|
| `CREDIT` | Money added to account |
| `DEBIT` | Money deducted from account |

## Transaction Status

| Status | Description |
|--------|-------------|
| `PENDING` | Transaction pending |
| `COMPLETED` | Transaction completed |
| `FAILED` | Transaction failed |
| `REVERSED` | Transaction reversed |

## Error Handling

```python
from swifttrack import NotFoundError, PermissionError, ValidationError

try:
    account = client.accounts.get_my_account("invalid-uuid")
except NotFoundError:
    print("Account not found")

try:
    # Access another user's transactions without permission
    client.accounts.get_transactions(other_account_id)
except PermissionError:
    print("Access denied")

try:
    # Top-up without admin rights
    client.accounts.top_up_wallet(user_id, 1000)
except PermissionError:
    print("Admin access required")
```

## Best Practices

1. **Check balance before orders**: Ensure sufficient funds
2. **Use idempotency keys**: Prevent duplicate charges
3. **Reconcile regularly**: Verify balance accuracy
4. **Handle precision**: Use `Decimal` for financial calculations

```python
from decimal import Decimal

# Good: Use Decimal for financial calculations
price = Decimal("150.50")
# Bad: Use float (precision issues)
price = 150.50
```
