package com.swifttrack.AuthService.Dto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.swifttrack.enums.UserType;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
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
