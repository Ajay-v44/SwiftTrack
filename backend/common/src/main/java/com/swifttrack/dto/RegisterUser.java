package com.swifttrack.dto;

import com.swifttrack.enums.UserType;

public record RegisterUser(
        String name,
        String password,
        String email,
        String mobile,
        UserType userType
) {

}
