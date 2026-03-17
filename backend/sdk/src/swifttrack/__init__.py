"""SwiftTrack Python SDK.

A production-grade Python SDK for the SwiftTrack logistics platform.
"""

from swifttrack.client import SwiftTrackClient
from swifttrack.config import SwiftTrackConfig
from swifttrack.exceptions import (
    APIError,
    AuthenticationError,
    NotFoundError,
    PermissionError,
    RateLimitError,
    ServerError,
    SwiftTrackError,
    ValidationError,
)

__version__ = "0.1.0"
__all__ = [
    "SwiftTrackClient",
    "SwiftTrackConfig",
    "SwiftTrackError",
    "AuthenticationError",
    "PermissionError",
    "RateLimitError",
    "APIError",
    "ValidationError",
    "NotFoundError",
    "ServerError",
]
