"""Tests for SwiftTrack account service."""

from __future__ import annotations

import uuid
from datetime import datetime, timezone
from decimal import Decimal

import pytest
import respx
from httpx import Response

from swifttrack import NotFoundError, PermissionError, SwiftTrackClient
from swifttrack.models.account import Account, AccountType, LedgerTransaction


class TestAccountService:
    """Test account/wallet operations."""

    @pytest.fixture
    def client(self) -> SwiftTrackClient:
        """Create authenticated client for tests."""
        return SwiftTrackClient(token="test-token")

    @respx.mock
    def test_get_my_account(self, client: SwiftTrackClient) -> None:
        """Test getting user's account."""
        # Arrange
        user_id = uuid.uuid4()
        account_id = uuid.uuid4()

        route = respx.get(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/getMyAccount"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(account_id),
                    "userId": str(user_id),
                    "accountType": "CONSUMER",
                    "balance": "1500.50",
                    "currency": "INR",
                    "isActive": True,
                    "createdAt": datetime.now(timezone.utc).isoformat(),
                    "updatedAt": datetime.now(timezone.utc).isoformat(),
                },
            )
        )

        # Act
        account = client.accounts.get_my_account(user_id)

        # Assert
        assert isinstance(account, Account)
        assert account.id == account_id
        assert account.user_id == user_id
        assert account.account_type == AccountType.CONSUMER
        assert account.balance == Decimal("1500.50")
        assert account.currency == "INR"
        assert route.called

    @respx.mock
    def test_get_transactions(self, client: SwiftTrackClient) -> None:
        """Test getting account transactions."""
        # Arrange
        account_id = uuid.uuid4()
        transaction_id = uuid.uuid4()

        route = respx.get(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/getTransactions"
        ).mock(
            return_value=Response(
                200,
                json=[
                    {
                        "id": str(transaction_id),
                        "accountId": str(account_id),
                        "transactionType": "CREDIT",
                        "amount": "500.00",
                        "currency": "INR",
                        "status": "COMPLETED",
                        "description": "Wallet top-up",
                        "createdAt": datetime.now(timezone.utc).isoformat(),
                    },
                    {
                        "id": str(uuid.uuid4()),
                        "accountId": str(account_id),
                        "transactionType": "DEBIT",
                        "amount": "200.00",
                        "currency": "INR",
                        "status": "COMPLETED",
                        "description": "Order payment",
                        "referenceId": "ORD-2024-001",
                        "createdAt": datetime.now(timezone.utc).isoformat(),
                    },
                ],
            )
        )

        # Act
        transactions = client.accounts.get_transactions(account_id, limit=10)

        # Assert
        assert len(transactions) == 2
        assert isinstance(transactions[0], LedgerTransaction)
        assert transactions[0].transaction_type.value == "CREDIT"
        assert transactions[0].amount == Decimal("500.00")
        assert transactions[1].transaction_type.value == "DEBIT"
        assert transactions[1].amount == Decimal("200.00")
        assert route.called

        # Verify query params
        request_url = str(route.calls.last.request.url)
        assert f"accountId={account_id}" in request_url
        assert "limit=10" in request_url

    @respx.mock
    def test_get_transactions_permission_denied(self, client: SwiftTrackClient) -> None:
        """Test getting transactions for unauthorized account."""
        # Arrange
        account_id = uuid.uuid4()

        respx.get(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/getTransactions"
        ).mock(
            return_value=Response(
                403,
                json={"message": "Access denied to this account"},
            )
        )

        # Act & Assert
        with pytest.raises(PermissionError) as exc_info:
            client.accounts.get_transactions(account_id)

        assert exc_info.value.status_code == 403

    @respx.mock
    def test_create_account(self, client: SwiftTrackClient) -> None:
        """Test creating a new account."""
        # Arrange
        user_id = uuid.uuid4()
        account_id = uuid.uuid4()

        route = respx.post(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/createAccount"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(account_id),
                    "userId": str(user_id),
                    "accountType": "PROVIDER",
                    "balance": "0.00",
                    "currency": "INR",
                    "isActive": True,
                    "createdAt": datetime.now(timezone.utc).isoformat(),
                    "updatedAt": datetime.now(timezone.utc).isoformat(),
                },
            )
        )

        # Act
        account = client.accounts.create_account(user_id, AccountType.PROVIDER)

        # Assert
        assert isinstance(account, Account)
        assert account.user_id == user_id
        assert account.account_type == AccountType.PROVIDER
        assert account.balance == Decimal("0.00")
        assert route.called

    @respx.mock
    def test_reconcile_balance(self, client: SwiftTrackClient) -> None:
        """Test reconciling account balance."""
        # Arrange
        account_id = uuid.uuid4()

        route = respx.post(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/reconcile"
        ).mock(
            return_value=Response(
                200,
                json={"message": "Balance is correct"},
            )
        )

        # Act
        result = client.accounts.reconcile_balance(account_id)

        # Assert
        assert result == "Balance is correct"
        assert route.called

    @respx.mock
    def test_reconcile_balance_corrected(self, client: SwiftTrackClient) -> None:
        """Test reconciling when balance was corrected."""
        # Arrange
        account_id = uuid.uuid4()

        respx.post(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/reconcile"
        ).mock(
            return_value=Response(
                200,
                json={"message": "Balance was corrected from ledger transactions"},
            )
        )

        # Act
        result = client.accounts.reconcile_balance(account_id)

        # Assert
        assert "corrected" in result

    @respx.mock
    def test_top_up_wallet(self, client: SwiftTrackClient) -> None:
        """Test wallet top-up (admin only)."""
        # Arrange
        user_id = uuid.uuid4()
        account_id = uuid.uuid4()

        route = respx.post(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/admin/topupWallet"
        ).mock(
            return_value=Response(
                200,
                json={
                    "id": str(account_id),
                    "userId": str(user_id),
                    "accountType": "CONSUMER",
                    "balance": "2500.00",
                    "currency": "INR",
                    "isActive": True,
                    "createdAt": datetime.now(timezone.utc).isoformat(),
                    "updatedAt": datetime.now(timezone.utc).isoformat(),
                },
            )
        )

        # Act
        account = client.accounts.top_up_wallet(user_id, 1000.00, "TXN-12345")

        # Assert
        assert isinstance(account, Account)
        assert account.balance == Decimal("2500.00")
        assert route.called

    @respx.mock
    def test_top_up_wallet_permission_denied(self, client: SwiftTrackClient) -> None:
        """Test wallet top-up without admin permission."""
        # Arrange
        user_id = uuid.uuid4()

        respx.post(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/admin/topupWallet"
        ).mock(
            return_value=Response(
                403,
                json={"message": "Admin access required"},
            )
        )

        # Act & Assert
        with pytest.raises(PermissionError) as exc_info:
            client.accounts.top_up_wallet(user_id, 500.00)

        assert exc_info.value.status_code == 403

    @respx.mock
    def test_get_account_not_found(self, client: SwiftTrackClient) -> None:
        """Test getting non-existent account."""
        # Arrange
        user_id = uuid.uuid4()

        respx.get(
            "https://backend-swifttrack.ajayv.online/api/accounts/v1/getMyAccount"
        ).mock(
            return_value=Response(
                404,
                json={"message": "Account not found"},
            )
        )

        # Act & Assert
        with pytest.raises(NotFoundError) as exc_info:
            client.accounts.get_my_account(user_id)

        assert exc_info.value.status_code == 404
