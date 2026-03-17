"""Tests for SwiftTrack order service."""

from __future__ import annotations

import uuid
from datetime import datetime, timezone

import pytest
import respx
from httpx import Response

from swifttrack import NotFoundError, SwiftTrackClient, ValidationError
from swifttrack.models.order import (
    CreateOrderRequest,
    LocationPoint,
    Order,
    OrderQuoteResponse,
)


class TestOrderService:
    """Test order operations."""

    @pytest.fixture
    def client(self) -> SwiftTrackClient:
        """Create authenticated client for tests."""
        return SwiftTrackClient(token="test-token")

    @respx.mock
    def test_get_quote(self, client: SwiftTrackClient) -> None:
        """Test getting delivery quote."""
        # Arrange
        pickup_id = uuid.uuid4()
        quote_session_id = uuid.uuid4()
        quote_id = uuid.uuid4()

        route = respx.post("https://backend-swifttrack.ajayv.online/api/order/v1/getQuote").mock(
            return_value=Response(
                200,
                json={
                    "quoteSessionId": str(quote_session_id),
                    "pickupAddress": {
                        "id": str(pickup_id),
                        "line1": "123 Pickup St",
                        "city": "Mumbai",
                        "state": "Maharashtra",
                        "pincode": "400001",
                    },
                    "dropoffLocation": {
                        "latitude": 19.0760,
                        "longitude": 72.8777,
                    },
                    "quotes": [
                        {
                            "quoteId": str(quote_id),
                            "providerId": str(uuid.uuid4()),
                            "providerName": "Fast Delivery",
                            "serviceType": "EXPRESS",
                            "estimatedDeliveryTime": datetime.now(timezone.utc).isoformat(),
                            "price": 150.0,
                            "currency": "INR",
                            "distanceKm": 5.5,
                        }
                    ],
                    "expiresAt": datetime.now(timezone.utc).isoformat(),
                },
            )
        )

        # Act
        quote = client.orders.get_quote(pickup_id, 19.0760, 72.8777)

        # Assert
        assert isinstance(quote, OrderQuoteResponse)
        assert quote.quote_session_id == quote_session_id
        assert len(quote.quotes) == 1
        assert quote.quotes[0].price == 150.0
        assert route.called

    @respx.mock
    def test_get_guest_quote(self, client: SwiftTrackClient) -> None:
        """Test getting guest quote (no auth required)."""
        # Arrange
        quote_session_id = uuid.uuid4()

        route = respx.post(
            "https://backend-swifttrack.ajayv.online/api/order/v1/guest/getQuote"
        ).mock(
            return_value=Response(
                200,
                json={
                    "quoteSessionId": str(quote_session_id),
                    "options": [
                        {
                            "quoteId": str(uuid.uuid4()),
                            "providerId": str(uuid.uuid4()),
                            "providerName": "Express Delivery",
                            "serviceType": "SAME_DAY",
                            "estimatedDeliveryTime": datetime.now(timezone.utc).isoformat(),
                            "price": 200.0,
                            "currency": "INR",
                        }
                    ],
                    "expiresAt": datetime.now(timezone.utc).isoformat(),
                    "guestAccessToken": "guest-token-123",
                },
            )
        )

        # Act
        quote = client.orders.get_guest_quote(
            pickup_lat=19.0760,
            pickup_lng=72.8777,
            pickup_address="123 Pickup St, Mumbai",
            dropoff_lat=19.2183,
            dropoff_lng=72.9781,
            dropoff_address="456 Dropoff St, Mumbai",
            package_weight_kg=2.5,
        )

        # Assert
        assert quote.quote_session_id == quote_session_id
        assert len(quote.options) == 1
        assert quote.options[0].price == 200.0
        assert quote.guest_access_token == "guest-token-123"
        assert route.called

    @respx.mock
    def test_create_order(self, client: SwiftTrackClient) -> None:
        """Test creating an order."""
        # Arrange
        quote_session_id = uuid.uuid4()
        order_id = uuid.uuid4()
        pickup_id = uuid.uuid4()

        route = respx.post("https://backend-swifttrack.ajayv.online/api/order/v1/createOrder").mock(
            return_value=Response(
                200,
                json={
                    "id": str(order_id),
                    "orderNumber": "ORD-2024-001",
                    "status": "CONFIRMED",
                    "pickupAddressId": str(pickup_id),
                    "dropoff": {
                        "latitude": 19.0760,
                        "longitude": 72.8777,
                        "address": "456 Dropoff St",
                    },
                    "price": 150.0,
                    "currency": "INR",
                    "paymentType": "PREPAID",
                    "createdAt": datetime.now(timezone.utc).isoformat(),
                    "updatedAt": datetime.now(timezone.utc).isoformat(),
                    "trackingUrl": f"https://track.swifttrack.io/{order_id}",
                },
            )
        )

        request = CreateOrderRequest(
            idempotency_key="unique-key-123",
            pickup_address_id=pickup_id,
            dropoff=LocationPoint(latitude=19.0760, longitude=72.8777, address="456 Dropoff St"),
        )

        # Act
        order = client.orders.create_order(request, quote_session_id)

        # Assert
        assert isinstance(order, Order)
        assert order.id == order_id
        assert order.order_number == "ORD-2024-001"
        assert order.status.value == "CONFIRMED"
        assert order.tracking_url == f"https://track.swifttrack.io/{order_id}"
        assert route.called

    @respx.mock
    def test_cancel_order(self, client: SwiftTrackClient) -> None:
        """Test cancelling an order."""
        # Arrange
        order_id = uuid.uuid4()

        route = respx.post("https://backend-swifttrack.ajayv.online/api/order/v1/cancelOrder").mock(
            return_value=Response(
                200,
                json={"message": "Order cancelled successfully"},
            )
        )

        # Act
        response = client.orders.cancel_order(order_id, "Customer requested")

        # Assert
        assert response["message"] == "Order cancelled successfully"
        assert route.called

        # Verify query params
        request_url = str(route.calls.last.request.url)
        assert f"orderId={order_id}" in request_url
        assert "reason=Customer+requested" in request_url

    @respx.mock
    def test_get_order_status(self, client: SwiftTrackClient) -> None:
        """Test getting order status."""
        # Arrange
        order_id = uuid.uuid4()

        route = respx.get(
            f"https://backend-swifttrack.ajayv.online/api/order/v1/getOrderStatus/{order_id}"
        ).mock(
            return_value=Response(
                200,
                json="IN_TRANSIT",
            )
        )

        # Act
        status = client.orders.get_order_status(order_id)

        # Assert
        assert status == "IN_TRANSIT"
        assert route.called

    @respx.mock
    def test_get_order(self, client: SwiftTrackClient) -> None:
        """Test getting order details."""
        # Arrange
        order_id = uuid.uuid4()
        pickup_id = uuid.uuid4()

        route = respx.get(
            f"https://backend-swifttrack.ajayv.online/api/order/v1/getOrderById/{order_id}"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(order_id),
                    "orderNumber": "ORD-2024-002",
                    "status": "DELIVERED",
                    "pickupAddressId": str(pickup_id),
                    "dropoff": {
                        "latitude": 19.0760,
                        "longitude": 72.8777,
                    },
                    "price": 200.0,
                    "currency": "INR",
                    "paymentType": "COD",
                    "createdAt": datetime.now(timezone.utc).isoformat(),
                    "updatedAt": datetime.now(timezone.utc).isoformat(),
                },
            )
        )

        # Act
        order = client.orders.get_order(order_id)

        # Assert
        assert isinstance(order, Order)
        assert order.id == order_id
        assert order.status.value == "DELIVERED"
        assert order.price == 200.0
        assert route.called

    @respx.mock
    def test_get_order_not_found(self, client: SwiftTrackClient) -> None:
        """Test getting non-existent order."""
        # Arrange
        order_id = uuid.uuid4()

        respx.get(
            f"https://backend-swifttrack.ajayv.online/api/order/v1/getOrderById/{order_id}"
        ).mock(
            return_value=Response(
                404,
                json={"message": "Order not found"},
            )
        )

        # Act & Assert
        with pytest.raises(NotFoundError) as exc_info:
            client.orders.get_order(order_id)

        assert exc_info.value.status_code == 404

    @respx.mock
    def test_get_quote_validation_error(self, client: SwiftTrackClient) -> None:
        """Test quote request with invalid coordinates."""
        # Arrange
        pickup_id = uuid.uuid4()

        respx.post("https://backend-swifttrack.ajayv.online/api/order/v1/getQuote").mock(
            return_value=Response(
                400,
                json={
                    "message": "Invalid coordinates",
                    "errors": {"dropoffLat": ["Latitude must be between -90 and 90"]},
                },
            )
        )

        # Act & Assert
        with pytest.raises(ValidationError) as exc_info:
            client.orders.get_quote(pickup_id, 200.0, 72.8777)  # Invalid lat

        assert exc_info.value.status_code == 400
