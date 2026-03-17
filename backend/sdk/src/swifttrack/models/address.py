"""Address models for SwiftTrack SDK."""

from __future__ import annotations

from uuid import UUID

from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class Address(BaseModel):
    """Address model representing a saved address."""

    model_config = ConfigDict(populate_by_name=True)

    id: UUID = Field(..., description="Unique address ID")
    label: Optional[str] = Field(None, description="Address label (e.g., 'Home', 'Office')")
    line1: str = Field(..., description="Address line 1")
    line2: Optional[str] = Field(None, description="Address line 2")
    city: str = Field(..., description="City name")
    state: str = Field(..., description="State/Province")
    country: str = Field(default="India", description="Country")
    pincode: str = Field(..., description="Postal/ZIP code")
    locality: Optional[str] = Field(None, description="Locality/Area")
    latitude: Optional[float] = Field(None, description="Latitude coordinate")
    longitude: Optional[float] = Field(None, description="Longitude coordinate")
    contact_name: Optional[str] = Field(None, alias="contactName", description="Contact person name")
    contact_phone: Optional[str] = Field(
        None, alias="contactPhone", description="Contact phone number"
    )
    business_name: Optional[str] = Field(
        None, alias="businessName", description="Business/Company name"
    )
    notes: Optional[str] = Field(None, description="Additional notes")
    is_default: bool = Field(
        default=False, alias="isDefault", description="Whether this is the default address"
    )


class AddressRequest(BaseModel):
    """Request model for creating/updating an address."""

    model_config = ConfigDict(populate_by_name=True)

    label: Optional[str] = Field(None, description="Address label")
    line1: str = Field(..., description="Address line 1")
    line2: Optional[str] = Field(None, description="Address line 2")
    city: str = Field(..., description="City name")
    state: str = Field(..., description="State/Province")
    country: str = Field(default="India", description="Country")
    pincode: str = Field(..., description="Postal/ZIP code")
    locality: Optional[str] = Field(None, description="Locality/Area")
    latitude: Optional[float] = Field(None, ge=-90, le=90, description="Latitude")
    longitude: Optional[float] = Field(None, ge=-180, le=180, description="Longitude")
    contact_name: Optional[str] = Field(None, alias="contactName", description="Contact name")
    contact_phone: Optional[str] = Field(None, alias="contactPhone", description="Contact phone")
    business_name: Optional[str] = Field(None, alias="businessName", description="Business name")
    notes: Optional[str] = Field(None, description="Additional notes")
    is_default: bool = Field(default=False, alias="isDefault", description="Set as default address")


class Coordinates(BaseModel):
    """Geographic coordinates."""

    latitude: float = Field(..., ge=-90, le=90, description="Latitude")
    longitude: float = Field(..., ge=-180, le=180, description="Longitude")
