package com.swifttrack.AuthService.Dto;

import com.swifttrack.enums.UserType;
import com.swifttrack.enums.VerificationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TenantUserListItemResponse(
        UUID id,
        String name,
        String mobile,
        String email,
        Boolean status,
        VerificationStatus verificationStatus,
        UserType userType,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
