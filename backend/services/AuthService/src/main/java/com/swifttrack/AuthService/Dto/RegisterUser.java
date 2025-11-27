package com.swifttrack.AuthService.Dto;

public record RegisterUser(
    String name,
    String password,
    String email,
    String mobile
) {
}