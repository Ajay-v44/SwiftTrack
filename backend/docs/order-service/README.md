# Order Service Saved Address Flow

## Overview

This document describes the saved pickup address feature added to `OrderService`.

The goal is to remove repeated manual pickup address entry for:

- tenant users
- consumer/customers

Guest users are unchanged and must still provide pickup details manually in quote and create-order requests.

## Problem Statement

Previously, tenant and consumer flows required the client to send full pickup address details every time:

- while fetching a quote
- while creating an order

This caused repeated data entry, inconsistent address formatting, and a worse user experience for frequent users.

## Solution Summary

`OrderService` now provides address management APIs for tenant and consumer users.

Users can:

- create pickup addresses
- update saved pickup addresses
- delete saved pickup addresses
- list saved pickup addresses
- mark one address as default
- fetch the default address

Once an address is stored:

- quote APIs accept `pickupAddressId`
- create-order APIs accept `pickupAddressId`
- the backend resolves the full saved address from the database

For guest flows:

- no saved address support is used
- existing manual address behavior remains unchanged

## Scope

Supported:

- tenant quote flow
- tenant create-order flow
- consumer quote flow
- consumer create-order flow
- saved pickup address CRUD
- default address management

Not changed:

- guest quote flow
- guest create-order flow
- dropoff address handling

## Why This Lives In OrderService

This feature was implemented inside `OrderService` because:

- quote generation already happens in `OrderService`
- create-order orchestration already happens in `OrderService`
- booking-channel logic for tenant, consumer, and guest already exists there
- the service already resolves user context through auth

Keeping address resolution close to quote/create logic avoids extra service hops and keeps ownership clear.

## Data Model

New table:

- `user_addresses`

Entity:

- `UserAddress`

Supporting enum:

- `AddressOwnerType`

### Ownership Model

Saved addresses are scoped differently by actor type:

- tenant users: address belongs to `tenantId`
- consumers: address belongs to `ownerUserId`

This ensures:

- tenant users share pickup addresses within the tenant scope
- consumer users only access their own addresses

### Main Columns

- `id`
- `tenant_id`
- `owner_user_id`
- `owner_type`
- `label`
- `line_1`
- `line_2`
- `city`
- `state`
- `country`
- `pincode`
- `locality`
- `latitude`
- `longitude`
- `is_default`
- `created_at`
- `updated_at`

## Default Address Rules

The default behavior is:

- if the first address is created, it becomes default automatically
- if a new address is created with `isDefault=true`, existing default is cleared
- if an existing address is updated with `isDefault=true`, existing default is cleared
- if the default address is deleted, another remaining address is promoted as default

At any point, the intent is to have at most one default address for a tenant or consumer owner scope.

## API Endpoints

Base path:

- `/api/order/addresses`

These APIs require a valid `token` header and are available only for:

- tenant users
- consumer users

### 1. List Saved Addresses

`GET /api/order/addresses/v1`

Response:

- list of all saved addresses for the current tenant or consumer

### 2. Get Default Address

`GET /api/order/addresses/v1/default`

Response:

- the current default address

### 3. Create Address

`POST /api/order/addresses/v1`

Request body:

```json
{
  "label": "Warehouse",
  "line1": "12 MG Road",
  "line2": "Near Metro Station",
  "city": "Bengaluru",
  "state": "Karnataka",
  "country": "India",
  "pincode": "560001",
  "locality": "MG Road",
  "latitude": 12.9751,
  "longitude": 77.6050,
  "contactName": "Store Manager",
  "contactPhone": "9999999999",
  "businessName": "SwiftTrack Store",
  "notes": "Collect from back gate",
  "isDefault": true
}
```

Required fields:

- `line1`
- `city`
- `state`
- `country`
- `pincode`
- `latitude`
- `longitude`
- `contactName`
- `contactPhone`

### 4. Update Address

`PUT /api/order/addresses/v1/{addressId}`

Request body shape is the same as create.

### 5. Mark Address As Default

`POST /api/order/addresses/v1/{addressId}/default`

Response:

- updated address object with `isDefault=true`

### 6. Delete Address

`DELETE /api/order/addresses/v1/{addressId}`

Response:

```json
{
  "message": "Address deleted successfully"
}
```

## Request Contract Changes

This feature changes shared request DTOs used across services.

### Quote Request Change

File:

- `common/src/main/java/com/swifttrack/dto/providerDto/QuoteInput.java`

Old shape:

```json
{
  "pickupLat": 12.9751,
  "pickupLng": 77.6050,
  "dropoffLat": 12.9352,
  "dropoffLng": 77.6245
}
```

New shape:

```json
{
  "pickupAddressId": "f1db7c7f-3d8e-4d35-a1f0-bdcfbec0d4f1",
  "dropoffLat": 12.9352,
  "dropoffLng": 77.6245
}
```

Behavior:

- tenant and consumer flows resolve pickup coordinates using `pickupAddressId`
- guest flow still sends raw `pickupLat` and `pickupLng`

### Create Order Request Change

Tenant and consumer create-order endpoints use an address-based request contract instead of requiring manual pickup details.

Example:

```json
{
  "idempotencyKey": "order-123",
  "orderReference": "REF-123",
  "paymentType": "PREPAID",
  "pickupAddressId": "f1db7c7f-3d8e-4d35-a1f0-bdcfbec0d4f1",
  "dropoff": {
    "address": {
      "line1": "45 Residency Road",
      "line2": "",
      "city": "Bengaluru",
      "state": "Karnataka",
      "country": "India",
      "pincode": "560025",
      "locality": "Ashok Nagar",
      "latitude": 12.9665,
      "longitude": 77.6080
    },
    "contact": {
      "name": "Customer",
      "phone": "8888888888"
    }
  }
}
```

Behavior:

- tenant and consumer create-order flows resolve `pickupAddressId` to full pickup address details
- guest create-order flow still requires full manual `pickup.address`

## Runtime Flow

### Tenant Quote Flow

1. request arrives with `pickupAddressId`
2. `OrderService` reads user context from auth
3. service validates tenant scope
4. saved address is loaded from `user_addresses`
5. pickup latitude and longitude are injected into quote processing
6. quote continues through existing provider/internal-driver selection

### Consumer Quote Flow

1. request arrives with `pickupAddressId`
2. `OrderService` validates authenticated consumer
3. service ensures the address belongs to that consumer
4. pickup coordinates are resolved
5. quote generation continues

### Tenant/Consumer Create Flow

1. request arrives with `pickupAddressId`
2. `OrderService` resolves the stored address
3. saved address fields are copied into the normalized create-order payload
4. downstream logic continues unchanged
5. provider adapters and internal driver flow receive full pickup address data

### Guest Flow

1. guest request arrives without saved address support
2. guest sends manual pickup coordinates for quote
3. guest sends manual full pickup address for create-order
4. flow continues exactly as before

## Validation Rules

Address APIs validate:

- required address fields
- valid token presence
- supported actor type
- access ownership for update/delete/default actions

Quote and create flow validations now also enforce:

- `pickupAddressId` is required for tenant and consumer quote flows
- `pickupAddressId` is required for tenant and consumer create-order flows
- guest flow still requires manual pickup fields

## Authorization Rules

### Tenant

Tenant users can only access addresses mapped to:

- their `tenantId`

### Consumer

Consumers can only access addresses mapped to:

- their own `ownerUserId`

### Guest

Guests cannot:

- create saved addresses
- update saved addresses
- delete saved addresses
- use saved addresses in quote/create flows

## Files Added

- `services/OrderService/src/main/java/com/swifttrack/OrderService/controllers/AddressController.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/dto/AddressRequest.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/dto/AddressResponse.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/dto/AddressQuoteRequest.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/dto/AddressCreateOrderRequest.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/models/UserAddress.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/models/enums/AddressOwnerType.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/repositories/UserAddressRepository.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/services/AddressService.java`
- `services/OrderService/src/main/resources/db/changelog/changes/015-create-user-addresses-table.yaml`
- `services/OrderService/src/main/resources/db/changelog/changes/016-add-contact-fields-to-user-addresses.yaml`

## Files Updated

- `common/src/main/java/com/swifttrack/dto/providerDto/QuoteInput.java`
- `common/src/main/java/com/swifttrack/dto/orderDto/CreateOrderRequest.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/controllers/OrderController.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/services/OrderServices.java`
- `services/OrderService/src/main/java/com/swifttrack/OrderService/services/InternalDriverAssignmentConsumer.java`
- `services/OrderService/src/main/resources/db/changelog/db.changelog-master.yaml`

## Migration

Liquibase change added:

- `015-create-user-addresses-table.yaml`
- `016-add-contact-fields-to-user-addresses.yaml`

This must be applied before using the new address APIs in any environment.

## Backward Compatibility Notes

This is not fully backward compatible for tenant and consumer clients.

Client changes required:

- tenant quote clients should stop sending manual pickup coordinates and send `pickupAddressId`
- consumer quote clients should stop sending manual pickup coordinates and send `pickupAddressId`
- tenant create-order clients should send `pickupAddressId`
- consumer create-order clients should send `pickupAddressId`

Guest clients do not need any change for pickup handling.

## Example Client Rollout Plan

1. deploy Liquibase migration
2. deploy updated `common`, `OrderService`, and dependent services
3. expose saved address management UI/API usage in frontend or client apps
4. update tenant and consumer quote requests to use saved address ids
5. update tenant and consumer create-order requests to use saved address ids
6. leave guest flow unchanged

## Operational Notes

- address lookup happens before quote/create execution
- provider adapters still receive full resolved address objects
- internal driver fallback flow remains compatible because normalized payload still contains full address data

## Known Constraints

- only pickup address is saved and resolved
- pickup contact name, phone, business name, and notes are stored inside the saved address
- dropoff address is still client-provided manually
- default uniqueness is enforced in service logic, not by a database partial unique index
- tenant addresses are tenant-scoped, not per individual tenant user

## Future Improvements

- add dropoff address book support
- add soft delete if audit history is needed
- add search/filter by label or locality
- add database-level protection for one-default-per-owner
- add tests for address ownership and default promotion behavior

## Related Source Files

- [OrderServices.java](/home/ajay/Ajay/Personal/SwiftTrack/backend/services/OrderService/src/main/java/com/swifttrack/OrderService/services/OrderServices.java)
- [AddressService.java](/home/ajay/Ajay/Personal/SwiftTrack/backend/services/OrderService/src/main/java/com/swifttrack/OrderService/services/AddressService.java)
- [AddressController.java](/home/ajay/Ajay/Personal/SwiftTrack/backend/services/OrderService/src/main/java/com/swifttrack/OrderService/controllers/AddressController.java)
- [QuoteInput.java](/home/ajay/Ajay/Personal/SwiftTrack/backend/common/src/main/java/com/swifttrack/dto/providerDto/QuoteInput.java)
- [CreateOrderRequest.java](/home/ajay/Ajay/Personal/SwiftTrack/backend/common/src/main/java/com/swifttrack/dto/orderDto/CreateOrderRequest.java)
- [015-create-user-addresses-table.yaml](/home/ajay/Ajay/Personal/SwiftTrack/backend/services/OrderService/src/main/resources/db/changelog/changes/015-create-user-addresses-table.yaml)
- [016-add-contact-fields-to-user-addresses.yaml](/home/ajay/Ajay/Personal/SwiftTrack/backend/services/OrderService/src/main/resources/db/changelog/changes/016-add-contact-fields-to-user-addresses.yaml)
