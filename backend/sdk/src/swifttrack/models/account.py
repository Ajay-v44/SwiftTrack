"""Account models for SwiftTrack SDK."""

from __future__ import annotations

from datetime import datetime
from decimal import Decimal
from enum import Enum
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


class AccountType(str, Enum):
    """Type of account."""

    CONSUMER = "CONSUMER"
    PROVIDER = "PROVIDER"
    DRIVER = "DRIVER"
    TENANT = "TENANT"
    PLATFORM = "PLATFORM"


class TransactionType(str, Enum):
    """Type of ledger transaction."""

    CREDIT = "CREDIT"
    DEBIT = "DEBIT"


class TransactionStatus(str, Enum):
    """Status of a transaction."""

    PENDING = "PENDING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    REVERSED = "REVERSED"


class Account(BaseModel):
    """Account model representing a user's wallet/account."""

    model_config = ConfigDict(populate_by_name=True)

    id: UUID = Field(..., description="Account ID")
    user_id: UUID = Field(..., alias="userId", description="User ID")
    account_type: AccountType = Field(..., alias="accountType", description="Account type")
    balance: Decimal = Field(..., description="Current balance")
    currency: str = Field(default="INR", description="Currency code")
    is_active: bool = Field(default=True, alias="isActive", description="Whether account is active")
    created_at: datetime = Field(..., alias="createdAt", description="Creation time")
    updated_at: datetime = Field(..., alias="updatedAt", description="Last update time")


class LedgerTransaction(BaseModel):
    """Ledger transaction model."""

    model_config = ConfigDict(populate_by_name=True)

    id: UUID = Field(..., description="Transaction ID")
    account_id: UUID = Field(..., alias="accountId", description="Account ID")
    transaction_type: TransactionType = Field(
        ..., alias="transactionType", description="CREDIT or DEBIT"
    )
    amount: Decimal = Field(..., gt=0, description="Transaction amount")
    currency: str = Field(default="INR", description="Currency code")
    status: TransactionStatus = Field(..., description="Transaction status")
    description: Optional[str] = Field(None, description="Transaction description")
    reference_id: Optional[str] = Field(None, alias="referenceId", description="External reference")
    order_id: Optional[UUID] = Field(None, alias="orderId", description="Related order ID")
    metadata: dict[str, str] = Field(default_factory=dict, description="Additional metadata")
    created_at: datetime = Field(..., alias="createdAt", description="Transaction time")
    settled_at: Optional[datetime] = Field(None, alias="settledAt", description="Settlement time")


class GetTransactionsRequest(BaseModel):
    """Request model for getting transactions."""

    account_id: UUID = Field(..., alias="accountId", description="Account ID")
    start_date: Optional[datetime] = Field(None, alias="startDate", description="Filter start date")
    end_date: Optional[datetime] = Field(None, alias="endDate", description="Filter end date")
    limit: int = Field(default=50, ge=1, le=100, description="Number of results")
    offset: int = Field(default=0, ge=0, description="Pagination offset")


class TopUpRequest(BaseModel):
    """Request model for wallet top-up."""

    user_id: UUID = Field(..., alias="userId", description="User ID")
    amount: Decimal = Field(..., gt=0, description="Top-up amount")
    currency: str = Field(default="INR", description="Currency")
    reference: Optional[str] = Field(None, description="Reference/payment ID")


class CreateAccountRequest(BaseModel):
    """Request model for creating an account."""

    user_id: UUID = Field(..., alias="userId", description="User ID")
    account_type: AccountType = Field(..., alias="accountType", description="Account type")
