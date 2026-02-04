package com.swifttrack.dto.driverDto;

public record RespondToAssignmentDto(
        Long assignmentId,
        boolean accept,
        String reason) {
}
