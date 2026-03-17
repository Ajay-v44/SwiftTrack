"""Tests for SwiftTrack address service."""

from __future__ import annotations

import uuid

import pytest
import respx
from httpx import Response

from swifttrack import NotFoundError, SwiftTrackClient
from swifttrack.models.address import Address, AddressRequest


class TestAddressService:
    """Test address operations."""

    @pytest.fixture
    def client(self) -> SwiftTrackClient:
        """Create authenticated client for tests."""
        return SwiftTrackClient(token="test-token")

    @respx.mock
    def test_list_addresses(self, client: SwiftTrackClient) -> None:
        """Test listing all addresses."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.get(
            "https://backend-swifttrack.ajayv.online/api/order/addresses/v1"
        ).mock(
            return_value=Response(
                200,
                json=[
                    {
                        "id": str(address_id),
                        "label": "Home",
                        "line1": "123 Main St",
                        "city": "Mumbai",
                        "state": "Maharashtra",
                        "country": "India",
                        "pincode": "400001",
                        "latitude": 19.0760,
                        "longitude": 72.8777,
                        "isDefault": True,
                    }
                ],
            )
        )

        # Act
        addresses = client.addresses.list_addresses()

        # Assert
        assert len(addresses) == 1
        assert isinstance(addresses[0], Address)
        assert addresses[0].label == "Home"
        assert addresses[0].is_default is True
        assert route.called

    @respx.mock
    def test_get_address(self, client: SwiftTrackClient) -> None:
        """Test getting a specific address."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.get(
            f"https://backend-swifttrack.ajayv.online/api/order/addresses/v1/{address_id}"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(address_id),
                    "label": "Office",
                    "line1": "456 Business Park",
                    "line2": "Floor 5",
                    "city": "Bangalore",
                    "state": "Karnataka",
                    "country": "India",
                    "pincode": "560001",
                    "contactName": "John Doe",
                    "contactPhone": "+91-9876543210",
                    "isDefault": False,
                },
            )
        )

        # Act
        address = client.addresses.get_address(address_id)

        # Assert
        assert isinstance(address, Address)
        assert address.id == address_id
        assert address.label == "Office"
        assert address.line2 == "Floor 5"
        assert route.called

    @respx.mock
    def test_get_address_not_found(self, client: SwiftTrackClient) -> None:
        """Test getting non-existent address."""
        # Arrange
        address_id = uuid.uuid4()
        respx.get(
            f"https://backend-swifttrack.ajayv.online/api/order/addresses/v1/{address_id}"
        ).mock(
            return_value=Response(
                404,
                json={"message": "Address not found"},
            )
        )

        # Act & Assert
        with pytest.raises(NotFoundError) as exc_info:
            client.addresses.get_address(address_id)

        assert exc_info.value.status_code == 404

    @respx.mock
    def test_get_default_address(self, client: SwiftTrackClient) -> None:
        """Test getting default address."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.get(
            "https://backend-swifttrack.ajayv.online/api/order/addresses/v1/default"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(address_id),
                    "label": "Home",
                    "line1": "123 Main St",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "pincode": "400001",
                    "isDefault": True,
                },
            )
        )

        # Act
        address = client.addresses.get_default_address()

        # Assert
        assert isinstance(address, Address)
        assert address.is_default is True
        assert route.called

    @respx.mock
    def test_create_address(self, client: SwiftTrackClient) -> None:
        """Test creating a new address."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.post(
            "https://backend-swifttrack.ajayv.online/api/order/addresses/v1"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(address_id),
                    "label": "Warehouse",
                    "line1": "789 Industrial Area",
                    "city": "Delhi",
                    "state": "Delhi",
                    "country": "India",
                    "pincode": "110001",
                    "latitude": 28.6139,
                    "longitude": 77.2090,
                    "isDefault": False,
                },
            )
        )

        request = AddressRequest(
            label="Warehouse",
            line1="789 Industrial Area",
            city="Delhi",
            state="Delhi",
            pincode="110001",
            latitude=28.6139,
            longitude=77.2090,
        )

        # Act
        address = client.addresses.create_address(request)

        # Assert
        assert isinstance(address, Address)
        assert address.id == address_id
        assert address.label == "Warehouse"
        assert route.called

        # Verify request body
        request_body = route.calls.last.request.content
        assert b"Warehouse" in request_body
        assert b"789 Industrial Area" in request_body

    @respx.mock
    def test_update_address(self, client: SwiftTrackClient) -> None:
        """Test updating an address."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.put(
            f"https://backend-swifttrack.ajayv.online/api/order/addresses/v1/{address_id}"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(address_id),
                    "label": "Updated Office",
                    "line1": "456 Business Park",
                    "city": "Bangalore",
                    "state": "Karnataka",
                    "pincode": "560001",
                    "isDefault": False,
                },
            )
        )

        request = AddressRequest(
            label="Updated Office",
            line1="456 Business Park",
            city="Bangalore",
            state="Karnataka",
            pincode="560001",
        )

        # Act
        address = client.addresses.update_address(address_id, request)

        # Assert
        assert isinstance(address, Address)
        assert address.label == "Updated Office"
        assert route.called

    @respx.mock
    def test_set_default_address(self, client: SwiftTrackClient) -> None:
        """Test setting an address as default."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.post(
            f"https://backend-swifttrack.ajayv.online/api/order/addresses/v1/{address_id}/default"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(address_id),
                    "label": "Office",
                    "line1": "456 Business Park",
                    "city": "Bangalore",
                    "state": "Karnataka",
                    "pincode": "560001",
                    "isDefault": True,
                },
            )
        )

        # Act
        address = client.addresses.set_default(address_id)

        # Assert
        assert isinstance(address, Address)
        assert address.is_default is True
        assert route.called

    @respx.mock
    def test_delete_address(self, client: SwiftTrackClient) -> None:
        """Test deleting an address."""
        # Arrange
        address_id = uuid.uuid4()
        route = respx.delete(
            f"https://backend-swifttrack.ajayv.online/api/order/addresses/v1/{address_id}"
        ).mock(
            return_value=Response(
                200,
                json={"message": "Address deleted successfully"},
            )
        )

        # Act
        response = client.addresses.delete_address(address_id)

        # Assert
        assert response["message"] == "Address deleted successfully"
        assert route.called

    @respx.mock
    def test_get_address_with_string_id(self, client: SwiftTrackClient) -> None:
        """Test getting address with string ID."""
        # Arrange
        address_id = uuid.uuid4()
        respx.get(
            f"https://backend-swifttrack.ajayv.online/api/order/addresses/v1/{address_id}"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(address_id),
                    "line1": "Test Address",
                    "city": "Test City",
                    "state": "Test State",
                    "pincode": "123456",
                },
            )
        )

        # Act
        address = client.addresses.get_address(str(address_id))

        # Assert
        assert address.id == address_id
