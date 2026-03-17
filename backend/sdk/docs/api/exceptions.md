# API Reference - Exceptions

## Base Exception

### SwiftTrackError

Base exception for all SwiftTrack SDK errors.

::: swifttrack.exceptions.SwiftTrackError
    handler: python
    options:
      show_root_heading: true

## Authentication Errors

### AuthenticationError

Raised when authentication fails or token is invalid.

::: swifttrack.exceptions.AuthenticationError
    handler: python
    options:
      show_root_heading: true

## Rate Limiting

### RateLimitError

Raised when API rate limit is exceeded.

::: swifttrack.exceptions.RateLimitError
    handler: python
    options:
      show_root_heading: true

## Validation Errors

### ValidationError

Raised when request validation fails.

::: swifttrack.exceptions.ValidationError
    handler: python
    options:
      show_root_heading: true

## Not Found Errors

### NotFoundError

Raised when a requested resource is not found.

::: swifttrack.exceptions.NotFoundError
    handler: python
    options:
      show_root_heading: true

## Server Errors

### ServerError

Raised when server returns 5xx error.

::: swifttrack.exceptions.ServerError
    handler: python
    options:
      show_root_heading: true

## General API Errors

### APIError

Raised for general API errors.

::: swifttrack.exceptions.APIError
    handler: python
    options:
      show_root_heading: true

## Timeout Errors

### TimeoutError

Raised when request times out.

::: swifttrack.exceptions.TimeoutError
    handler: python
    options:
      show_root_heading: true

## Conflict Errors

### ConflictError

Raised when there's a conflict (e.g., duplicate resource).

::: swifttrack.exceptions.ConflictError
    handler: python
    options:
      show_root_heading: true

## Permission Errors

### PermissionError

Raised when user doesn't have permission for the operation.

::: swifttrack.exceptions.PermissionError
    handler: python
    options:
      show_root_heading: true

## Exception Hierarchy

```
SwiftTrackError (base)
├── AuthenticationError (401)
├── RateLimitError (429)
├── ValidationError (400)
├── NotFoundError (404)
├── APIError
├── ServerError (5xx)
├── TimeoutError
├── ConflictError (409)
└── PermissionError (403)
```

## Error Handling Examples

### Catching Specific Errors

```python
from swifttrack import (
    SwiftTrackClient,
    AuthenticationError,
    NotFoundError,
    ValidationError,
)

client = SwiftTrackClient()

try:
    client.login("user@example.com", "wrong-password")
except AuthenticationError as e:
    print(f"Auth failed: {e.message}")
    print(f"Status code: {e.status_code}")

try:
    client.addresses.get_address("non-existent")
except NotFoundError as e:
    print(f"Not found: {e.resource_type} {e.resource_id}")

try:
    # Invalid request
    client.orders.get_quote(address_id, 200, 72)  # Invalid lat
except ValidationError as e:
    print(f"Validation errors: {e.errors}")
```

### Catching All SDK Errors

```python
from swifttrack import SwiftTrackError

try:
    # Any SDK operation
    client.orders.create_order(request, session_id)
except SwiftTrackError as e:
    # Catches all SDK-specific errors
    print(f"SDK Error [{e.status_code}]: {e.message}")
    if e.response_data:
        print(f"Response: {e.response_data}")
```

### Using Error Data

```python
from swifttrack import RateLimitError

try:
    client.addresses.list_addresses()
except RateLimitError as e:
    if e.retry_after:
        print(f"Rate limited. Retry after {e.retry_after} seconds")
        time.sleep(e.retry_after)
    else:
        print("Rate limited. Using default backoff")
```
