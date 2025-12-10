package com.swifttrack.ProviderService.dto;


public record ProviderOnBoardingInput(
    String providerName,
    String contactPhone,
    String contactEmail,
    String notes,
    String docLinks,
    String providerWebsite
) {

}
