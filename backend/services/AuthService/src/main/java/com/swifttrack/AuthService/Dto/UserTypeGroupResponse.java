package com.swifttrack.AuthService.Dto;

import java.util.List;

public record UserTypeGroupResponse(
        String code,
        String displayName,
        String description,
        List<UserTypeOptionResponse> userTypes) {
}
