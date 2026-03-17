"""Authentication service for SwiftTrack SDK."""

from __future__ import annotations

import logging
from typing import TYPE_CHECKING

from swifttrack.models.auth import LoginRequest, LoginResponse, UserDetails

if TYPE_CHECKING:
    from swifttrack.utils.http_client import HTTPClient

logger = logging.getLogger(__name__)


class AuthService:
    """Service for authentication operations."""

    BASE_PATH = "/api/tenant/api/users/v1"

    def __init__(self, http_client: HTTPClient) -> None:
        self._client = http_client

    def login(self, email: str, password: str) -> LoginResponse:
        """Authenticate with email and password.

        Args:
            email: User email address.
            password: User password.

        Returns:
            LoginResponse containing the access token.

        Raises:
            AuthenticationError: If credentials are invalid.
            ValidationError: If request validation fails.
        """
        request = LoginRequest(email=email, password=password)
        logger.debug(f"Authenticating user: {email}")

        response = self._client.post(
            f"{self.BASE_PATH}/login/emailAndPassword",
            json_data=request.model_dump(by_alias=True),
        )

        login_response = LoginResponse.model_validate(response)
        logger.info(f"Successfully authenticated user: {email}")
        return login_response

    def get_user_details(self, token: str) -> UserDetails:
        """Get user details from token.

        Args:
            token: JWT token to validate.

        Returns:
            UserDetails containing user information.

        Raises:
            AuthenticationError: If token is invalid.
        """
        logger.debug("Getting user details from token")

        response = self._client.post(
            f"{self.BASE_PATH}/getUserDetails",
            params={"token": token},
        )

        return UserDetails.model_validate(response)

    def update_auth_token(self, token: str) -> None:
        """Update the authentication token for subsequent requests.

        Args:
            token: New authentication token.
        """
        self._client.update_token(token)
        logger.debug("Updated authentication token")
