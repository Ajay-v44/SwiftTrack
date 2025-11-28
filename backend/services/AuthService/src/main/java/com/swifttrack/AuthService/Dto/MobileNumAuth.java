package com.swifttrack.AuthService.Dto;

import java.util.Optional;

public record MobileNumAuth(
        String mobileNum,
        Optional<String> otp

)
{}
