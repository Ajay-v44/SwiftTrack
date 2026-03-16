package com.swifttrack.dto.adminDto;

import java.util.UUID;

import com.swifttrack.enums.UserType;
import com.swifttrack.enums.VerificationStatus;

public record ManagedUserResponse(
        UUID id,
        UUID tenantId,
        String name,
        String email,
        String mobile,
        UserType userType,
        Boolean status,
        VerificationStatus verificationStatus) {
}
