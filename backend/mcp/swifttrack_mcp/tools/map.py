from typing import Any, Dict, List, Optional
from swifttrack_mcp.core.server import mcp
from swifttrack_mcp.core.config import config
from swifttrack_mcp.utils.api_client import APIClient

client = APIClient(config.MAP_SERVICE_URL)

@mcp.tool
def reverse_geocode(lat: float, lng: float) -> Any:
    """
    Reverse Geocoding: Convert coordinates to address.
    
    Args:
        lat: Latitude.
        lng: Longitude.
    """
    return client.get("map/reverse", params={"lat": lat, "lng": lng})

@mcp.tool
def search_location(query: str, limit: int = 5) -> Any:
    """
    Forward Geocoding: Search for a location.
    
    Args:
        query: Address or place name to search.
        limit: Maximum results (default 5).
    """
    return client.get("map/search", params={"query": query, "limit": limit})

@mcp.tool
def get_directions(origin: Dict[str, float], destination: Dict[str, float], mode: str = "DRIVING") -> Any:
    """
    Get directions between two points.
    
    Args:
        origin: Dict with 'latitude' and 'longitude'.
        destination: Dict with 'latitude' and 'longitude'.
        mode: Travel mode (DRIVING, etc.).
    """
    data = {
        "origin": origin,
        "destination": destination,
        "mode": mode
    }
    return client.post("map/directions", json=data)

@mcp.tool
def calculate_eta(origin: Dict[str, float], destination: Dict[str, float], mode: str = "DRIVING") -> Any:
    """
    Calculate ETA between two points.
    
    Args:
        origin: Dict with 'latitude' and 'longitude'.
        destination: Dict with 'latitude' and 'longitude'.
        mode: Travel mode.
    """
    data = {
        "origin": origin,
        "destination": destination,
        "mode": mode
    }
    return client.post("map/eta", json=data)
