"""Tests for SwiftTrack SDK exceptions."""

from __future__ import annotations

import pytest

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


class TestExceptions:
    """Test exception classes."""

    def test_base_exception(self) -> None:
        """Test base SwiftTrackError."""
        error = SwiftTrackError("Base error")
        assert error.message == "Base error"
        assert error.status_code is None
        assert error.response_data == {}
        assert str(error) == "Base error"

    def test_base_exception_with_status(self) -> None:
        """Test base exception with status code."""
        error = SwiftTrackError("Error with status", status_code=500)
        assert error.status_code == 500
        assert str(error) == "[500] Error with status"

    def test_authentication_error(self) -> None:
        """Test AuthenticationError."""
        error = AuthenticationError("Invalid token")
        assert error.status_code == 401
        assert str(error) == "[401] Invalid token"

    def test_authentication_error_default_message(self) -> None:
        """Test AuthenticationError default message."""
        error = AuthenticationError()
        assert error.message == "Authentication failed"
        assert error.status_code == 401

    def test_rate_limit_error(self) -> None:
        """Test RateLimitError."""
        error = RateLimitError("Too many requests", retry_after=60)
        assert error.status_code == 429
        assert error.retry_after == 60
        assert str(error) == "[429] Too many requests"

    def test_rate_limit_error_without_retry(self) -> None:
        """Test RateLimitError without retry_after."""
        error = RateLimitError()
        assert error.retry_after is None
        assert error.message == "Rate limit exceeded"

    def test_validation_error(self) -> None:
        """Test ValidationError."""
        errors = {"email": ["Invalid format"], "password": ["Too short"]}
        error = ValidationError("Validation failed", errors=errors)
        assert error.status_code == 400
        assert error.errors == errors

    def test_validation_error_default(self) -> None:
        """Test ValidationError defaults."""
        error = ValidationError()
        assert error.errors == {}
        assert error.message == "Validation error"

    def test_not_found_error(self) -> None:
        """Test NotFoundError."""
        error = NotFoundError(
            "User not found",
            resource_type="User",
            resource_id="123",
        )
        assert error.status_code == 404
        assert error.resource_type == "User"
        assert error.resource_id == "123"
        assert str(error) == "[404] User not found"

    def test_server_error(self) -> None:
        """Test ServerError."""
        error = ServerError("Internal server error", status_code=500)
        assert error.status_code == 500
        assert str(error) == "[500] Internal server error"

    def test_server_error_custom_status(self) -> None:
        """Test ServerError with different status."""
        error = ServerError("Bad gateway", status_code=502)
        assert error.status_code == 502

    def test_timeout_error(self) -> None:
        """Test TimeoutError."""
        error = TimeoutError("Request timed out", timeout_seconds=30.0)
        assert error.timeout_seconds == 30.0
        assert error.status_code is None

    def test_timeout_error_default(self) -> None:
        """Test TimeoutError default."""
        error = TimeoutError()
        assert error.message == "Request timed out"

    def test_conflict_error(self) -> None:
        """Test ConflictError."""
        error = ConflictError("Email already exists")
        assert error.status_code == 409
        assert str(error) == "[409] Email already exists"

    def test_permission_error(self) -> None:
        """Test PermissionError."""
        error = PermissionError("Access denied")
        assert error.status_code == 403
        assert str(error) == "[403] Access denied"

    def test_api_error(self) -> None:
        """Test APIError."""
        error = APIError("Generic API error", status_code=418)
        assert error.status_code == 418

    def test_response_data(self) -> None:
        """Test exception with response data."""
        response_data = {"detail": "More info", "code": "ERROR_001"}
        error = SwiftTrackError("Error", status_code=400, response_data=response_data)
        assert error.response_data == response_data
