# API Error Handling Guide

## Problem

The original error you received:
```json
{
  "timestamp": "2025-12-03T17:23:18.002+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/tenant/api/users/v1/login/mobileAndOtp"
}
```

This is a **generic 500 error** that doesn't tell you what's actually wrong. The real issue is hidden because the error from the Auth Service wasn't being properly extracted and displayed.

## Root Cause Analysis

### The Actual Issue

When you sent:
```json
{
  "mobileNum": "8921057654",
  "otp": "12345"
}
```

The `otp` field is a **plain string**, but the Auth Service expects:
```java
Optional<String> otp
```

Jackson (the JSON parser) couldn't deserialize a string into an `Optional<String>` directly, causing a JSON parsing error.

### Why You Didn't See This Error

The error handling wasn't configured to:
1. Catch JSON parsing exceptions
2. Extract error details from Feign client responses
3. Display meaningful error messages to the client

## Solution: Improved Error Handling

Two new `GlobalExceptionHandler` classes have been added:

1. **AuthService/conf/GlobalExceptionHandler.java** - Catches errors in Auth Service
2. **TenantService/conf/GlobalExceptionHandler.java** - Catches errors in Tenant Service

These handlers catch and properly format errors so you see meaningful messages like:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "JSON parsing error: Cannot deserialize value of type `java.util.Optional<java.lang.String>` from String value",
  "path": "/api/users/v1/login/mobileNumAndOtp"
}
```

## Correct API Usage

### Mobile Login with OTP Endpoint

**URL:** `POST http://localhost:8003/api/tenant/api/users/v1/login/mobileAndOtp`

**Expected Request Format:**
```json
{
  "mobileNum": "8921057654",
  "otp": "12345"
}
```

**DTO Definition (AuthService.Dto.MobileNumAuth):**
```java
public record MobileNumAuth(
    String mobileNum,
    Optional<String> otp
) {}
```

### Important Notes

- The `mobileNum` field expects a **String**
- The `otp` field is a **Java Optional<String>**, but you should send it as a plain string (not an object)
- Jackson automatically wraps plain string values into Optional when deserializing

## When Errors Occur Going Forward

Instead of getting a generic "Internal Server Error", you'll now see:

1. **JSON Parsing Errors** → Clear message about what field format is wrong
2. **Business Logic Errors** → Specific message (e.g., "Invalid OTP", "Account not activated")
3. **Not Found Errors** → "User account doesn't exist"
4. **Authorization Errors** → Specific authentication/permission issues

## Example Error Responses

### Bad Request (Wrong Field Type)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "JSON parsing error: Cannot deserialize value...",
  "path": "/api/users/v1/login/mobileNumAndOtp"
}
```

### Invalid Credentials
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid OTP",
  "path": "/api/users/v1/login/mobileNumAndOtp"
}
```

### Account Not Activated
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "User account is not verified",
  "path": "/api/users/v1/login/mobileNumAndOtp"
}
```

### User Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User account doesn't exist",
  "path": "/api/users/v1/login/mobileNumAndOtp"
}
```
