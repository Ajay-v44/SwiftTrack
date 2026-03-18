"""Main SwiftTrack client for interacting with the API."""

from __future__ import annotations

import logging
from collections.abc import Iterator
from contextlib import contextmanager
from types import TracebackType

from typing_extensions import Self

from swifttrack.account import AccountService
from swifttrack.address import AddressService
from swifttrack.auth import AuthService
from swifttrack.config import SwiftTrackConfig
from swifttrack.models.auth import LoginResponse
from swifttrack.order import OrderService
from swifttrack.utils.http_client import HTTPClient

logger = logging.getLogger(__name__)


class SwiftTrackClient:
    """Main client for interacting with the SwiftTrack API.

    This client provides a high-level interface to all SwiftTrack API endpoints,
    with automatic authentication, retry logic, and error handling.

    Example:
        >>> from swifttrack import SwiftTrackClient
        >>> client = SwiftTrackClient(base_url="https://api.example.com")
        >>> client.login("user@email.com", "password")
        >>> addresses = client.addresses.list_addresses()
        >>> order = client.orders.get_order(order_id)

    The client can be used as a context manager for automatic cleanup:
        >>> with SwiftTrackClient() as client:
        ...     client.login("user@email.com", "password")
        ...     # use client...
    """

    def __init__(
        self,
        base_url: str | None = None,
        token: str | None = None,
        config: SwiftTrackConfig | None = None,
        timeout: float = 30.0,
        max_retries: int = 3,
    ) -> None:
        """Initialize the SwiftTrack client.

        Args:
            base_url: The base URL for the SwiftTrack API.
                     Defaults to https://backend-swifttrack.ajayv.online
            token: Optional authentication token.
            config: Optional SwiftTrackConfig instance. If provided,
                   base_url, token, timeout, and max_retries are ignored.
            timeout: Request timeout in seconds (default: 30).
            max_retries: Maximum number of retries for failed requests (default: 3).

        Raises:
            ValueError: If configuration is invalid.
        """
        if config is not None:
            self._config = config
        else:
            # Use environment or default if base_url not provided
            if base_url is None:
                config = SwiftTrackConfig.from_env()
                base_url = config.base_url

            self._config = SwiftTrackConfig(
                base_url=base_url or "https://backend-swifttrack.ajayv.online",
                token=token,
                timeout=timeout,
                max_retries=max_retries,
            )

        self._http_client = HTTPClient(self._config)

        # Initialize services
        self._auth = AuthService(self._http_client)
        self._addresses = AddressService(self._http_client)
        self._orders = OrderService(self._http_client)
        self._accounts = AccountService(self._http_client)

        self._is_authenticated = self._config.token is not None

        logger.debug(f"SwiftTrackClient initialized with base_url: {self._config.base_url}")

    @property
    def config(self) -> SwiftTrackConfig:
        """Get the current configuration."""
        return self._config

    @property
    def is_authenticated(self) -> bool:
        """Check if the client has an authentication token."""
        return self._is_authenticated

    @property
    def auth(self) -> AuthService:
        """Access authentication operations."""
        return self._auth

    @property
    def addresses(self) -> AddressService:
        """Access address operations."""
        return self._addresses

    @property
    def orders(self) -> OrderService:
        """Access order operations."""
        return self._orders

    @property
    def accounts(self) -> AccountService:
        """Access account/wallet operations."""
        return self._accounts

    def login(self, email: str, password: str) -> LoginResponse:
        """Authenticate with email and password.

        This method authenticates the user and automatically stores the
        access token for subsequent API calls.

        Args:
            email: User email address.
            password: User password.

        Returns:
            LoginResponse containing the access token.

        Example:
            >>> response = client.login("user@example.com", "password123")
            >>> print(f"Token: {response.access_token}")

        Raises:
            AuthenticationError: If credentials are invalid.
            ValidationError: If request validation fails.
        """
        response = self._auth.login(email, password)
        self._set_token(response.access_token)
        return response

    def set_token(self, token: str) -> Self:
        """Set or update the authentication token.

        Use this method to manually set the token if you have it from
        an external source (e.g., stored from a previous session).

        Args:
            token: The authentication token.

        Returns:
            Self for method chaining.

        Example:
            >>> client.set_token("your-jwt-token")
            >>> # or chain it:
            >>> client.set_token("token").addresses.list_addresses()
        """
        self._set_token(token)
        return self

    def _set_token(self, token: str) -> None:
        """Internal method to set the token."""
        self._http_client.update_token(token)
        self._config = self._http_client.config
        self._is_authenticated = True
        logger.debug("Authentication token set")

    def logout(self) -> None:
        """Clear the authentication token.

        This method clears the stored token but does not invalidate it
        on the server. Call this when the user logs out of your application.
        """
        self._http_client.update_token("")
        self._is_authenticated = False
        logger.debug("Logged out, token cleared")

    def close(self) -> None:
        """Close the HTTP client and release resources.

        This method should be called when you're done using the client
        to properly close the underlying HTTP connection pool.
        """
        self._http_client.close()
        logger.debug("SwiftTrackClient closed")

    def __enter__(self) -> Self:
        """Enter context manager."""
        return self

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc_val: BaseException | None,
        exc_tb: TracebackType | None,
    ) -> None:
        """Exit context manager."""
        self.close()

    @contextmanager
    def temp_token(self, token: str) -> Iterator[Self]:
        """Context manager to temporarily use a different token.

        This is useful for operations that need a different user's token
        without affecting the main client's authentication.

        Args:
            token: Temporary token to use.

        Example:
            >>> with client.temp_token("other-user-token"):
            ...     # operations use the temporary token
            ...     addresses = client.addresses.list_addresses()
            >>> # original token is restored
        """
        original_token = self._config.token
        try:
            self._set_token(token)
            yield self
        finally:
            if original_token:
                self._set_token(original_token)
            else:
                self.logout()
