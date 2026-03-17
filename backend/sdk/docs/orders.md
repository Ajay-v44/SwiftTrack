# Order Management

The Order API allows you to get delivery quotes, create orders, track shipments, and manage the complete delivery lifecycle.

## Getting Delivery Quotes

### For Authenticated Users

Get delivery quotes using a saved address:

```python
from uuid import UUID

# Get quote for delivery
quote = client.orders.get_quote(
    pickup_address_id=UUID("your-address-id"),
    dropoff_lat=19.0760,
    dropoff_lng=72.8777,
)

print(f"Quote Session: {quote.quote_session_id}")
print(f"Expires: {quote.expires_at}")

for option in quote.quotes:
    print(f"\n{option.provider_name}")
    print(f"  Service: {option.service_type}")
    print(f"  Price: ₹{option.price}")
    print(f"  Distance: {option.distance_km} km")
    print(f"  ETA: {option.estimated_delivery_time}")
```

### Guest Quotes (No Authentication)

Get quotes without logging in:

```python
quote = client.orders.get_guest_quote(
    pickup_lat=19.0760,
    pickup_lng=72.8777,
    pickup_address="123 Pickup St, Mumbai",
    dropoff_lat=19.2183,
    dropoff_lng=72.9781,
    dropoff_address="456 Dropoff St, Mumbai",
    package_weight_kg=2.5,
)

for option in quote.options:
    print(f"{option.provider_name}: ₹{option.price}")
```

## Creating an Order

### Using Authenticated Quote

```python
from swifttrack.models.order import CreateOrderRequest, LocationPoint, Item, PackageInfo
from uuid import UUID

# Build order request
order_request = CreateOrderRequest(
    idempotency_key="unique-key-123",  # Prevents duplicate orders
    tenant_id="tenant-uuid",  # Optional
    quote_id="selected-quote-uuid",  # Optional: specific quote
    order_reference="MY-ORDER-001",  # Your internal reference
    order_type="IMMEDIATE",  # IMMEDIATE, SCHEDULED, RECURRING
    payment_type="PREPAID",  # PREPAID, COD, WALLET
    pickup_address_id=UUID("pickup-address-id"),
    dropoff=LocationPoint(
        latitude=19.0760,
        longitude=72.8777,
        address="456 Delivery St, Mumbai",
    ),
    items=[
        Item(
            name="Package",
            quantity=1,
            weight_kg=2.5,
            dimensions={"length": 30, "width": 20, "height": 15},
            value=1000.0,
        ),
    ],
    package_info=PackageInfo(
        weight_kg=2.5,
        dimensions={"length": 30, "width": 20, "height": 15},
        fragile=False,
        temperature_controlled=False,
    ),
    delivery_instructions="Leave at reception",
)

# Create order with quote session
order = client.orders.create_order(
    request=order_request,
    quote_session_id=quote.quote_session_id,
)

print(f"✅ Order created: {order.order_number}")
print(f"📦 Status: {order.status.value}")
print(f"🔗 Track: {order.tracking_url}")
```

### Order Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `idempotency_key` | str | Yes | Unique key to prevent duplicates |
| `pickup_address_id` | UUID | Yes | Pickup address UUID |
| `dropoff` | LocationPoint | Yes | Dropoff location |
| `order_type` | OrderType | No | IMMEDIATE, SCHEDULED, RECURRING |
| `payment_type` | PaymentType | No | PREPAID, COD, WALLET |
| `quote_id` | str | No | Selected quote ID |
| `order_reference` | str | No | Your internal reference |
| `items` | list[Item] | No | Items to deliver |
| `package_info` | PackageInfo | No | Package details |
| `delivery_instructions` | str | No | Special instructions |

## Tracking Orders

### Get Order Status

```python
status = client.orders.get_order_status(order_id)
print(f"Order status: {status}")
# PENDING, QUOTED, CONFIRMED, ASSIGNED, PICKED_UP, 
# IN_TRANSIT, DELIVERED, CANCELLED, FAILED
```

### Get Full Order Details

```python
order = client.orders.get_order(order_id)

print(f"Order: {order.order_number}")
print(f"Status: {order.status.value}")
print(f"Price: ₹{order.price} {order.currency}")
print(f"Payment: {order.payment_type.value}")
print(f"Created: {order.created_at}")

if order.estimated_delivery:
    print(f"Estimated: {order.estimated_delivery}")

if order.actual_delivery:
    print(f"Delivered: {order.actual_delivery}")

if order.tracking_url:
    print(f"Track: {order.tracking_url}")
```

## Cancelling Orders

```python
# Cancel with reason
result = client.orders.cancel_order(
    order_id="order-uuid",
    reason="Customer requested cancellation",
)
print(result["message"])

# Cancel without reason
result = client.orders.cancel_order(order_id="order-uuid")
```

## Order Status Lifecycle

```
PENDING → QUOTED → CONFIRMED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
                                    ↓
                              CANCELLED/FAILED
```

| Status | Description |
|--------|-------------|
| `PENDING` | Order created, awaiting quote |
| `QUOTED` | Quote received, awaiting confirmation |
| `CONFIRMED` | Order confirmed, finding provider |
| `ASSIGNED` | Driver/provider assigned |
| `PICKED_UP` | Package picked up |
| `IN_TRANSIT` | Package in transit |
| `DELIVERED` | Package delivered |
| `CANCELLED` | Order cancelled |
| `FAILED` | Delivery failed |

## Complete Workflow Example

```python
from swifttrack import SwiftTrackClient
from swifttrack.models.order import (
    CreateOrderRequest,
    LocationPoint,
    Item,
    PackageInfo,
)
from uuid import UUID

def create_delivery_order(client: SwiftTrackClient):
    """Complete order creation workflow."""
    
    # 1. Get pickup address
    addresses = client.addresses.list_addresses()
    if not addresses:
        raise ValueError("No pickup addresses available")
    
    pickup_address = addresses[0]
    print(f"📍 Pickup: {pickup_address.label}")
    
    # 2. Get quote
    print("💰 Getting quotes...")
    quote = client.orders.get_quote(
        pickup_address_id=pickup_address.id,
        dropoff_lat=19.0760,
        dropoff_lng=72.8777,
    )
    
    if not quote.quotes:
        raise ValueError("No delivery options available")
    
    # 3. Select best option (cheapest in this example)
    best_option = min(quote.quotes, key=lambda q: q.price)
    print(f"✅ Selected: {best_option.provider_name} @ ₹{best_option.price}")
    
    # 4. Create order
    print("📦 Creating order...")
    order_request = CreateOrderRequest(
        idempotency_key=f"order-{UUID(int=0)}-{pickup_address.id}",
        pickup_address_id=pickup_address.id,
        dropoff=LocationPoint(
            latitude=19.0760,
            longitude=72.8777,
            address="456 Delivery Street, Mumbai 400001",
        ),
        order_reference="DEMO-ORDER-001",
        items=[
            Item(name="Documents", quantity=1, weight_kg=0.5),
        ],
        package_info=PackageInfo(weight_kg=0.5),
        delivery_instructions="Deliver to reception",
    )
    
    order = client.orders.create_order(order_request, quote.quote_session_id)
    
    print(f"🎉 Order created!")
    print(f"   Number: {order.order_number}")
    print(f"   Status: {order.status.value}")
    print(f"   Track: {order.tracking_url}")
    
    return order

# Usage
with SwiftTrackClient() as client:
    client.login("user@example.com", "password")
    order = create_delivery_order(client)
```

## Guest Order Flow

For unauthenticated users:

```python
# 1. Get guest quote
quote = client.orders.get_guest_quote(
    pickup_lat=19.0760,
    pickup_lng=72.8777,
    pickup_address="123 Pickup St",
    dropoff_lat=19.2183,
    dropoff_lng=72.9781,
    dropoff_address="456 Dropoff St",
    package_weight_kg=2.5,
)

# 2. Create guest order (requires guest token)
# Note: This is typically done via a different endpoint
# that accepts guest access tokens
```

## Error Handling

```python
from swifttrack import NotFoundError, ValidationError, SwiftTrackError

try:
    order = client.orders.get_order("invalid-uuid")
except NotFoundError:
    print("Order not found")

try:
    # Invalid coordinates
    client.orders.get_quote(address_id, 200, 72)
except ValidationError as e:
    print(f"Invalid input: {e.errors}")

try:
    # Cancel already delivered order
    client.orders.cancel_order(delivered_order_id)
except SwiftTrackError as e:
    print(f"Cannot cancel: {e.message}")
```
