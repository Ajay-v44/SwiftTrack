package com.swifttrack.dto;

public record TenantSetupStatusResponse(
        boolean companyRegistered,
        boolean providersConfigured,
        boolean deliveryPreferencesConfigured,
        boolean setupComplete,
        String nextStep) {
}
