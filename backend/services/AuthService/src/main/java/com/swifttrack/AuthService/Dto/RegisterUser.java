package com.swifttrack.AuthService.Dto;

import com.swifttrack.AuthService.Models.Enum.UserType;

public record RegisterUser(
    String name,
    String password,
    String email,
    String mobile,
    UserType userType
) {
}