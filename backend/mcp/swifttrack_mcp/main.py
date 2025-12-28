import sys
from os.path import dirname, abspath
sys.path.append(dirname(dirname(abspath(__file__))))

from swifttrack_mcp.core.server import mcp
import swifttrack_mcp.tools.provider
import swifttrack_mcp.tools.auth
import swifttrack_mcp.tools.map

if __name__ == "__main__":
    # HTTP MCP server, easier for debugging
    mcp.run(transport="http", host="127.0.0.1", port=8000)
