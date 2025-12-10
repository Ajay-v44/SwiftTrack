from typing import Dict, Any, List
from fastmcp import FastMCP
from dotenv import load_dotenv

load_dotenv()
mcp = FastMCP("swifttrack-mcp")


@mcp.tool
def ping() -> str:
    """Health check for the MCP server."""
    return "swifttrack-mcp is alive"


# Provider Tools
@mcp.tool
def get_providers() -> List[Dict[Any, Any]]:

    """Get a list of all providers."""
    return [
        {
            "id": "provider-1",
            "name": "Provider 1",
            "description": "This is provider 1",
            "logo_url": "https://example.com/logo1.png",
            "website_url": "https://example.com/provider1",
            "supports_hyperlocal": True,
            "supports_courier": False,
            "supports_same_day": True,
            "supports_intercity": False,
            "servicable_areas": ["Area 1", "Area 2"],
        },
        {
            "id": "provider-2",
            "name": "Provider 2",
            "description": "This is provider 2",
            "logo_url": "https://example.com/logo2.png",
            "website_url": "https://example.com/provider2",
            "supports_hyperlocal": False,
            "supports_courier": True,
            "supports_same_day": False,
            "supports_intercity": True,
            "servicable_areas": ["Area 3", "Area 4"],
        },
    ]


if __name__ == "__main__":
    # HTTP MCP server, easier for debugging
    mcp.run(transport="http", host="127.0.0.1", port=8000)
