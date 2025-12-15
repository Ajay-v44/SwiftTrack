from typing import Any, Dict, List, Optional
from swifttrack_mcp.core.server import mcp
from swifttrack_mcp.core.config import config
from swifttrack_mcp.utils.api_client import APIClient

client = APIClient(config.PROVIDER_SERVICE_URL)

@mcp.tool
def get_providers() -> Any:
    """Get a list of all providers."""
    return client.get("api/providers/v1/list")

@mcp.tool
def create_provider(provider_data: Dict[str, Any], token: str) -> Any:
    """
    Create a new provider.
    
    Args:
        provider_data: Dictionary containing provider details and servicable areas.
        token: Authorization token.
    """
    headers = {"token": token}
    return client.post("api/providers/v1/create", json=provider_data, headers=headers)

@mcp.tool
def get_tenant_providers(token: str) -> Any:
    """
    Get providers for a specific tenant.
    
    Args:
        token: Authorization token.
    """
    headers = {"token": token}
    return client.get("api/providers/v1/getTenantProviders", headers=headers)

@mcp.tool
def configure_tenant_providers(provider_ids: List[str], token: str) -> Any:
    """
    Configure providers for a tenant.
    
    Args:
        provider_ids: List of provider UUIDs.
        token: Authorization token.
    """
    headers = {"token": token}
    return client.post("api/providers/v1/configureTenantProviders", json=provider_ids, headers=headers)

@mcp.tool
def request_provider_onboarding(onboarding_data: Dict[str, Any], token: str) -> Any:
    """
    Request provider onboarding.
    
    Args:
        onboarding_data: Dictionary containing onboarding details.
        token: Authorization token.
    """
    headers = {"token": token}
    return client.post("api/providers/v1/requestProviderOnboarding", json=onboarding_data, headers=headers)
