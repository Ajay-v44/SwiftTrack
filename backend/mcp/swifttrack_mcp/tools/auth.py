from typing import Any, Dict, List, Optional
from swifttrack_mcp.core.server import mcp
from swifttrack_mcp.core.config import config
from swifttrack_mcp.utils.api_client import APIClient

client = APIClient(config.AUTH_SERVICE_URL)

@mcp.tool
def register_user(user_data: Dict[str, Any]) -> Any:
    """
    Register a new user.
    
    Args:
        user_data: Dictionary containing email, mobile, password, etc.
    """
    return client.post("api/users/v1/register", json=user_data)

@mcp.tool
def login_email_password(login_data: Dict[str, Any]) -> Any:
    """
    Login with email and password.
    
    Args:
        login_data: Dictionary containing email and password.
    """
    return client.post("api/users/v1/login/emailAndPassword", json=login_data)

@mcp.tool
def get_user_details(token: str) -> Any:
    """
    Get user details from token.
    
    Args:
        token: Authorization token.
    """
    # The controller method uses @RequestParam String token, not header, and it's a POST
    return client.post("api/users/v1/getUserDetails", params={"token": token})

@mcp.tool
def assign_admin(tenant_id: str, token: str) -> Any:
    """
    Assign admin role to a user.
    
    Args:
        tenant_id: UUID of the tenant.
        token: Authorization token.
    """
    return client.post("api/users/v1/assignAdmin", params={"token": token, "tenantId": tenant_id})
