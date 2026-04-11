package com.swifttrack.services;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.FeignClients.ProviderInterface;
import com.swifttrack.dto.TenantSetupStatusResponse;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.UserType;
import com.swifttrack.exception.CustomException;
import com.swifttrack.repositories.CompanyRepository;
import com.swifttrack.repositories.TenantDeliveryConfigurationRepository;

@Service
public class TenantSetupService {

    private final AuthInterface authInterface;
    private final CompanyRepository companyRepository;
    private final ProviderInterface providerInterface;
    private final TenantDeliveryConfigurationRepository tenantDeliveryConfigurationRepository;

    public TenantSetupService(
            AuthInterface authInterface,
            CompanyRepository companyRepository,
            ProviderInterface providerInterface,
            TenantDeliveryConfigurationRepository tenantDeliveryConfigurationRepository) {
        this.authInterface = authInterface;
        this.companyRepository = companyRepository;
        this.providerInterface = providerInterface;
        this.tenantDeliveryConfigurationRepository = tenantDeliveryConfigurationRepository;
    }

    public TenantSetupStatusResponse getSetupStatus(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.id() == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        UserType userType = tokenResponse.userType().orElse(null);
        if (userType != UserType.TENANT_ADMIN && userType != UserType.TENANT_MANAGER && userType != UserType.TENANT_USER) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Only tenant users can access setup status");
        }

        UUID tenantScopeId = tokenResponse.tenantId().orElse(tokenResponse.id());
        UUID userId = tokenResponse.id();
        boolean companyRegistered = companyRepository.existsById(tenantScopeId)
                || (!tenantScopeId.equals(userId) && companyRepository.existsById(userId));
        boolean providersConfigured = !providerInterface.getTenantProviders(tenantScopeId).isEmpty();
        boolean deliveryPreferencesConfigured =
                !tenantDeliveryConfigurationRepository.findByTenantId(tenantScopeId).isEmpty();

        boolean setupComplete = companyRegistered && providersConfigured && deliveryPreferencesConfigured;
        String nextStep = resolveNextStep(companyRegistered, providersConfigured, deliveryPreferencesConfigured);

        return new TenantSetupStatusResponse(
                companyRegistered,
                providersConfigured,
                deliveryPreferencesConfigured,
                setupComplete,
                nextStep);
    }

    private String resolveNextStep(
            boolean companyRegistered,
            boolean providersConfigured,
            boolean deliveryPreferencesConfigured) {
        if (!companyRegistered) {
            return "company";
        }
        if (!providersConfigured) {
            return "providers";
        }
        if (!deliveryPreferencesConfigured) {
            return "delivery";
        }
        return "complete";
    }
}
