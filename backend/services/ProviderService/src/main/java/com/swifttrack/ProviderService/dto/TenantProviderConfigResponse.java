package com.swifttrack.ProviderService.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TenantProviderConfigResponse(
        UUID id,
        String providerName,
        String providerCode,
        String description,
        String logoUrl,
        String websiteUrl,
        boolean supportsHyperlocal,
        boolean supportsCourier,
        boolean supportsSameDay,
        boolean supportsIntercity,
        List<String> servicableAreas,
        boolean enabled,
        boolean verified,
        String disabledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
