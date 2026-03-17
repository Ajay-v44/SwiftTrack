# SwiftTrack Python SDK

[![CI](https://github.com/swifttrack/swifttrack-python/actions/workflows/ci.yml/badge.svg)](https://github.com/swifttrack/swifttrack-python/actions/workflows/ci.yml)
[![PyPI](https://img.shields.io/pypi/v/swifttrack.svg)](https://pypi.org/project/swifttrack/)
[![Python](https://img.shields.io/pypi/pyversions/swifttrack.svg)](https://pypi.org/project/swifttrack/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

```bash
pip install swifttrack
```

A production-grade Python SDK for the **SwiftTrack** logistics platform. This SDK provides a clean, developer-friendly interface to interact with the SwiftTrack API for managing deliveries, addresses, orders, and accounts.

## Features

- **Full API Coverage**: Auth, Address, Order, and Account management
- **Type-Safe**: Full type hints with Pydantic models
- **Robust Error Handling**: Structured exceptions for all error cases
- **Retry Logic**: Automatic retries with exponential backoff
- **Authentication**: Simple token-based authentication
- **Modern Python**: Supports Python 3.9+
- **Production Ready**: Industry-standard patterns and best practices

## Installation

```bash
pip install swifttrack
```

## Quick Start

```python
from swifttrack import SwiftTrackClient

# Initialize client
client = SwiftTrackClient()

# Authenticate
client.login("your@email.com", "password")

# Use the API
addresses = client.addresses.list_addresses()
print(f"You have {len(addresses)} saved addresses")
```

## Authentication

### Login with Email/Password

```python
from swifttrack import SwiftTrackClient

client = SwiftTrackClient()
response = client.login("user@example.com", "password")
print(f"Logged in! Token: {response.access_token}")
```

### Using Existing Token

```python
from swifttrack import SwiftTrackClient

# If you already have a token
client = SwiftTrackClient(token="your-jwt-token")
# or
client = SwiftTrackClient()
client.set_token("your-jwt-token")
```

### Environment Variables

You can also configure the client via environment variables:

```bash
export SWIFTTRACK_BASE_URL="https://backend-swifttrack.ajayv.online"
export SWIFTTRACK_TOKEN="your-token"
```

```python
from swifttrack import SwiftTrackClient

# Uses environment variables automatically
client = SwiftTrackClient()
```

## Address Management

```python
from swifttrack.models.address import AddressRequest

# List all addresses
addresses = client.addresses.list_addresses()
for addr in addresses:
    print(f"{addr.label}: {addr.line1}, {addr.city}")

# Get default address
default = client.addresses.get_default_address()

# Create new address
new_address = AddressRequest(
    label="Office",
    line1="456 Business Park",
    city="Bangalore",
    state="Karnataka",
    pincode="560001",
    latitude=12.9716,
    longitude=77.5946,
)
created = client.addresses.create_address(new_address)
print(f"Created address: {created.id}")

# Set as default
client.addresses.set_default(created.id)

# Update address
updated = client.addresses.update_address(created.id, AddressRequest(
    label="Updated Office",
    line1="789 New Business Park",
    city="Bangalore",
    state="Karnataka",
    pincode="560001",
))

# Delete address
client.addresses.delete_address(created.id)
```

## Order Management

### Get Delivery Quote

```python
from uuid import UUID

# Get quote for delivery
pickup_address_id = UUID("your-address-id")
quote = client.orders.get_quote(
    pickup_address_id=pickup_address_id,
    dropoff_lat=19.0760,
    dropoff_lng=72.8777,
)

# View available options
for option in quote.quotes:
    print(f"{option.provider_name}: ₹{option.price} ({option.estimated_delivery_time})")
```

### Create Order

```python
from swifttrack.models.order import CreateOrderRequest, LocationPoint
from uuid import UUID

# Create order request
order_request = CreateOrderRequest(
    idempotency_key="unique-key-123",  # Prevent duplicate orders
    pickup_address_id=UUID("pickup-address-id"),
    dropoff=LocationPoint(
        latitude=19.0760,
        longitude=72.8777,
        address="456 Dropoff Street, Mumbai",
    ),
    order_reference="MY-ORDER-001",
)

# Create the order
order = client.orders.create_order(order_request, quote.quote_session_id)
print(f"Order created: {order.order_number}")
print(f"Tracking URL: {order.tracking_url}")
```

### Cancel Order

```python
# Cancel an order
result = client.orders.cancel_order(
    order_id=UUID("order-id"),
    reason="Customer requested cancellation"
)
print(result["message"])
```

### Get Order Details

```python
# Get order status
status = client.orders.get_order_status(order_id)
print(f"Order status: {status}")

# Get full order details
order = client.orders.get_order(order_id)
print(f"Order: {order.order_number}")
print(f"Status: {order.status}")
print(f"Price: ₹{order.price}")
```

### Guest Quotes (No Auth Required)

```python
# Get a quote without authentication
guest_quote = client.orders.get_guest_quote(
    pickup_lat=19.0760,
    pickup_lng=72.8777,
    pickup_address="123 Pickup St, Mumbai",
    dropoff_lat=19.2183,
    dropoff_lng=72.9781,
    dropoff_address="456 Dropoff St, Mumbai",
    package_weight_kg=2.5,
)

for option in guest_quote.options:
    print(f"{option.provider_name}: ₹{option.price}")
```

## Account Management

```python
from swifttrack.models.account import AccountType

# Get your account
account = client.accounts.get_my_account(user_id)
print(f"Balance: ₹{account.balance}")

# Get transaction history
transactions = client.accounts.get_transactions(account.id, limit=10)
for txn in transactions:
    print(f"{txn.transaction_type}: ₹{txn.amount} - {txn.description}")

# Create new account (for providers/drivers)
new_account = client.accounts.create_account(
    user_id=user_id,
    account_type=AccountType.PROVIDER,
)

# Reconcile account balance
result = client.accounts.reconcile_balance(account.id)
print(result)

# Admin: Top up wallet (requires admin permissions)
updated = client.accounts.top_up_wallet(
    user_id=user_id,
    amount=1000.00,
    reference="TXN-REF-123",
)
```

## Error Handling

The SDK provides structured exceptions for different error scenarios:

```python
from swifttrack import (
    SwiftTrackClient,
    AuthenticationError,
    NotFoundError,
    ValidationError,
    RateLimitError,
    ServerError,
)

client = SwiftTrackClient()

try:
    client.login("user@example.com", "wrong-password")
except AuthenticationError as e:
    print(f"Login failed: {e.message}")

try:
    client.addresses.get_address("non-existent-id")
except NotFoundError as e:
    print(f"Address not found: {e.message}")

try:
    # Invalid coordinates
    client.orders.get_quote(address_id, 200, 72)  # Invalid latitude
except ValidationError as e:
    print(f"Validation error: {e.errors}")

try:
    # API temporarily unavailable
    client.orders.get_order(order_id)
except ServerError as e:
    print(f"Server error: {e.message} (status: {e.status_code})")

try:
    # Rate limited
    client.addresses.list_addresses()
except RateLimitError as e:
    print(f"Rate limited. Retry after: {e.retry_after} seconds")
```

## Configuration

### Custom Configuration

```python
from swifttrack import SwiftTrackClient, SwiftTrackConfig

# Create custom config
config = SwiftTrackConfig(
    base_url="https://custom-api.example.com",
    token="your-token",
    timeout=60.0,           # Request timeout
    max_retries=5,        # Max retry attempts
    retry_delay=2.0,      # Initial retry delay
)

client = SwiftTrackClient(config=config)
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SWIFTTRACK_BASE_URL` | API base URL | `https://backend-swifttrack.ajayv.online` |
| `SWIFTTRACK_TOKEN` | Authentication token | `None` |
| `SWIFTTRACK_TIMEOUT` | Request timeout (seconds) | `30.0` |
| `SWIFTTRACK_MAX_RETRIES` | Max retry attempts | `3` |
| `SWIFTTRACK_VERIFY_SSL` | Verify SSL certificates | `true` |

## Context Manager

The client can be used as a context manager for automatic cleanup:

```python
from swifttrack import SwiftTrackClient

with SwiftTrackClient() as client:
    client.login("user@example.com", "password")
    addresses = client.addresses.list_addresses()
    # Client automatically closed when exiting context
```

## Logging

The SDK uses Python's standard logging. Enable debug logging to see request details:

```python
import logging

logging.basicConfig(level=logging.DEBUG)
logging.getLogger("swifttrack").setLevel(logging.DEBUG)

client = SwiftTrackClient()
# Now you'll see detailed request/response logs
```

## API Reference

See the full [API documentation](https://swifttrack.readthedocs.io) for detailed reference.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Made with ❤️ by the SwiftTrack Team
