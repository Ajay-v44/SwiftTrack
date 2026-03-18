"""Account service for SwiftTrack SDK."""

from __future__ import annotations

import logging
from typing import TYPE_CHECKING, Any
from uuid import UUID

from swifttrack.models.account import (
    Account,
    AccountType,
    CreateAccountRequest,
    LedgerTransaction,
)

if TYPE_CHECKING:
    from swifttrack.utils.http_client import HTTPClient

logger = logging.getLogger(__name__)


class AccountService:
    """Service for account/wallet operations."""

    BASE_PATH = "/api/accounts/v1"

    def __init__(self, http_client: HTTPClient) -> None:
        self._client = http_client

    def get_my_account(self, user_id: UUID | str) -> Account:
        """Get the authenticated user's account.

        Args:
            user_id: User ID.

        Returns:
            Account object.

        Raises:
            NotFoundError: If account doesn't exist.
            AuthenticationError: If user is not authenticated.
        """
        user_uuid = UUID(user_id) if isinstance(user_id, str) else user_id
        logger.debug(f"Fetching account for user: {user_uuid}")

        response = self._client.get(
            f"{self.BASE_PATH}/getMyAccount",
            params={"userId": str(user_uuid)},
        )
        account = Account.model_validate(response)

        logger.info(f"Retrieved account: {account.id}")
        return account

    def get_transactions(
        self,
        account_id: UUID | str,
        limit: int = 50,
        offset: int = 0,
    ) -> list[LedgerTransaction]:
        """Get transactions for an account.

        Args:
            account_id: Account ID.
            limit: Number of transactions to return (1-100).
            offset: Pagination offset.

        Returns:
            List of LedgerTransaction objects.

        Raises:
            NotFoundError: If account doesn't exist.
            AuthenticationError: If user is not authenticated.
            PermissionError: If user doesn't have access to account.
        """
        account_uuid = UUID(account_id) if isinstance(account_id, str) else account_id
        logger.debug(f"Fetching transactions for account: {account_uuid}")

        response = self._client.get(
            f"{self.BASE_PATH}/getTransactions",
            params={"accountId": str(account_uuid), "limit": limit, "offset": offset},
        )

        transactions = [LedgerTransaction.model_validate(item) for item in response]

        logger.info(f"Retrieved {len(transactions)} transactions")
        return transactions

    def create_account(
        self,
        user_id: UUID | str,
        account_type: AccountType,
    ) -> Account:
        """Create a new account for a user.

        Args:
            user_id: User ID.
            account_type: Type of account to create.

        Returns:
            Created Account object.

        Raises:
            ValidationError: If account already exists.
            AuthenticationError: If user is not authenticated.
            PermissionError: If user doesn't have permission.
        """
        user_uuid = UUID(user_id) if isinstance(user_id, str) else user_id
        request = CreateAccountRequest.model_validate(
            {"userId": user_uuid, "accountType": account_type}
        )
        logger.debug(f"Creating {account_type} account for user: {user_uuid}")

        response = self._client.post(
            f"{self.BASE_PATH}/createAccount",
            json_data=request.model_dump(by_alias=True, exclude_none=True),
        )
        account = Account.model_validate(response)

        logger.info(f"Created account: {account.id}")
        return account

    def reconcile_balance(self, account_id: UUID | str) -> str:
        """Reconcile account balance from ledger transactions.

        Args:
            account_id: Account ID to reconcile.

        Returns:
            Reconciliation status message.

        Raises:
            NotFoundError: If account doesn't exist.
            AuthenticationError: If user is not authenticated.
            PermissionError: If user doesn't have access to account.
        """
        account_uuid = UUID(account_id) if isinstance(account_id, str) else account_id
        logger.debug(f"Reconciling balance for account: {account_uuid}")

        response: Any = self._client.post(
            f"{self.BASE_PATH}/reconcile",
            params={"accountId": str(account_uuid)},
        )

        if isinstance(response, str):
            message = response
        elif isinstance(response, dict):
            message = str(response.get("message", ""))
        else:
            message = str(response)

        logger.info(f"Reconciled account: {account_uuid}")
        return message

    def top_up_wallet(
        self,
        user_id: UUID | str,
        amount: float,
        reference: str | None = None,
    ) -> Account:
        """Top up a user's wallet (admin only).

        Args:
            user_id: User ID.
            amount: Amount to add.
            reference: Payment reference ID.

        Returns:
            Updated Account object.

        Raises:
            PermissionError: If user doesn't have admin permissions.
            NotFoundError: If account doesn't exist.
        """
        user_uuid = UUID(user_id) if isinstance(user_id, str) else user_id
        logger.debug(f"Topping up wallet for user: {user_uuid}, amount: {amount}")

        params: dict[str, Any] = {"userId": str(user_uuid), "amount": str(amount)}
        if reference:
            params["reference"] = reference

        response = self._client.post(f"{self.BASE_PATH}/admin/topupWallet", params=params)
        account = Account.model_validate(response)

        logger.info(f"Topped up wallet for user: {user_uuid}")
        return account
