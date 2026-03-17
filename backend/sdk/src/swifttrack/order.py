"""Order service for SwiftTrack SDK."""

from __future__ import annotations

import logging
from typing import TYPE_CHECKING
from uuid import UUID

from swifttrack.models.order import (
    CreateOrderRequest,
    DeliveryOptionsQuoteResponse,
    GuestQuoteRequest,
    Order,
    OrderQuoteRequest,
    OrderQuoteResponse,
)

if TYPE_CHECKING:
    from swifttrack.utils.http_client import HTTPClient

logger = logging.getLogger(__name__)


class OrderService:
    """Service for order management operations."""

    BASE_PATH = "/api/order/v1"

    def __init__(self, http_client: HTTPClient) -> None:
        self._client = http_client

    def get_quote(
        self,
        pickup_address_id: UUID | str,
        dropoff_lat: float,
        dropoff_lng: float,
    ) -> OrderQuoteResponse:
        """Get delivery quote for authenticated user.

        Args:
            pickup_address_id: UUID of pickup address.
            dropoff_lat: Dropoff location latitude.
            dropoff_lng: Dropoff location longitude.

        Returns:
            OrderQuoteResponse containing available quotes.

        Raises:
            ValidationError: If coordinates are invalid.
            AuthenticationError: If user is not authenticated.
        """
        pickup_uuid = (
            UUID(pickup_address_id) if isinstance(pickup_address_id, str) else pickup_address_id
        )
        request = OrderQuoteRequest(
            pickup_address_id=pickup_uuid,
            dropoff_lat=dropoff_lat,
            dropoff_lng=dropoff_lng,
        )
        logger.debug(f"Getting quote for pickup: {pickup_uuid}")

        response = self._client.post(
            f"{self.BASE_PATH}/getQuote",
            json_data=request.model_dump(by_alias=True),
        )
        quote_response = OrderQuoteResponse.model_validate(response)

        logger.info(f"Retrieved {len(quote_response.quotes)} quote options")
        return quote_response

    def get_guest_quote(
        self,
        pickup_lat: float,
        pickup_lng: float,
        pickup_address: str,
        dropoff_lat: float,
        dropoff_lng: float,
        dropoff_address: str,
        package_weight_kg: float,
    ) -> DeliveryOptionsQuoteResponse:
        """Get delivery quote for guest (no authentication required).

        Args:
            pickup_lat: Pickup latitude.
            pickup_lng: Pickup longitude.
            pickup_address: Pickup address string.
            dropoff_lat: Dropoff latitude.
            dropoff_lng: Dropoff longitude.
            dropoff_address: Dropoff address string.
            package_weight_kg: Package weight in kg.

        Returns:
            DeliveryOptionsQuoteResponse containing available quotes.
        """
        request = GuestQuoteRequest(
            pickup_lat=pickup_lat,
            pickup_lng=pickup_lng,
            pickup_address=pickup_address,
            dropoff_lat=dropoff_lat,
            dropoff_lng=dropoff_lng,
            dropoff_address=dropoff_address,
            package_weight_kg=package_weight_kg,
        )
        logger.debug("Getting guest quote")

        response = self._client.post(
            f"{self.BASE_PATH}/guest/getQuote",
            json_data=request.model_dump(by_alias=True),
        )
        quote_response = DeliveryOptionsQuoteResponse.model_validate(response)

        logger.info(f"Retrieved {len(quote_response.options)} guest quote options")
        return quote_response

    def create_order(
        self,
        request: CreateOrderRequest,
        quote_session_id: UUID | str,
    ) -> Order:
        """Create an order.

        Args:
            request: CreateOrderRequest with order details.
            quote_session_id: Quote session ID from get_quote.

        Returns:
            Created Order object.

        Raises:
            ValidationError: If order data is invalid.
            AuthenticationError: If user is not authenticated.
        """
        quote_session_uuid = (
            UUID(quote_session_id) if isinstance(quote_session_id, str) else quote_session_id
        )
        logger.debug(f"Creating order with quote session: {quote_session_uuid}")

        response = self._client.post(
            f"{self.BASE_PATH}/createOrder",
            params={"quoteSessionId": str(quote_session_uuid)},
            json_data=request.model_dump(by_alias=True, exclude_none=True),
        )
        order = Order.model_validate(response)

        logger.info(f"Created order: {order.id}")
        return order

    def cancel_order(self, order_id: UUID | str, reason: str | None = None) -> dict:
        """Cancel an order.

        Args:
            order_id: UUID of the order to cancel.
            reason: Optional cancellation reason.

        Returns:
            Response message.

        Raises:
            NotFoundError: If order doesn't exist.
            ValidationError: If order cannot be cancelled.
            AuthenticationError: If user is not authenticated.
        """
        order_uuid = UUID(order_id) if isinstance(order_id, str) else order_id
        logger.debug(f"Cancelling order: {order_uuid}")

        params: dict = {"orderId": str(order_uuid)}
        if reason:
            params["reason"] = reason

        response = self._client.post(f"{self.BASE_PATH}/cancelOrder", params=params)

        logger.info(f"Cancelled order: {order_uuid}")
        return response

    def get_order_status(self, order_id: UUID | str) -> str:
        """Get the status of an order.

        Args:
            order_id: UUID of the order.

        Returns:
            Order status string.

        Raises:
            NotFoundError: If order doesn't exist.
            AuthenticationError: If user is not authenticated.
        """
        order_uuid = UUID(order_id) if isinstance(order_id, str) else order_id
        logger.debug(f"Getting order status: {order_uuid}")

        response = self._client.get(f"{self.BASE_PATH}/getOrderStatus/{order_uuid}")

        return response if isinstance(response, str) else response.get("status", "")

    def get_order(self, order_id: UUID | str) -> Order:
        """Get order details by ID.

        Args:
            order_id: UUID of the order.

        Returns:
            Order object.

        Raises:
            NotFoundError: If order doesn't exist.
            AuthenticationError: If user is not authenticated.
        """
        order_uuid = UUID(order_id) if isinstance(order_id, str) else order_id
        logger.debug(f"Getting order details: {order_uuid}")

        response = self._client.get(f"{self.BASE_PATH}/getOrderById/{order_uuid}")
        order = Order.model_validate(response)

        logger.info(f"Retrieved order: {order_uuid}")
        return order
