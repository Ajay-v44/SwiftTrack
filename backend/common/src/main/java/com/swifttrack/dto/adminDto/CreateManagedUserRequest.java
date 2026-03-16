package com.swifttrack.dto.adminDto;

import java.util.UUID;

import com.swifttrack.enums.UserType;

public record CreateManagedUserRequest(
        UUID tenantId,
        String name,
        String password,
        String email,
        String mobile,
        UserType userType,
        Boolean enabled) {
}
