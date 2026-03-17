# Authentication

The SwiftTrack SDK uses JWT token-based authentication. This guide covers all authentication methods.

## Login with Email/Password

The primary authentication method is email/password login:

```python
from swifttrack import SwiftTrackClient

client = SwiftTrackClient()
response = client.login("user@example.com", "password123")

print(f"Access token: {response.access_token}")
print(f"Token type: {response.token_type}")
```

The SDK automatically stores the token for subsequent requests. No manual header management needed!

## Using Existing Token

If you already have a token (e.g., from a previous session or external source):

```python
from swifttrack import SwiftTrackClient

# Method 1: Pass token during initialization
client = SwiftTrackClient(token="your-jwt-token")

# Method 2: Set token after initialization
client = SwiftTrackClient()
client.set_token("your-jwt-token")

# Method 3: Chain the method
client.set_token("your-jwt-token").addresses.list_addresses()
```

## Check Authentication Status

```python
if client.is_authenticated:
    print("User is logged in")
else:
    print("User is not authenticated")
```

## Logout

To clear the authentication token:

```python
client.logout()
print(client.is_authenticated)  # False
```

Note: This only clears the local token. It does not invalidate the token on the server.

## Environment Variables

Configure authentication via environment:

```bash
export SWIFTTRACK_TOKEN="your-jwt-token"
```

```python
from swifttrack import SwiftTrackClient

# Automatically uses SWIFTTRACK_TOKEN from environment
client = SwiftTrackClient()
print(client.is_authenticated)  # True if token was set
```

## Temporary Token Switching

Use a different token temporarily without affecting the main client:

```python
# Current token
client.set_token("main-user-token")

# Use different token in a context
with client.temp_token("other-user-token"):
    # All requests use other-user-token
    addresses = client.addresses.list_addresses()

# Back to main-user-token
```

This is useful for admin operations on behalf of other users.

## Get User Details

Validate a token and get user information:

```python
user = client.auth.get_user_details("some-token")

print(f"User ID: {user.id}")
print(f"Email: {user.email}")
print(f"Name: {user.name}")
print(f"Tenant ID: {user.tenant_id}")
print(f"Roles: {user.roles}")
```

## Error Handling

Handle authentication errors gracefully:

```python
from swifttrack import AuthenticationError, ValidationError

try:
    client.login("user@example.com", "wrong-password")
except AuthenticationError as e:
    print(f"Login failed: {e.message}")
    # Redirect to login page

except ValidationError as e:
    print(f"Invalid input: {e.errors}")
    # Show validation errors to user
```

## Token Security Best Practices

1. **Store tokens securely**: Use environment variables or secure vaults
2. **Don't log tokens**: Never print or log JWT tokens
3. **Use HTTPS**: Always verify SSL in production
4. **Token expiration**: Handle 401 errors by re-authenticating
5. **Context managers**: Use `with` statement for automatic cleanup

```python
# ✅ Good: Secure token handling
import os
from swifttrack import SwiftTrackClient

token = os.environ.get("SWIFTTRACK_TOKEN")
client = SwiftTrackClient(token=token)

# ❌ Bad: Hardcoded token
client = SwiftTrackClient(token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
```

## Complete Authentication Flow

```python
from swifttrack import SwiftTrackClient, AuthenticationError

def authenticate_client() -> SwiftTrackClient:
    """Create and authenticate a client."""
    client = SwiftTrackClient()
    
    # Try to use existing token
    if client.is_authenticated:
        try:
            # Validate token
            user = client.auth.get_user_details(client.config.token)
            print(f"Authenticated as {user.email}")
            return client
        except AuthenticationError:
            print("Token expired, need to login")
            client.logout()
    
    # Login with credentials
    try:
        client.login("user@example.com", "password")
        return client
    except AuthenticationError as e:
        raise Exception(f"Authentication failed: {e.message}")

# Usage
client = authenticate_client()
```
