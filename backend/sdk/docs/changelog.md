# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2024-XX-XX

### Added

- Initial release of SwiftTrack Python SDK
- **Authentication**
  - Email/password login (`client.login()`)
  - Token-based authentication (`client.set_token()`)
  - Token validation and user details retrieval
- **Address Management**
  - List, get, create, update, delete addresses
  - Set default address
  - Geocoding support (latitude/longitude)
- **Order Management**
  - Get delivery quotes (authenticated and guest)
  - Create orders with idempotency support
  - Cancel orders
  - Track order status
  - Get order details
- **Account Management**
  - Get account details
  - View transaction history
  - Create accounts (different types)
  - Balance reconciliation
  - Admin wallet top-up
- **Features**
  - Automatic retry with exponential backoff
  - Type-safe Pydantic models
  - Comprehensive error handling
  - Context manager support
  - Environment variable configuration
  - Full type hints (mypy compatible)
  - Complete test coverage
- **Documentation**
  - MkDocs documentation site
  - API reference documentation
  - Usage examples and guides
  - Contributing guidelines
- **CI/CD**
  - GitHub Actions CI pipeline
  - Automated testing (Python 3.9-3.13)
  - Linting (ruff, black, mypy)
  - PyPI publishing via OIDC

[Unreleased]: https://github.com/swifttrack/swifttrack-python/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/swifttrack/swifttrack-python/releases/tag/v0.1.0
