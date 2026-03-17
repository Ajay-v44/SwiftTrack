"""Authentication models for SwiftTrack SDK."""

from __future__ import annotations

from pydantic import BaseModel, EmailStr, Field


class LoginRequest(BaseModel):
    """Request model for email/password login."""

    email: EmailStr = Field(..., description="User email address")
    password: str = Field(..., description="User password", min_length=1)


class LoginResponse(BaseModel):
    """Response model for successful login."""

    token_type: str = Field(..., description="Type of token (e.g., Bearer)")
    access_token: str = Field(..., description="JWT access token")

    @property
    def token(self) -> str:
        """Get the full authorization token."""
        return f"{self.token_type} {self.access_token}"


class MobileLoginRequest(BaseModel):
    """Request model for mobile/OTP login."""

    mobile_number: str = Field(..., description="Mobile phone number", min_length=10)
    otp: str = Field(..., description="One-time password", min_length=4, max_length=8)


class TokenValidationRequest(BaseModel):
    """Request model for token validation."""

    token: str = Field(..., description="JWT token to validate")


class UserDetails(BaseModel):
    """User details from token validation."""

    id: str = Field(..., description="User ID")
    email: str | None = Field(None, description="User email")
    name: str | None = Field(None, description="User name")
    tenant_id: str | None = Field(None, description="Tenant ID")
    roles: list[str] = Field(default_factory=list, description="User roles")
    is_active: bool = Field(True, description="Whether the user is active")


class RegisterUserRequest(BaseModel):
    """Request model for user registration."""

    email: EmailStr = Field(..., description="User email address")
    password: str = Field(..., description="User password", min_length=8)
    mobile: str | None = Field(None, description="Mobile phone number")
    name: str | None = Field(None, description="User full name")
