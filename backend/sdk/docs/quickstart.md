# Quick Start Guide

This guide will help you get up and running with the SwiftTrack SDK in minutes.

## 1. Installation

```bash
pip install swifttrack
```

## 2. Basic Usage

### Initialize Client and Login

```python
from swifttrack import SwiftTrackClient

# Create client (uses default base URL)
client = SwiftTrackClient()

# Login with email and password
response = client.login("your@email.com", "your-password")
print(f"Logged in! Token expires... never (JWT)")
```

### Using with Context Manager

```python
from swifttrack import SwiftTrackClient

with SwiftTrackClient() as client:
    client.login("your@email.com", "your-password")
    # Client automatically closes when done
    addresses = client.addresses.list_addresses()
```

## 3. Working with Addresses

```python
from swifttrack.models.address import AddressRequest

# List addresses
addresses = client.addresses.list_addresses()
print(f"You have {len(addresses)} addresses")

# Create new address
new_address = client.addresses.create_address(
    AddressRequest(
        label="Home",
        line1="123 Main Street",
        city="Mumbai",
        state="Maharashtra",
        pincode="400001",
        latitude=19.0760,
        longitude=72.8777,
    )
)
print(f"Created: {new_address.id}")

# Set as default
client.addresses.set_default(new_address.id)
```

## 4. Getting Delivery Quotes

```python
from uuid import UUID

# Get quote for delivery
quote = client.orders.get_quote(
    pickup_address_id=UUID("your-address-id"),
    dropoff_lat=19.0760,
    dropoff_lng=72.8777,
)

# Show options
for i, option in enumerate(quote.quotes, 1):
    print(f"{i}. {option.provider_name}: ₹{option.price}")
    print(f"   Estimated delivery: {option.estimated_delivery_time}")
```

## 5. Creating an Order

```python
from swifttrack.models.order import CreateOrderRequest, LocationPoint
from uuid import UUID

# Create order request
order_request = CreateOrderRequest(
    idempotency_key="order-123-unique-key",  # Prevents duplicates
    pickup_address_id=UUID("your-address-id"),
    dropoff=LocationPoint(
        latitude=19.0760,
        longitude=72.8777,
        address="456 Delivery St, Mumbai",
    ),
    order_reference="MY-ORDER-001",
)

# Create the order
order = client.orders.create_order(order_request, quote.quote_session_id)

print(f"✅ Order created: {order.order_number}")
print(f"📦 Status: {order.status.value}")
print(f"🔗 Track: {order.tracking_url}")
```

## 6. Complete Example

Here's a complete workflow from login to order creation:

```python
from swifttrack import SwiftTrackClient
from swifttrack.models.address import AddressRequest
from swifttrack.models.order import CreateOrderRequest, LocationPoint
from uuid import UUID

def main():
    # Initialize
    client = SwiftTrackClient()
    
    # Login
    print("🔐 Logging in...")
    client.login("user@example.com", "password")
    
    # Get or create address
    addresses = client.addresses.list_addresses()
    if not addresses:
        print("📍 Creating pickup address...")
        address = client.addresses.create_address(
            AddressRequest(
                label="Office",
                line1="123 Business Park",
                city="Bangalore",
                state="Karnataka",
                pincode="560001",
                latitude=12.9716,
                longitude=77.5946,
            )
        )
    else:
        address = addresses[0]
    
    # Get quote
    print("💰 Getting delivery quotes...")
    quote = client.orders.get_quote(
        pickup_address_id=address.id,
        dropoff_lat=12.9352,
        dropoff_lng=77.6245,
    )
    
    if not quote.quotes:
        print("❌ No delivery options available")
        return
    
    # Select first option
    selected = quote.quotes[0]
    print(f"✅ Selected: {selected.provider_name} @ ₹{selected.price}")
    
    # Create order
    print("📦 Creating order...")
    order_request = CreateOrderRequest(
        idempotency_key=f"demo-order-{datetime.now().timestamp()}",
        pickup_address_id=address.id,
        dropoff=LocationPoint(
            latitude=12.9352,
            longitude=77.6245,
            address="456 Delivery Address, Bangalore",
        ),
    )
    
    order = client.orders.create_order(order_request, quote.quote_session_id)
    
    print(f"🎉 Success!")
    print(f"   Order: {order.order_number}")
    print(f"   Track: {order.tracking_url}")

if __name__ == "__main__":
    from datetime import datetime
    main()
```

## Next Steps

- Learn about [Authentication](authentication.md)
- Explore [Address Management](addresses.md)
- Understand [Order Lifecycle](orders.md)
- Check [API Reference](api/client.md)
