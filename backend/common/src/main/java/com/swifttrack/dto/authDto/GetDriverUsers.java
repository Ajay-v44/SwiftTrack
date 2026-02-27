package com.swifttrack.dto.authDto;

import java.util.UUID;

public record GetDriverUsers(
        UUID id,
        String name,
        String email,
        String mobile) {

}
