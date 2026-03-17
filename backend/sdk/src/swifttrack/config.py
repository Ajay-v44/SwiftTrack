"""SwiftTrack SDK configuration."""

from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Any


@dataclass
class SwiftTrackConfig:
    """Configuration for the SwiftTrack SDK.

    Attributes:
        base_url: The base URL for the SwiftTrack API.
        token: Optional authentication token.
        timeout: Request timeout in seconds (default: 30).
        max_retries: Maximum number of retries for failed requests (default: 3).
        retry_delay: Initial retry delay in seconds (default: 1.0).
        retry_backoff: Backoff multiplier for retries (default: 2.0).
        retry_max_delay: Maximum retry delay in seconds (default: 60.0).
        verify_ssl: Whether to verify SSL certificates (default: True).
    """

    base_url: str = "https://backend-swifttrack.ajayv.online"
    token: str | None = None
    timeout: float = 30.0
    max_retries: int = 3
    retry_delay: float = 1.0
    retry_backoff: float = 2.0
    retry_max_delay: float = 60.0
    verify_ssl: bool = True

    def __post_init__(self) -> None:
        """Validate configuration after initialization."""
        if not self.base_url:
            raise ValueError("base_url is required")
        # Remove trailing slash from base_url
        self.base_url = self.base_url.rstrip("/")

        if self.timeout <= 0:
            raise ValueError("timeout must be positive")
        if self.max_retries < 0:
            raise ValueError("max_retries must be non-negative")
        if self.retry_delay <= 0:
            raise ValueError("retry_delay must be positive")
        if self.retry_backoff < 1:
            raise ValueError("retry_backoff must be >= 1")

    @classmethod
    def from_env(cls) -> SwiftTrackConfig:
        """Create configuration from environment variables.

        Environment variables:
            SWIFTTRACK_BASE_URL: API base URL
            SWIFTTRACK_TOKEN: Authentication token
            SWIFTTRACK_TIMEOUT: Request timeout
            SWIFTTRACK_MAX_RETRIES: Maximum retries
            SWIFTTRACK_VERIFY_SSL: Verify SSL (true/false)

        Returns:
            SwiftTrackConfig instance from environment.
        """
        return cls(
            base_url=os.getenv("SWIFTTRACK_BASE_URL", "https://backend-swifttrack.ajayv.online"),
            token=os.getenv("SWIFTTRACK_TOKEN") or None,
            timeout=float(os.getenv("SWIFTTRACK_TIMEOUT", "30.0")),
            max_retries=int(os.getenv("SWIFTTRACK_MAX_RETRIES", "3")),
            verify_ssl=os.getenv("SWIFTTRACK_VERIFY_SSL", "true").lower() == "true",
        )

    def with_token(self, token: str) -> SwiftTrackConfig:
        """Return a new config with the specified token.

        Args:
            token: Authentication token.

        Returns:
            New SwiftTrackConfig with updated token.
        """
        return SwiftTrackConfig(
            base_url=self.base_url,
            token=token,
            timeout=self.timeout,
            max_retries=self.max_retries,
            retry_delay=self.retry_delay,
            retry_backoff=self.retry_backoff,
            retry_max_delay=self.retry_max_delay,
            verify_ssl=self.verify_ssl,
        )

    def to_dict(self) -> dict[str, Any]:
        """Convert configuration to dictionary.

        Returns:
            Dictionary representation of config.
        """
        return {
            "base_url": self.base_url,
            "token": self.token,
            "timeout": self.timeout,
            "max_retries": self.max_retries,
            "retry_delay": self.retry_delay,
            "retry_backoff": self.retry_backoff,
            "retry_max_delay": self.retry_max_delay,
            "verify_ssl": self.verify_ssl,
        }
