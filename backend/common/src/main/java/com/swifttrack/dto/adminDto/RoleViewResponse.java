package com.swifttrack.dto.adminDto;

import java.util.UUID;

public record RoleViewResponse(
        UUID id,
        String name,
        String description,
        Boolean status) {
}
