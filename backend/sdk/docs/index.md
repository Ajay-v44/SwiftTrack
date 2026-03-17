# Welcome to SwiftTrack Python SDK

A production-grade Python SDK for the SwiftTrack logistics platform.

## What is SwiftTrack?

SwiftTrack is a comprehensive logistics and delivery management platform that enables businesses and consumers to:

- **Manage Delivery Addresses**: Store and manage pickup/delivery locations
- **Get Delivery Quotes**: Compare prices and options from multiple providers
- **Create and Track Orders**: Full lifecycle order management
- **Manage Wallet/Account**: Track payments and transactions

## Quick Links

- [Installation](installation.md)
- [Quick Start](quickstart.md)
- [API Reference](api/client.md)

## Features

- 🔐 **Secure Authentication**: Token-based authentication with auto-refresh
- 🔄 **Automatic Retries**: Intelligent retry logic with exponential backoff
- 📦 **Type Safety**: Full Pydantic models with type hints
- 🛡️ **Error Handling**: Structured exceptions for all error cases
- 🚀 **Modern Python**: Supports Python 3.9+
- 📚 **Well Documented**: Comprehensive documentation and examples

## Example Usage

```python
from swifttrack import SwiftTrackClient

# Initialize and authenticate
client = SwiftTrackClient()
client.login("user@example.com", "password")

# List your addresses
addresses = client.addresses.list_addresses()

# Get a delivery quote
quote = client.orders.get_quote(
    pickup_address_id=addresses[0].id,
    dropoff_lat=19.0760,
    dropoff_lng=72.8777,
)

# Create an order
order = client.orders.create_order(order_request, quote.quote_session_id)

print(f"Order created: {order.order_number}")
print(f"Track at: {order.tracking_url}")
```

## Installation

```bash
pip install swifttrack
```

For development:

```bash
pip install swifttrack[dev]
```

## Getting Help

- 📖 [Documentation](https://swifttrack.readthedocs.io)
- 🐛 [Issue Tracker](https://github.com/swifttrack/swifttrack-python/issues)
- 💬 [Discussions](https://github.com/swifttrack/swifttrack-python/discussions)
