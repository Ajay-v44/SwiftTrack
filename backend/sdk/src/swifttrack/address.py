"""Address service for SwiftTrack SDK."""

from __future__ import annotations

import logging
from typing import TYPE_CHECKING, Any
from uuid import UUID

from swifttrack.models.address import Address, AddressRequest

if TYPE_CHECKING:
    from swifttrack.utils.http_client import HTTPClient

logger = logging.getLogger(__name__)


class AddressService:
    """Service for address management operations."""

    BASE_PATH = "/api/order/addresses/v1"

    def __init__(self, http_client: HTTPClient) -> None:
        self._client = http_client

    def list_addresses(self) -> list[Address]:
        """Get all saved addresses for the authenticated user.

        Returns:
            List of Address objects.

        Raises:
            AuthenticationError: If user is not authenticated.
        """
        logger.debug("Fetching all addresses")

        response = self._client.get(self.BASE_PATH)
        addresses = [Address.model_validate(item) for item in response]

        logger.info(f"Retrieved {len(addresses)} addresses")
        return addresses

    def get_address(self, address_id: UUID | str) -> Address:
        """Get a specific address by ID.

        Args:
            address_id: UUID of the address.

        Returns:
            Address object.

        Raises:
            NotFoundError: If address doesn't exist.
            AuthenticationError: If user is not authenticated.
        """
        address_uuid = UUID(address_id) if isinstance(address_id, str) else address_id
        logger.debug(f"Fetching address: {address_uuid}")

        response = self._client.get(f"{self.BASE_PATH}/{address_uuid}")
        address = Address.model_validate(response)

        logger.info(f"Retrieved address: {address_uuid}")
        return address

    def get_default_address(self) -> Address:
        """Get the default address for the authenticated user.

        Returns:
            Default Address object.

        Raises:
            NotFoundError: If no default address exists.
            AuthenticationError: If user is not authenticated.
        """
        logger.debug("Fetching default address")

        response = self._client.get(f"{self.BASE_PATH}/default")
        address = Address.model_validate(response)

        logger.info(f"Retrieved default address: {address.id}")
        return address

    def create_address(self, address: AddressRequest) -> Address:
        """Create a new address.

        Args:
            address: AddressRequest containing address details.

        Returns:
            Created Address object.

        Raises:
            ValidationError: If address data is invalid.
            AuthenticationError: If user is not authenticated.
        """
        logger.debug("Creating new address")

        response = self._client.post(
            self.BASE_PATH,
            json_data=address.model_dump(by_alias=True, exclude_none=True),
        )
        created_address = Address.model_validate(response)

        logger.info(f"Created address: {created_address.id}")
        return created_address

    def update_address(self, address_id: UUID | str, address: AddressRequest) -> Address:
        """Update an existing address.

        Args:
            address_id: UUID of the address to update.
            address: AddressRequest containing updated details.

        Returns:
            Updated Address object.

        Raises:
            NotFoundError: If address doesn't exist.
            ValidationError: If address data is invalid.
            AuthenticationError: If user is not authenticated.
        """
        address_uuid = UUID(address_id) if isinstance(address_id, str) else address_id
        logger.debug(f"Updating address: {address_uuid}")

        response = self._client.put(
            f"{self.BASE_PATH}/{address_uuid}",
            json_data=address.model_dump(by_alias=True, exclude_none=True),
        )
        updated_address = Address.model_validate(response)

        logger.info(f"Updated address: {address_uuid}")
        return updated_address

    def set_default(self, address_id: UUID | str) -> Address:
        """Set an address as the default.

        Args:
            address_id: UUID of the address to set as default.

        Returns:
            Updated Address object.

        Raises:
            NotFoundError: If address doesn't exist.
            AuthenticationError: If user is not authenticated.
        """
        address_uuid = UUID(address_id) if isinstance(address_id, str) else address_id
        logger.debug(f"Setting default address: {address_uuid}")

        response = self._client.post(f"{self.BASE_PATH}/{address_uuid}/default")
        updated_address = Address.model_validate(response)

        logger.info(f"Set default address: {address_uuid}")
        return updated_address

    def delete_address(self, address_id: UUID | str) -> dict[str, Any]:
        """Delete an address.

        Args:
            address_id: UUID of the address to delete.

        Returns:
            Response message.

        Raises:
            NotFoundError: If address doesn't exist.
            AuthenticationError: If user is not authenticated.
        """
        address_uuid = UUID(address_id) if isinstance(address_id, str) else address_id
        logger.debug(f"Deleting address: {address_uuid}")

        response = self._client.delete(f"{self.BASE_PATH}/{address_uuid}")

        logger.info(f"Deleted address: {address_uuid}")
        return response if isinstance(response, dict) else {"message": str(response)}
