package com.swifttrack.AuthService.Dto;

import java.util.List;

public record PaginatedTenantUsersResponse(
        List<TenantUserListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<UserTypeGroupResponse> userTypeGroups) {
}
