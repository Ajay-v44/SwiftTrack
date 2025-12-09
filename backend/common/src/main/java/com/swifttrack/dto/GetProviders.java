package com.swifttrack.dto;

import java.util.List;
import java.util.UUID;

public record GetProviders(
        UUID id,
        String providerName,
        String description,
        String logoUrl,
        String websiteUrl,
        boolean supportsHyperlocal,
        boolean supportsCourier,
        boolean supportsSameDay,
        boolean supportsIntercity,
        List<String> servicableAreas) {

}
