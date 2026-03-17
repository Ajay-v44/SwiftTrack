"""Tests for SwiftTrack authentication service."""

from __future__ import annotations

import pytest
import respx
from httpx import Response

from swifttrack import AuthenticationError, SwiftTrackClient, ValidationError
from swifttrack.models.auth import LoginResponse


class TestAuthService:
    """Test authentication operations."""

    @respx.mock
    def test_login_success(self) -> None:
        """Test successful login."""
        # Arrange
        route = respx.post(
            "https://backend-swifttrack.ajayv.online/api/tenant/api/users/v1/login/emailAndPassword"
        ).mock(
            return_value=Response(
                200,
                json={
                    "tokenType": "Bearer",
                    "accessToken": "test-jwt-token-12345",
                },
            )
        )

        client = SwiftTrackClient()

        # Act
        response = client.login("user@example.com", "password123")

        # Assert
        assert isinstance(response, LoginResponse)
        assert response.token_type == "Bearer"
        assert response.access_token == "test-jwt-token-12345"
        assert client.is_authenticated
        assert route.called

    @respx.mock
    def test_login_invalid_credentials(self) -> None:
        """Test login with invalid credentials."""
        # Arrange
        respx.post(
            "https://backend-swifttrack.ajayv.online/api/tenant/api/users/v1/login/emailAndPassword"
        ).mock(
            return_value=Response(
                401,
                json={"message": "Invalid email or password"},
            )
        )

        client = SwiftTrackClient()

        # Act & Assert
        with pytest.raises(AuthenticationError) as exc_info:
            client.login("user@example.com", "wrongpassword")

        assert exc_info.value.status_code == 401
        assert "Invalid" in exc_info.value.message
        assert not client.is_authenticated

    @respx.mock
    def test_login_validation_error(self) -> None:
        """Test login with invalid email format."""
        # Arrange
        respx.post(
            "https://backend-swifttrack.ajayv.online/api/tenant/api/users/v1/login/emailAndPassword"
        ).mock(
            return_value=Response(
                400,
                json={
                    "message": "Validation failed",
                    "errors": {"email": ["Invalid email format"]},
                },
            )
        )

        client = SwiftTrackClient()

        # Act & Assert
        with pytest.raises(ValidationError) as exc_info:
            client.login("invalid-email", "password")

        assert exc_info.value.status_code == 400
        assert "email" in exc_info.value.errors

    @respx.mock
    def test_set_token(self) -> None:
        """Test manually setting token."""
        # Arrange
        client = SwiftTrackClient()

        # Act
        result = client.set_token("manual-token-123")

        # Assert
        assert client.is_authenticated
        assert result is client  # Test chaining

    @respx.mock
    def test_logout(self) -> None:
        """Test logout clears token."""
        # Arrange
        client = SwiftTrackClient()
        client.set_token("test-token")

        # Act
        client.logout()

        # Assert
        assert not client.is_authenticated

    @respx.mock
    def test_get_user_details(self) -> None:
        """Test getting user details from token."""
        # Arrange
        respx.post(
            "https://backend-swifttrack.ajayv.online/api/tenant/api/users/v1/getUserDetails"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": "user-123",
                    "email": "user@example.com",
                    "name": "Test User",
                    "tenantId": "tenant-456",
                    "roles": ["CONSUMER"],
                    "isActive": True,
                },
            )
        )

        client = SwiftTrackClient()
        client.set_token("valid-token")

        # Act
        user = client.auth.get_user_details("valid-token")

        # Assert
        assert user.id == "user-123"
        assert user.email == "user@example.com"
        assert user.name == "Test User"

    @respx.mock
    def test_get_user_details_invalid_token(self) -> None:
        """Test getting user details with invalid token."""
        # Arrange
        respx.post(
            "https://backend-swifttrack.ajayv.online/api/tenant/api/users/v1/getUserDetails"
        ).mock(
            return_value=Response(
                401,
                json={"message": "Invalid token"},
            )
        )

        client = SwiftTrackClient()

        # Act & Assert
        with pytest.raises(AuthenticationError):
            client.auth.get_user_details("invalid-token")

    def test_client_with_initial_token(self) -> None:
        """Test client initialized with token."""
        # Act
        client = SwiftTrackClient(token="initial-token")

        # Assert
        assert client.is_authenticated

    def test_client_context_manager(self) -> None:
        """Test client as context manager."""
        with SwiftTrackClient() as client:
            assert not client.is_authenticated
        # After exiting context, client should be closed

    @respx.mock
    def test_temp_token_context(self) -> None:
        """Test temporary token context manager."""
        # Arrange
        client = SwiftTrackClient()
        client.set_token("original-token")

        # Act
        with client.temp_token("temp-token"):
            # In this context, temp-token should be active
            assert client.is_authenticated

        # Assert - original token restored
        assert client.is_authenticated
