"""HTTP client utilities for SwiftTrack SDK."""

from __future__ import annotations

import logging
from typing import Any

import httpx

from swifttrack.config import SwiftTrackConfig
from swifttrack.exceptions import (
    APIError,
    AuthenticationError,
    ConflictError,
    NotFoundError,
    PermissionError,
    RateLimitError,
    ServerError,
    SwiftTrackError,
    TimeoutError,
    ValidationError,
)
from swifttrack.utils.retry import RetryHandler

logger = logging.getLogger(__name__)


class HTTPClient:
    """HTTP client for making API requests with retry logic."""

    def __init__(self, config: SwiftTrackConfig) -> None:
        self.config = config
        self.retry_handler = RetryHandler(config)
        self._client: httpx.Client | None = None

    @property
    def client(self) -> httpx.Client:
        """Get or create the HTTP client."""
        if self._client is None or self._client.is_closed:
            self._client = httpx.Client(
                timeout=httpx.Timeout(self.config.timeout),
                verify=self.config.verify_ssl,
                headers=self._default_headers(),
            )
        return self._client

    def _default_headers(self) -> dict[str, str]:
        """Generate default headers for requests."""
        headers = {
            "Accept": "application/json",
            "Content-Type": "application/json",
            "User-Agent": "swifttrack-python/0.1.0",
        }
        if self.config.token:
            headers["token"] = self.config.token
        return headers

    def update_token(self, token: str) -> None:
        """Update the authentication token."""
        self.config = self.config.with_token(token)
        if self._client and not self._client.is_closed:
            self._client.headers["token"] = token
        logger.debug("Updated authentication token")

    def request(
        self,
        method: str,
        path: str,
        json_data: dict[str, Any] | None = None,
        params: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ) -> dict[str, Any]:
        """Make an HTTP request with retry logic."""
        url = f"{self.config.base_url}{path}"
        request_headers = {**self._default_headers(), **(headers or {})}

        def _make_request() -> httpx.Response:
            logger.debug(f"{method} {url}")
            response = self.client.request(
                method=method,
                url=url,
                json=json_data,
                params=params,
                headers=request_headers,
            )
            return response

        try:
            response = self.retry_handler.execute(_make_request)
            return self._handle_response(response)
        except httpx.TimeoutException as e:
            logger.error(f"Request timed out: {e}")
            raise TimeoutError(
                f"Request to {url} timed out after {self.config.timeout}s",
                timeout_seconds=self.config.timeout,
            ) from e
        except httpx.RequestError as e:
            logger.error(f"Request error: {e}")
            raise SwiftTrackError(f"Request failed: {e}") from e

    def _handle_response(self, response: httpx.Response) -> dict[str, Any]:
        """Handle API response and raise appropriate exceptions."""
        try:
            data = response.json() if response.content else {}
        except ValueError:
            data = {"raw_response": response.text}

        if 200 <= response.status_code < 300:
            return data

        self._raise_for_status(response, data)
        return {}

    def _raise_for_status(self, response: httpx.Response, data: dict[str, Any]) -> None:
        """Raise appropriate exception based on status code."""
        status_code = response.status_code
        message = data.get("message", f"HTTP {status_code}")

        if status_code == 400:
            raise ValidationError(
                message=message,
                errors=data.get("errors"),
                response_data=data,
            )
        elif status_code == 401:
            raise AuthenticationError(message=message, response_data=data)
        elif status_code == 403:
            raise PermissionError(message=message, response_data=data)
        elif status_code == 404:
            raise NotFoundError(message=message, response_data=data)
        elif status_code == 409:
            raise ConflictError(message=message, response_data=data)
        elif status_code == 429:
            retry_after = response.headers.get("Retry-After")
            raise RateLimitError(
                message=message,
                retry_after=int(retry_after) if retry_after else None,
                response_data=data,
            )
        elif 500 <= status_code < 600:
            raise ServerError(
                message=message,
                status_code=status_code,
                response_data=data,
            )
        else:
            raise APIError(
                message=message,
                status_code=status_code,
                response_data=data,
            )

    def get(
        self,
        path: str,
        params: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ) -> dict[str, Any]:
        """Make a GET request."""
        return self.request("GET", path, params=params, headers=headers)

    def post(
        self,
        path: str,
        json_data: dict[str, Any] | None = None,
        params: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ) -> dict[str, Any]:
        """Make a POST request."""
        return self.request("POST", path, json_data=json_data, params=params, headers=headers)

    def put(
        self,
        path: str,
        json_data: dict[str, Any] | None = None,
        params: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ) -> dict[str, Any]:
        """Make a PUT request."""
        return self.request("PUT", path, json_data=json_data, params=params, headers=headers)

    def delete(
        self,
        path: str,
        params: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ) -> dict[str, Any]:
        """Make a DELETE request."""
        return self.request("DELETE", path, params=params, headers=headers)

    def close(self) -> None:
        """Close the HTTP client."""
        if self._client and not self._client.is_closed:
            self._client.close()
            logger.debug("HTTP client closed")

    def __enter__(self) -> HTTPClient:
        """Enter context manager."""
        return self

    def __exit__(self, exc_type: Any, exc_val: Any, exc_tb: Any) -> None:
        """Exit context manager."""
        self.close()
