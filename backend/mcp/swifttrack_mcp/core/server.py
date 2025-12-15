from fastmcp import FastMCP

mcp = FastMCP("swifttrack-mcp")

@mcp.tool
def ping() -> str:
    """Health check for the MCP server."""
    return "swifttrack-mcp is alive"
