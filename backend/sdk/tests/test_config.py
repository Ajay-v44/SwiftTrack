"""Tests for SwiftTrack SDK configuration."""

from __future__ import annotations

import os
from unittest import mock

import pytest

from swifttrack import SwiftTrackClient
from swifttrack.config import SwiftTrackConfig
from swifttrack.exceptions import AuthenticationError, NotFoundError


class TestConfig:
    """Test configuration."""

    def test_default_config(self) -> None:
        """Test default configuration values."""
        config = SwiftTrackConfig()

        assert config.base_url == "https://backend-swifttrack.ajayv.online"
        assert config.token is None
        assert config.timeout == 30.0
        assert config.max_retries == 3
        assert config.retry_delay == 1.0
        assert config.retry_backoff == 2.0
        assert config.verify_ssl is True

    def test_custom_config(self) -> None:
        """Test custom configuration values."""
        config = SwiftTrackConfig(
            base_url="https://custom.example.com",
            token="test-token",
            timeout=60.0,
            max_retries=5,
        )

        assert config.base_url == "https://custom.example.com"
        assert config.token == "test-token"
        assert config.timeout == 60.0
        assert config.max_retries == 5

    def test_config_trailing_slash_removal(self) -> None:
        """Test that trailing slashes are removed from base_url."""
        config = SwiftTrackConfig(base_url="https://example.com/")
        assert config.base_url == "https://example.com"

    def test_config_from_env(self) -> None:
        """Test loading config from environment variables."""
        env_vars = {
            "SWIFTTRACK_BASE_URL": "https://env.example.com",
            "SWIFTTRACK_TOKEN": "env-token",
            "SWIFTTRACK_TIMEOUT": "45.0",
            "SWIFTTRACK_MAX_RETRIES": "2",
            "SWIFTTRACK_VERIFY_SSL": "false",
        }

        with mock.patch.dict(os.environ, env_vars, clear=True):
            config = SwiftTrackConfig.from_env()

        assert config.base_url == "https://env.example.com"
        assert config.token == "env-token"
        assert config.timeout == 45.0
        assert config.max_retries == 2
        assert config.verify_ssl is False

    def test_config_from_env_defaults(self) -> None:
        """Test loading config from environment with defaults."""
        with mock.patch.dict(os.environ, {}, clear=True):
            config = SwiftTrackConfig.from_env()

        assert config.base_url == "https://backend-swifttrack.ajayv.online"
        assert config.token is None

    def test_config_validation(self) -> None:
        """Test configuration validation."""
        with pytest.raises(ValueError, match="base_url is required"):
            SwiftTrackConfig(base_url="")

        with pytest.raises(ValueError, match="timeout must be positive"):
            SwiftTrackConfig(timeout=0)

        with pytest.raises(ValueError, match="max_retries must be non-negative"):
            SwiftTrackConfig(max_retries=-1)

        with pytest.raises(ValueError, match="retry_delay must be positive"):
            SwiftTrackConfig(retry_delay=0)

        with pytest.raises(ValueError, match="retry_backoff must be >= 1"):
            SwiftTrackConfig(retry_backoff=0.5)

    def test_config_with_token(self) -> None:
        """Test creating new config with token."""
        config = SwiftTrackConfig(base_url="https://example.com")
        new_config = config.with_token("new-token")

        assert new_config.token == "new-token"
        assert new_config.base_url == config.base_url  # Other values unchanged

    def test_config_to_dict(self) -> None:
        """Test converting config to dictionary."""
        config = SwiftTrackConfig(token="secret-token")
        config_dict = config.to_dict()

        assert config_dict["base_url"] == "https://backend-swifttrack.ajayv.online"
        assert config_dict["token"] == "secret-token"
        assert config_dict["timeout"] == 30.0


class TestClient:
    """Test main client functionality."""

    def test_client_initialization(self) -> None:
        """Test client initialization."""
        client = SwiftTrackClient()

        assert not client.is_authenticated
        assert client.config.base_url == "https://backend-swifttrack.ajayv.online"

    def test_client_with_token(self) -> None:
        """Test client with initial token."""
        client = SwiftTrackClient(token="test-token")

        assert client.is_authenticated

    def test_client_with_config(self) -> None:
        """Test client with custom config."""
        config = SwiftTrackConfig(
            base_url="https://custom.example.com",
            token="config-token",
            timeout=60.0,
        )
        client = SwiftTrackClient(config=config, token="config-token")

        assert client.is_authenticated
        assert client.config.base_url == "https://custom.example.com"
        assert client.config.timeout == 60.0

    def test_client_method_chaining(self) -> None:
        """Test client method chaining."""
        client = SwiftTrackClient()
        result = client.set_token("token")

        assert result is client
        assert client.is_authenticated

    def test_exception_str(self) -> None:
        """Test exception string representation."""
        error = NotFoundError("Resource not found", status_code=404)
        assert str(error) == "[404] Resource not found"

        error2 = NotFoundError("No status")
        assert str(error2) == "[404] No status"

    def test_authentication_error(self) -> None:
        """Test authentication error."""
        error = AuthenticationError("Invalid credentials")
        assert error.status_code == 401
        assert str(error) == "[401] Invalid credentials"
