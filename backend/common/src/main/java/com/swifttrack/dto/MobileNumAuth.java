package com.swifttrack.dto;

import java.util.Optional;

public record MobileNumAuth(String mobileNum,
        Optional<String> otp) {

}
