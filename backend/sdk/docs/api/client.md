# API Reference - Client

## SwiftTrackClient

The main client class for interacting with the SwiftTrack API.

::: swifttrack.client.SwiftTrackClient
    handler: python
    options:
      show_root_heading: true
      show_source: true

## SwiftTrackConfig

Configuration class for the SDK.

::: swifttrack.config.SwiftTrackConfig
    handler: python
    options:
      show_root_heading: true
      show_source: true

## Service Classes

### AuthService

Authentication operations.

::: swifttrack.auth.AuthService
    handler: python
    options:
      show_root_heading: true

### AddressService

Address management operations.

::: swifttrack.address.AddressService
    handler: python
    options:
      show_root_heading: true

### OrderService

Order management operations.

::: swifttrack.order.OrderService
    handler: python
    options:
      show_root_heading: true

### AccountService

Account and wallet operations.

::: swifttrack.account.AccountService
    handler: python
    options:
      show_root_heading: true

## HTTP Client

Internal HTTP client with retry logic.

::: swifttrack.utils.http_client.HTTPClient
    handler: python
    options:
      show_root_heading: true

::: swifttrack.utils.retry.RetryHandler
    handler: python
    options:
      show_root_heading: true
