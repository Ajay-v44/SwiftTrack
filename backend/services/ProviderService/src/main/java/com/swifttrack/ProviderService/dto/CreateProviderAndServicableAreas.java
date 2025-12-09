package com.swifttrack.ProviderService.dto;

import java.util.List;

public record CreateProviderAndServicableAreas(

        String providerName,
        String description,
        String logoUrl,
        String websiteUrl,
        boolean supportsHyperlocal,
        boolean supportsCourier,
        boolean supportsSameDay,
        boolean supportsIntercity,
        List<CreateServicableAreas> servicableAreas

) {

}
