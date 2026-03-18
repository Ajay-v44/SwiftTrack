"""Retry logic for HTTP requests."""

from __future__ import annotations

import logging
import random
import time
from typing import Callable, TypeVar

import httpx

from swifttrack.config import SwiftTrackConfig

logger = logging.getLogger(__name__)

T = TypeVar("T")


class RetryHandler:
    """Handle retry logic for HTTP requests."""

    RETRYABLE_STATUS_CODES = {408, 429, 500, 502, 503, 504}
    RETRYABLE_EXCEPTIONS = (
        httpx.ConnectError,
        httpx.ConnectTimeout,
        httpx.ReadTimeout,
        httpx.WriteTimeout,
        httpx.PoolTimeout,
        httpx.NetworkError,
    )

    def __init__(self, config: SwiftTrackConfig) -> None:
        self.config = config

    def execute(self, operation: Callable[[], T]) -> T:
        """Execute an operation with retry logic.

        Args:
            operation: The operation to execute (should return httpx.Response)

        Returns:
            The result of the operation

        Raises:
            httpx.HTTPStatusError: If all retries fail
            httpx.RequestError: For request errors
        """
        last_exception: Exception | None = None
        delay = self.config.retry_delay

        for attempt in range(self.config.max_retries + 1):
            try:
                result = operation()
                # For responses
                if isinstance(result, httpx.Response):
                    # If it's a response with retryable status and we have retries left
                    if self._should_retry(result) and attempt < self.config.max_retries:
                        logger.warning(
                            f"Retryable status {result.status_code}, "
                            f"attempt {attempt + 1}/{self.config.max_retries + 1}, "
                            f"retrying in {delay:.2f}s"
                        )
                        time.sleep(delay)
                        delay = min(
                            delay * self.config.retry_backoff,
                            self.config.retry_max_delay,
                        )
                        delay = self._add_jitter(delay)
                        continue

                    # Return result for non-retryable errors or exhausted retries so custom error handling works
                    return result

            except self.RETRYABLE_EXCEPTIONS as e:
                last_exception = e
                if attempt < self.config.max_retries:
                    logger.warning(
                        f"Request error: {e}, "
                        f"attempt {attempt + 1}/{self.config.max_retries + 1}, "
                        f"retrying in {delay:.2f}s"
                    )
                    time.sleep(delay)
                    delay = min(
                        delay * self.config.retry_backoff,
                        self.config.retry_max_delay,
                    )
                    delay = self._add_jitter(delay)
                else:
                    logger.error(f"All {self.config.max_retries + 1} attempts failed")
                    raise

            except httpx.HTTPStatusError as e:
                # Don't retry client errors (4xx) except specific ones
                if e.response.status_code in self.RETRYABLE_STATUS_CODES:
                    last_exception = e
                    if attempt < self.config.max_retries:
                        logger.warning(
                            f"HTTP {e.response.status_code}, "
                            f"attempt {attempt + 1}/{self.config.max_retries + 1}, "
                            f"retrying in {delay:.2f}s"
                        )
                        time.sleep(delay)
                        delay = min(
                            delay * self.config.retry_backoff,
                            self.config.retry_max_delay,
                        )
                        delay = self._add_jitter(delay)
                        continue
                raise

        # If we get here, all retries failed
        if last_exception:
            raise last_exception
        raise RuntimeError("Unexpected retry loop exit")

    def _should_retry(self, response: httpx.Response) -> bool:
        """Determine if a response should be retried."""
        return response.status_code in self.RETRYABLE_STATUS_CODES

    def _add_jitter(self, delay: float) -> float:
        """Add random jitter to prevent thundering herd."""
        # Add up to 20% random jitter
        jitter = delay * 0.2 * random.random()
        return delay + jitter
