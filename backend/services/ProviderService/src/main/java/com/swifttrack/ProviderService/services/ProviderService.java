package com.swifttrack.ProviderService.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.FeignClient.BillingAndSettlementInterface;
import com.swifttrack.ProviderService.dto.CreateProviderAndServicableAreas;
import com.swifttrack.ProviderService.dto.CreateServicableAreas;
import com.swifttrack.ProviderService.dto.GetProviders;
import com.swifttrack.ProviderService.dto.ProviderOnBoardingInput;
import com.swifttrack.ProviderService.dto.TenantProviderConfigResponse;
import com.swifttrack.ProviderService.models.Provider;
import com.swifttrack.ProviderService.models.ProviderOnboardingRequest;
import com.swifttrack.ProviderService.models.ProviderServicableAreas;
import com.swifttrack.ProviderService.models.TenantProviderConfig;
import com.swifttrack.ProviderService.models.enums.Status;
import com.swifttrack.ProviderService.repositories.ProviderOnboardingRequestRepository;
import com.swifttrack.ProviderService.repositories.ProviderRepository;
import com.swifttrack.ProviderService.repositories.ProviderServicableAreasRepository;
import com.swifttrack.ProviderService.repositories.TenantProviderConfigRepository;
import com.swifttrack.enums.UserType;
import com.swifttrack.enums.BillingAndSettlement.AccountType;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.exception.CustomException;

@Service
public class ProviderService {
    ProviderRepository providerRepository;
    ProviderServicableAreasRepository providerServicableAreasRepository;
    AuthInterface authInterface;
    TenantProviderConfigRepository tenantProviderConfigRepository;
    ProviderOnboardingRequestRepository providerOnboardingRequestRepository;
    BillingAndSettlementInterface billingAndSettlementInterface;

    public ProviderService(ProviderRepository providerRepository,
            AuthInterface authInterface,
            TenantProviderConfigRepository tenantProviderConfigRepository,
            ProviderServicableAreasRepository providerServicableAreasRepository,
            ProviderOnboardingRequestRepository providerOnboardingRequestRepository,
            BillingAndSettlementInterface billingAndSettlementInterface) {
        this.providerRepository = providerRepository;
        this.authInterface = authInterface;
        this.tenantProviderConfigRepository = tenantProviderConfigRepository;
        this.providerServicableAreasRepository = providerServicableAreasRepository;
        this.providerOnboardingRequestRepository = providerOnboardingRequestRepository;
        this.billingAndSettlementInterface = billingAndSettlementInterface;
    }

    public List<GetProviders> getProviders() {
        List<GetProviders> getProviders = new ArrayList<>();
        List<Provider> providers = providerRepository.findByIsActive(true);
        for (Provider provider : providers) {
            List<String> servicableAreas = provider.getProviderServicableAreas().stream()
                    .map(providerServicableAreas -> providerServicableAreas.getCity()).collect(Collectors.toList());
            getProviders.add(new GetProviders(provider.getId(), provider.getProviderName(), provider.getProviderCode(),
                    provider.getDescription(),
                    provider.getLogoUrl(), provider.getWebsiteUrl(), provider.isSupportsHyperlocal(),
                    provider.isSupportsCourier(), provider.isSupportsSameDay(), provider.isSupportsIntercity(),
                    servicableAreas));
        }
        return getProviders;
    }

    public Message createProvider(String token, CreateProviderAndServicableAreas provider) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        // if(tokenResponse
        // !=null&&(tokenResponse.userType().get()!=UserType.SYSTEM_ADMIN ||
        // tokenResponse.userType().get()!=UserType.SYSTEM_USER))
        // throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");

        if (providerRepository.findByProviderName(provider.providerName()) != null)
            throw new CustomException(HttpStatus.ALREADY_REPORTED, "Provider already exists");

        Provider providerToSave = new Provider();
        providerToSave.setProviderName(provider.providerName());
        providerToSave.setProviderCode(provider.providerName().toUpperCase().replace(" ", "_"));
        providerToSave.setDescription(provider.description());
        providerToSave.setLogoUrl(provider.logoUrl());
        providerToSave.setWebsiteUrl(provider.websiteUrl());
        providerToSave.setSupportsHyperlocal(provider.supportsHyperlocal());
        providerToSave.setSupportsCourier(provider.supportsCourier());
        providerToSave.setSupportsSameDay(provider.supportsSameDay());
        providerToSave.setSupportsIntercity(provider.supportsIntercity());
        providerToSave.setActive(false);
        providerToSave.setCreatedById(tokenResponse.id());
        providerToSave.setUpdatedBy(tokenResponse.id());
        providerRepository.save(providerToSave);

        // for (CreateServicableAreas createServicableAreas :
        // provider.servicableAreas()) {
        // ProviderServicableAreas providerServicableAreas = new
        // ProviderServicableAreas();
        // providerServicableAreas.setProvider(providerToSave);
        // providerServicableAreas.setCity(createServicableAreas.city());
        // providerServicableAreas.setState(createServicableAreas.state());
        // providerServicableAreas.setPinCode(createServicableAreas.pinCode());
        // providerServicableAreas.setActive(true);
        // providerServicableAreas.setCreatedBy(tokenResponse.id());
        // providerServicableAreas.setUpdatedBy(tokenResponse.id());
        // providerServicableAreasRepository.save(providerServicableAreas);
        // }

        billingAndSettlementInterface.createAccount(token, providerToSave.getId(), AccountType.PROVIDER);

        return new Message("Provider created successfully");
    }

    public Message configureTenantProviders(String token, List<UUID> providers) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null || tokenResponse.tenantId().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        for (UUID providerId : providers) {
            Provider provider = providerRepository.findById(providerId).orElse(null);
            if (provider != null) {
                TenantProviderConfig existingConfig = tenantProviderConfigRepository
                        .findByTenantIdAndProviderId(tokenResponse.tenantId().get(), providerId);
                if (existingConfig == null) {
                    TenantProviderConfig tenantProviderConfig = new TenantProviderConfig();
                    tenantProviderConfig.setProvider(provider);
                    tenantProviderConfig.setTenantId(tokenResponse.tenantId().get());
                    tenantProviderConfig.setEnabled(true);
                    tenantProviderConfig.setCreatedBy(tokenResponse.id());
                    tenantProviderConfig.setUpdatedBy(tokenResponse.id());
                    tenantProviderConfigRepository.save(tenantProviderConfig);
                } else {
                    existingConfig.setEnabled(true);
                    existingConfig.setUpdatedBy(tokenResponse.id());
                    tenantProviderConfigRepository.save(existingConfig);
                }
            }
        }
        return new Message("Providers configured successfully");

    }

    public List<GetProviders> getTenantProviders(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null || tokenResponse.tenantId().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        return getTenantProvidersByTenantId(tokenResponse.tenantId().get());
    }

    public List<GetProviders> getTenantProvidersByTenantId(UUID tenantId) {
        return tenantProviderConfigRepository.findByTenantId(tenantId).stream()
                .map(tenantProviderConfig -> new GetProviders(tenantProviderConfig.getProvider().getId(),
                        tenantProviderConfig.getProvider().getProviderName(),
                        tenantProviderConfig.getProvider().getProviderCode(),
                        tenantProviderConfig.getProvider().getDescription(),
                        tenantProviderConfig.getProvider().getLogoUrl(),
                        tenantProviderConfig.getProvider().getWebsiteUrl(),
                        tenantProviderConfig.getProvider().isSupportsHyperlocal(),
                        tenantProviderConfig.getProvider().isSupportsCourier(),
                        tenantProviderConfig.getProvider().isSupportsSameDay(),
                        tenantProviderConfig.getProvider().isSupportsIntercity(),
                        tenantProviderConfig.getProvider().getProviderServicableAreas().stream()
                                .map(providerServicableAreas -> providerServicableAreas.getCity())
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public List<TenantProviderConfigResponse> getTenantProviderConfigs(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null || tokenResponse.tenantId().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        return getTenantProviderConfigsByTenantId(tokenResponse.tenantId().get());
    }

    public List<TenantProviderConfigResponse> getTenantProviderConfigsByTenantId(UUID tenantId) {
        return tenantProviderConfigRepository.findByTenantId(tenantId).stream()
                .map(this::toTenantProviderConfigResponse)
                .collect(Collectors.toList());
    }

    public Message requestProviderOnboarding(String token, ProviderOnBoardingInput providerOnboardingRequest) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null)
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        if (providerOnboardingRequestRepository.findByRequestedUserId(tokenResponse.id()) != null)
            throw new CustomException(HttpStatus.ALREADY_REPORTED,
                    "Provider onboarding already requested,please wait for approval");

        ProviderOnboardingRequest providerOnboardingRequestToSave = new ProviderOnboardingRequest();
        providerOnboardingRequestToSave.setRequestedUserId(tokenResponse.id());
        providerOnboardingRequestToSave.setProviderName(providerOnboardingRequest.providerName());
        providerOnboardingRequestToSave.setProviderWebsite(providerOnboardingRequest.providerWebsite());
        providerOnboardingRequestToSave.setContactEmail(providerOnboardingRequest.contactEmail());
        providerOnboardingRequestToSave.setContactPhone(providerOnboardingRequest.contactPhone());
        providerOnboardingRequestToSave.setNotes(providerOnboardingRequest.notes());
        // Ensure docLinks is valid JSON, defaulting to empty object if null or empty
        String docLinks = providerOnboardingRequest.docLinks();
        // Always default to valid JSON
        if (docLinks == null || docLinks.trim().isEmpty()) {
            docLinks = "{}";
        }
        providerOnboardingRequestToSave.setDocLinks(docLinks);
        providerOnboardingRequestToSave.setStatus(Status.PENDING);
        providerOnboardingRequestToSave.setCreatedBy(tokenResponse.id());
        providerOnboardingRequestToSave.setUpdatedBy(tokenResponse.id());
        providerOnboardingRequestRepository.save(providerOnboardingRequestToSave);
        return new Message("Provider onboarding requested successfully please wait for approval");
    }

    public List<GetProviders> getProviderByStatus(String token, Boolean status) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null)
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        return providerRepository.findByIsActive(status).stream()
                .map(provider -> new GetProviders(provider.getId(),
                        provider.getProviderName(),
                        provider.getProviderCode(),
                        provider.getDescription(),
                        provider.getLogoUrl(),
                        provider.getWebsiteUrl(),
                        provider.isSupportsHyperlocal(),
                        provider.isSupportsCourier(),
                        provider.isSupportsSameDay(),
                        provider.isSupportsIntercity(),
                        provider.getProviderServicableAreas().stream()
                                .map(providerServicableAreas -> providerServicableAreas.getCity())
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public Message updateProviderStatus(String token, UUID providerId, Boolean status) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.userType() == null || tokenResponse.userType().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");

        UserType userType = tokenResponse.userType().get();
        if (userType != UserType.SUPER_ADMIN && userType != UserType.SYSTEM_USER)
            throw new CustomException(HttpStatus.FORBIDDEN, "Only SUPER_ADMIN or SYSTEM_USER can update provider status");
        if (status == null)
            throw new CustomException(HttpStatus.BAD_REQUEST, "status is required");

        Provider provider = providerRepository.findById(providerId).orElse(null);
        if (provider == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Provider not found");

        provider.setActive(status);
        provider.setUpdatedBy(tokenResponse.id());
        providerRepository.save(provider);
        return new Message("Provider status updated successfully");
    }

    public Message setTenantProviderStatus(String token, UUID providerId, Boolean enabled, String disabledReason) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null || tokenResponse.tenantId().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        if (enabled == null)
            throw new CustomException(HttpStatus.BAD_REQUEST, "enabled flag is required");

        TenantProviderConfig config = tenantProviderConfigRepository
                .findByTenantIdAndProviderId(tokenResponse.tenantId().get(), providerId);
        if (config == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Provider config not found for tenant");

        config.setEnabled(enabled);
        config.setDisabledReason(enabled ? "" : (disabledReason == null ? "" : disabledReason.trim()));
        config.setUpdatedBy(tokenResponse.id());
        tenantProviderConfigRepository.save(config);
        return new Message("Tenant provider status updated successfully");
    }

    public Message removeTenantProvider(String token, UUID providerId) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null || tokenResponse.tenantId().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");

        TenantProviderConfig config = tenantProviderConfigRepository
                .findByTenantIdAndProviderId(tokenResponse.tenantId().get(), providerId);
        if (config == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Provider config not found for tenant");

        tenantProviderConfigRepository.delete(config);
        return new Message("Tenant provider removed successfully");
    }

    private TenantProviderConfigResponse toTenantProviderConfigResponse(TenantProviderConfig tenantProviderConfig) {
        Provider provider = tenantProviderConfig.getProvider();

        return new TenantProviderConfigResponse(
                provider.getId(),
                provider.getProviderName(),
                provider.getProviderCode(),
                provider.getDescription(),
                provider.getLogoUrl(),
                provider.getWebsiteUrl(),
                provider.isSupportsHyperlocal(),
                provider.isSupportsCourier(),
                provider.isSupportsSameDay(),
                provider.isSupportsIntercity(),
                provider.getProviderServicableAreas().stream()
                        .map(providerServicableAreas -> providerServicableAreas.getCity())
                        .collect(Collectors.toList()),
                tenantProviderConfig.isEnabled(),
                tenantProviderConfig.isVerified(),
                tenantProviderConfig.getDisabledReason(),
                tenantProviderConfig.getCreatedAt(),
                tenantProviderConfig.getUpdatedAt());
    }
}
