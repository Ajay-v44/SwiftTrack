package com.swifttrack.dto.adminDto;

import java.util.List;
import java.util.UUID;

public record AssignRolesRequest(
        UUID userId,
        List<UUID> roleIds) {
}
