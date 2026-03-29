package com.swifttrack.AuthService.Dto;

import com.swifttrack.enums.UserType;

public record UserTypeOptionResponse(
        UserType userType,
        String displayName,
        String description) {
}
