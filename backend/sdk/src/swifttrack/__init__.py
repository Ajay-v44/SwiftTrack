"""SwiftTrack Python SDK.

A production-grade Python SDK for the SwiftTrack logistics platform.
"""

from swifttrack.client import SwiftTrackClient
from swifttrack.config import SwiftTrackConfig
from swifttrack.exceptions import (
    SwiftTrackError,
    AuthenticationError,
    RateLimitError,
    APIError,
    ValidationError,
    NotFoundError,
    ServerError,
)

__version__ = "0.1.0"
__all__ = [
    "SwiftTrackClient",
    "SwiftTrackConfig",
    "SwiftTrackError",
    "AuthenticationError",
    "RateLimitError",
    "APIError",
    "ValidationError",
    "NotFoundError",
    "ServerError",
]
