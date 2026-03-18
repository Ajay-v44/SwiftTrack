"""SwiftTrack SDK exceptions."""

from typing import Any, Optional


class SwiftTrackError(Exception):
    """Base exception for all SwiftTrack SDK errors."""

    def __init__(
        self,
        message: str,
        status_code: Optional[int] = None,
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.response_data = response_data or {}

    def __str__(self) -> str:
        if self.status_code:
            return f"[{self.status_code}] {self.message}"
        return self.message


class AuthenticationError(SwiftTrackError):
    """Raised when authentication fails or token is invalid."""

    def __init__(
        self,
        message: str = "Authentication failed",
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=401, response_data=response_data)


class RateLimitError(SwiftTrackError):
    """Raised when API rate limit is exceeded."""

    def __init__(
        self,
        message: str = "Rate limit exceeded",
        retry_after: Optional[int] = None,
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=429, response_data=response_data)
        self.retry_after = retry_after


class ValidationError(SwiftTrackError):
    """Raised when request validation fails."""

    def __init__(
        self,
        message: str = "Validation error",
        errors: Optional[dict[str, list[str]]] = None,
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=400, response_data=response_data)
        self.errors = errors or {}


class NotFoundError(SwiftTrackError):
    """Raised when a requested resource is not found."""

    def __init__(
        self,
        message: str = "Resource not found",
        status_code: Optional[int] = None,
        resource_type: Optional[str] = None,
        resource_id: Optional[str] = None,
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=status_code or 404, response_data=response_data)
        self.resource_type = resource_type
        self.resource_id = resource_id


class APIError(SwiftTrackError):
    """Raised for general API errors."""

    pass


class ServerError(SwiftTrackError):
    """Raised when server returns 5xx error."""

    def __init__(
        self,
        message: str = "Server error",
        status_code: int = 500,
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=status_code, response_data=response_data)


class TimeoutError(SwiftTrackError):
    """Raised when request times out."""

    def __init__(
        self,
        message: str = "Request timed out",
        timeout_seconds: Optional[float] = None,
    ) -> None:
        super().__init__(message, status_code=None)
        self.timeout_seconds = timeout_seconds


class ConflictError(SwiftTrackError):
    """Raised when there's a conflict (e.g., duplicate resource)."""

    def __init__(
        self,
        message: str = "Conflict error",
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=409, response_data=response_data)


class PermissionError(SwiftTrackError):
    """Raised when user doesn't have permission for the operation."""

    def __init__(
        self,
        message: str = "Permission denied",
        response_data: Optional[dict[str, Any]] = None,
    ) -> None:
        super().__init__(message, status_code=403, response_data=response_data)
