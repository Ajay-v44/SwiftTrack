package com.swifttrack.AdminService.dto;

import com.swifttrack.enums.UserType;
import com.swifttrack.enums.VerificationStatus;

import java.util.UUID;

/**
 * Request DTO for bulk updating user status and verification.
 */
public record AdminUpdateUserRequest(
        UUID userId,
        Boolean status,
        VerificationStatus verificationStatus,
        String reason
) {}
