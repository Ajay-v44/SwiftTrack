package com.swifttrack.dto;

import java.util.UUID;

import com.swifttrack.enums.UserType;

public record ListOfTenantUsers(
        UUID id,
        String name,
        String mobile,
        String email,
        Boolean status,
        UserType userType) {

}
