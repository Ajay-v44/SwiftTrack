package com.swifttrack.dto.authDto;

import java.util.UUID;

import com.swifttrack.enums.VerificationStatus;

public record UpdateUserStatusVerificationRequest(
        UUID userId,
        Boolean status,
        VerificationStatus verificationStatus) {
}
