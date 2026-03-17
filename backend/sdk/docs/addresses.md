# Address Management

The Address API allows you to manage pickup and delivery locations for orders.

## Listing Addresses

Get all saved addresses for the authenticated user:

```python
addresses = client.addresses.list_addresses()

for addr in addresses:
    print(f"{addr.label}: {addr.line1}, {addr.city}")
    print(f"  Default: {addr.is_default}")
```

## Getting a Specific Address

```python
from uuid import UUID

# Get by UUID
address = client.addresses.get_address(UUID("address-uuid"))

# Or use string
address = client.addresses.get_address("address-uuid-string")

print(f"Address: {address.line1}")
print(f"City: {address.city}")
print(f"Contact: {address.contact_name} ({address.contact_phone})")
```

## Getting Default Address

```python
try:
    default = client.addresses.get_default_address()
    print(f"Default address: {default.label}")
except NotFoundError:
    print("No default address set")
```

## Creating an Address

```python
from swifttrack.models.address import AddressRequest

new_address = client.addresses.create_address(
    AddressRequest(
        label="Warehouse",  # Optional: helps identify the address
        line1="789 Industrial Area",
        line2="Phase 2, Sector 10",  # Optional
        city="Mumbai",
        state="Maharashtra",
        country="India",  # Defaults to India
        pincode="400001",
        locality="Andheri East",  # Optional
        latitude=19.1136,  # Optional but recommended
        longitude=72.8697,
        contact_name="John Doe",  # Optional
        contact_phone="+91-9876543210",  # Optional
        business_name="Acme Corp",  # Optional
        notes="Gate code: 1234",  # Optional
        is_default=True,  # Set as default immediately
    )
)

print(f"Created: {new_address.id}")
```

### Address Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `label` | str | No | Address label (e.g., "Home", "Office") |
| `line1` | str | Yes | Street address |
| `line2` | str | No | Apartment, floor, etc. |
| `city` | str | Yes | City name |
| `state` | str | Yes | State/Province |
| `country` | str | No | Country (default: India) |
| `pincode` | str | Yes | Postal code |
| `locality` | str | No | Neighborhood/Area |
| `latitude` | float | No | Latitude (-90 to 90) |
| `longitude` | float | No | Longitude (-180 to 180) |
| `contact_name` | str | No | Contact person |
| `contact_phone` | str | No | Contact phone |
| `business_name` | str | No | Company name |
| `notes` | str | No | Delivery notes |
| `is_default` | bool | No | Set as default address |

## Updating an Address

```python
from swifttrack.models.address import AddressRequest

updated = client.addresses.update_address(
    address_id="address-uuid",
    address=AddressRequest(
        label="Updated Office",
        line1="123 New Street",
        city="Bangalore",
        state="Karnataka",
        pincode="560001",
    ),
)

print(f"Updated: {updated.label}")
```

## Setting Default Address

```python
result = client.addresses.set_default("address-uuid")
print(f"Set as default: {result.is_default}")
```

## Deleting an Address

```python
response = client.addresses.delete_address("address-uuid")
print(response["message"])  # "Address deleted successfully"
```

## Complete Example

```python
from swifttrack import SwiftTrackClient
from swifttrack.models.address import AddressRequest, Coordinates

def manage_addresses(client: SwiftTrackClient):
    """Demonstrate address management operations."""
    
    # List existing addresses
    print("📍 Your addresses:")
    addresses = client.addresses.list_addresses()
    
    if not addresses:
        print("No addresses saved")
    else:
        for addr in addresses:
            default_marker = " ⭐" if addr.is_default else ""
            print(f"  {addr.label}{default_marker}: {addr.line1}, {addr.city}")
    
    # Create new address
    print("\n🏠 Creating new address...")
    new_addr = client.addresses.create_address(
        AddressRequest(
            label="Store",
            line1="456 Market Street",
            city="Delhi",
            state="Delhi",
            pincode="110001",
            latitude=28.6139,
            longitude=77.2090,
            contact_name="Store Manager",
            contact_phone="+91-9876543210",
        )
    )
    print(f"Created: {new_addr.id}")
    
    # Set as default
    print("\n⭐ Setting as default...")
    default_addr = client.addresses.set_default(new_addr.id)
    print(f"Default: {default_addr.is_default}")
    
    # Update address
    print("\n✏️ Updating address...")
    updated = client.addresses.update_address(
        new_addr.id,
        AddressRequest(
            label="Main Store",
            line1="456 Market Street, Block A",
            city="Delhi",
            state="Delhi",
            pincode="110001",
        ),
    )
    print(f"Updated label: {updated.label}")
    
    # Cleanup
    print("\n🗑️ Deleting address...")
    result = client.addresses.delete_address(new_addr.id)
    print(result["message"])

# Usage
with SwiftTrackClient() as client:
    client.login("user@example.com", "password")
    manage_addresses(client)
```

## Error Handling

```python
from swifttrack import NotFoundError, ValidationError

try:
    address = client.addresses.get_address("invalid-uuid")
except NotFoundError:
    print("Address not found")

try:
    # Invalid coordinates
    client.addresses.create_address(
        AddressRequest(
            line1="Test",
            city="Test",
            state="Test",
            pincode="123456",
            latitude=200,  # Invalid: must be -90 to 90
            longitude=0,
        )
    )
except ValidationError as e:
    print(f"Validation error: {e.errors}")
```
