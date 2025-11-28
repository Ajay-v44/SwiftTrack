package com.swifttrack.AuthService.Dto;

import java.util.Optional;
import java.util.UUID;

import com.swifttrack.AuthService.Models.Enum.UserType;

public record TokenResponse(
                UUID id,
                Optional<UUID> tenantId,
                Optional<UUID> providerId,
                Optional<UserType> userType,
                String name,
                String mobile) {

}
