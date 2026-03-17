"""Order models for SwiftTrack SDK."""

from __future__ import annotations

from datetime import datetime
from enum import Enum
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


class OrderType(str, Enum):
    """Type of order."""

    IMMEDIATE = "IMMEDIATE"
    SCHEDULED = "SCHEDULED"
    RECURRING = "RECURRING"


class PaymentType(str, Enum):
    """Payment method type."""

    COD = "COD"
    PREPAID = "PREPAID"
    WALLET = "WALLET"


class OrderStatus(str, Enum):
    """Order status."""

    PENDING = "PENDING"
    QUOTED = "QUOTED"
    CONFIRMED = "CONFIRMED"
    ASSIGNED = "ASSIGNED"
    PICKED_UP = "PICKED_UP"
    IN_TRANSIT = "IN_TRANSIT"
    DELIVERED = "DELIVERED"
    CANCELLED = "CANCELLED"
    FAILED = "FAILED"


class LocationPoint(BaseModel):
    """Geographic location point."""

    latitude: float = Field(..., ge=-90, le=90, description="Latitude")
    longitude: float = Field(..., ge=-180, le=180, description="Longitude")
    address: str | None = Field(None, description="Address string")


class Item(BaseModel):
    """Order item details."""

    name: str = Field(..., description="Item name")
    quantity: int = Field(default=1, ge=1, description="Item quantity")
    weight_kg: float | None = Field(None, alias="weightKg", ge=0, description="Item weight in kg")
    dimensions: dict[str, float] | None = Field(
        None, description="Dimensions {length, width, height} in cm"
    )
    value: float | None = Field(None, ge=0, description="Item value")
    description: str | None = Field(None, description="Item description")


class PackageInfo(BaseModel):
    """Package information."""

    weight_kg: float = Field(..., alias="weightKg", ge=0, description="Total weight in kg")
    dimensions: dict[str, float] | None = Field(
        None, description="Package dimensions {length, width, height} in cm"
    )
    fragile: bool = Field(default=False, description="Is fragile")
    temperature_controlled: bool = Field(
        default=False, alias="temperatureControlled", description="Needs temperature control"
    )


class TimeWindows(BaseModel):
    """Delivery time windows."""

    pickup_time: datetime | None = Field(None, alias="pickupTime", description="Pickup time")
    delivery_time: datetime | None = Field(
        None, alias="deliveryTime", description="Preferred delivery time"
    )
    flexible_pickup: bool = Field(
        default=False, alias="flexiblePickup", description="Pickup time is flexible"
    )
    flexible_delivery: bool = Field(
        default=False, alias="flexibleDelivery", description="Delivery time is flexible"
    )


class DeliveryPreferences(BaseModel):
    """Customer delivery preferences."""

    contact_before_delivery: bool = Field(
        default=True, alias="contactBeforeDelivery", description="Call before delivery"
    )
    leave_at_door: bool = Field(default=False, alias="leaveAtDoor", description="Can leave at door")
    signature_required: bool = Field(
        default=True, alias="signatureRequired", description="Signature required"
    )
    handling_instructions: str | None = Field(
        None, alias="handlingInstructions", description="Special handling instructions"
    )


class ExternalMetadata(BaseModel):
    """External system metadata."""

    source: str | None = Field(None, description="Source system")
    reference_id: str | None = Field(None, alias="referenceId", description="External reference")
    tags: list[str] = Field(default_factory=list, description="Tags")


class OrderQuoteRequest(BaseModel):
    """Request model for getting a delivery quote."""

    model_config = ConfigDict(populate_by_name=True)

    pickup_address_id: UUID = Field(..., alias="pickupAddressId", description="Pickup address ID")
    dropoff_lat: float = Field(
        ..., alias="dropoffLat", ge=-90, le=90, description="Dropoff latitude"
    )
    dropoff_lng: float = Field(
        ..., alias="dropoffLng", ge=-180, le=180, description="Dropoff longitude"
    )


class QuoteOption(BaseModel):
    """Individual quote option."""

    model_config = ConfigDict(populate_by_name=True)

    quote_id: UUID = Field(..., alias="quoteId", description="Quote option ID")
    provider_id: UUID = Field(..., alias="providerId", description="Service provider ID")
    provider_name: str = Field(..., alias="providerName", description="Provider name")
    service_type: str = Field(..., alias="serviceType", description="Service type")
    estimated_delivery_time: datetime = Field(
        ..., alias="estimatedDeliveryTime", description="Estimated delivery time"
    )
    price: float = Field(..., ge=0, description="Price amount")
    currency: str = Field(default="INR", description="Currency code")
    distance_km: float | None = Field(None, alias="distanceKm", description="Distance in km")


class OrderQuoteResponse(BaseModel):
    """Response model for delivery quote."""

    model_config = ConfigDict(populate_by_name=True)

    quote_session_id: UUID = Field(..., alias="quoteSessionId", description="Quote session ID")
    pickup_address: AddressResponse = Field(
        ..., alias="pickupAddress", description="Pickup address"
    )
    dropoff_location: LocationPoint = Field(
        ..., alias="dropoffLocation", description="Dropoff location"
    )
    quotes: list[QuoteOption] = Field(..., description="Available quotes")
    expires_at: datetime = Field(..., alias="expiresAt", description="Quote expiration time")


class AddressResponse(BaseModel):
    """Address response in quote."""

    model_config = ConfigDict(populate_by_name=True)

    id: UUID = Field(..., description="Address ID")
    label: str | None = Field(None, description="Address label")
    line1: str = Field(..., description="Address line 1")
    line2: str | None = Field(None, description="Address line 2")
    city: str = Field(..., description="City")
    state: str = Field(..., description="State")
    pincode: str = Field(..., description="Pincode")
    latitude: float | None = Field(None, description="Latitude")
    longitude: float | None = Field(None, description="Longitude")
    contact_name: str | None = Field(None, alias="contactName", description="Contact name")
    contact_phone: str | None = Field(None, alias="contactPhone", description="Contact phone")


class CreateOrderRequest(BaseModel):
    """Request model for creating an order."""

    model_config = ConfigDict(populate_by_name=True)

    idempotency_key: str = Field(
        ..., alias="idempotencyKey", description="Unique key for idempotency"
    )
    tenant_id: str | None = Field(None, alias="tenantId", description="Tenant ID")
    quote_id: str | None = Field(None, alias="quoteId", description="Selected quote ID")
    order_reference: str | None = Field(None, alias="orderReference", description="Your order ref")
    order_type: OrderType = Field(default=OrderType.IMMEDIATE, alias="orderType")
    payment_type: PaymentType = Field(default=PaymentType.PREPAID, alias="paymentType")
    pickup_address_id: UUID = Field(..., alias="pickupAddressId", description="Pickup address ID")
    dropoff: LocationPoint = Field(..., description="Dropoff location")
    items: list[Item] = Field(default_factory=list, description="Items to deliver")
    package_info: PackageInfo | None = Field(None, alias="packageInfo", description="Package info")
    time_windows: TimeWindows | None = Field(None, alias="timeWindows", description="Time windows")
    delivery_preferences: DeliveryPreferences | None = Field(
        None, alias="deliveryPreferences", description="Preferences"
    )
    external_metadata: ExternalMetadata | None = Field(
        None, alias="externalMetadata", description="External metadata"
    )
    delivery_instructions: str | None = Field(
        None, alias="deliveryInstructions", description="Instructions"
    )


class Order(BaseModel):
    """Order model."""

    model_config = ConfigDict(populate_by_name=True)

    id: UUID = Field(..., description="Order ID")
    order_number: str = Field(..., alias="orderNumber", description="Order number")
    status: OrderStatus = Field(..., description="Order status")
    pickup_address_id: UUID = Field(..., alias="pickupAddressId", description="Pickup address ID")
    dropoff: LocationPoint = Field(..., description="Dropoff location")
    provider_id: UUID | None = Field(None, alias="providerId", description="Assigned provider")
    driver_id: UUID | None = Field(None, alias="driverId", description="Assigned driver")
    items: list[Item] = Field(default_factory=list, description="Items")
    package_info: PackageInfo | None = Field(None, alias="packageInfo", description="Package info")
    price: float | None = Field(None, ge=0, description="Final price")
    currency: str = Field(default="INR", description="Currency")
    payment_type: PaymentType = Field(..., alias="paymentType", description="Payment type")
    estimated_delivery: datetime | None = Field(
        None, alias="estimatedDelivery", description="Estimated delivery time"
    )
    actual_pickup: datetime | None = Field(None, alias="actualPickup", description="Actual pickup")
    actual_delivery: datetime | None = Field(
        None, alias="actualDelivery", description="Actual delivery"
    )
    created_at: datetime = Field(..., alias="createdAt", description="Creation time")
    updated_at: datetime = Field(..., alias="updatedAt", description="Last update time")
    tracking_url: str | None = Field(None, alias="trackingUrl", description="Tracking URL")


class CancelOrderRequest(BaseModel):
    """Request model for cancelling an order."""

    order_id: UUID = Field(..., alias="orderId", description="Order ID to cancel")
    reason: str | None = Field(None, description="Cancellation reason")


class GuestQuoteRequest(BaseModel):
    """Request model for guest quote (no auth required)."""

    pickup_lat: float = Field(..., alias="pickupLat", ge=-90, le=90)
    pickup_lng: float = Field(..., alias="pickupLng", ge=-180, le=180)
    pickup_address: str = Field(..., alias="pickupAddress")
    dropoff_lat: float = Field(..., alias="dropoffLat", ge=-90, le=90)
    dropoff_lng: float = Field(..., alias="dropoffLng", ge=-180, le=180)
    dropoff_address: str = Field(..., alias="dropoffAddress")
    package_weight_kg: float = Field(..., alias="packageWeightKg", ge=0)


class DeliveryOptionsQuoteResponse(BaseModel):
    """Response model for guest/consumer quotes."""

    model_config = ConfigDict(populate_by_name=True)

    quote_session_id: UUID = Field(..., alias="quoteSessionId")
    options: list[QuoteOption] = Field(..., description="Delivery options")
    expires_at: datetime = Field(..., alias="expiresAt")
    guest_access_token: str | None = Field(None, alias="guestAccessToken")
