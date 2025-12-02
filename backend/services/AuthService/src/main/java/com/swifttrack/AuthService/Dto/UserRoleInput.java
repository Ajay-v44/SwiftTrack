package com.swifttrack.AuthService.Dto;

import java.util.List;

public record UserRoleInput(
        String userId,
        List<String> roleList
) {

}
