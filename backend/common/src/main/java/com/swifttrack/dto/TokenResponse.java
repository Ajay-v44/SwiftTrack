package com.swifttrack.dto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.swifttrack.enums.UserType;

public record TokenResponse(
        UUID id,
        Optional<UUID> tenantId,
        Optional<UUID> providerId,
        Optional<UserType> userType,
        String name,
        String mobile,
        List<String> roles

) {

}
