package com.swifttrack.ProviderService.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.ProviderService.dto.CreateProviderAndServicableAreas;
import com.swifttrack.ProviderService.dto.CreateServicableAreas;
import com.swifttrack.ProviderService.dto.GetProviders;
import com.swifttrack.ProviderService.dto.ProviderOnBoardingInput;
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

    public ProviderService(ProviderRepository providerRepository,
            AuthInterface authInterface,
            TenantProviderConfigRepository tenantProviderConfigRepository,
            ProviderServicableAreasRepository providerServicableAreasRepository,
            ProviderOnboardingRequestRepository providerOnboardingRequestRepository) {
        this.providerRepository = providerRepository;
        this.authInterface = authInterface;
        this.tenantProviderConfigRepository = tenantProviderConfigRepository;
        this.providerServicableAreasRepository = providerServicableAreasRepository;
        this.providerOnboardingRequestRepository = providerOnboardingRequestRepository;
    }

    public List<GetProviders> getProviders() {
        List<GetProviders> getProviders = new ArrayList<>();
        List<Provider> providers = providerRepository.findByIsActive(true);
        for (Provider provider : providers) {
            List<String> servicableAreas = provider.getProviderServicableAreas().stream()
                    .map(providerServicableAreas -> providerServicableAreas.getCity()).collect(Collectors.toList());
            getProviders.add(new GetProviders(provider.getId(), provider.getProviderName(), provider.getDescription(),
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
        providerToSave.setActive(true);
        providerToSave.setCreatedById(tokenResponse.id());
        providerToSave.setUpdatedBy(tokenResponse.id());
        providerRepository.save(providerToSave);

        for (CreateServicableAreas createServicableAreas : provider.servicableAreas()) {
            ProviderServicableAreas providerServicableAreas = new ProviderServicableAreas();
            providerServicableAreas.setProvider(providerToSave);
            providerServicableAreas.setCity(createServicableAreas.city());
            providerServicableAreas.setState(createServicableAreas.state());
            providerServicableAreas.setPinCode(createServicableAreas.pinCode());
            providerServicableAreas.setActive(true);
            providerServicableAreas.setCreatedBy(tokenResponse.id());
            providerServicableAreas.setUpdatedBy(tokenResponse.id());
            providerServicableAreasRepository.save(providerServicableAreas);
        }

        return new Message("Provider created successfully");
    }

    public Message configureTenantProviders(String token, List<UUID> providers) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null)
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        for (UUID providerId : providers) {
            Provider provider = providerRepository.findById(providerId).orElse(null);
            if (provider != null) {
                TenantProviderConfig tenantProviderConfig = new TenantProviderConfig();
                tenantProviderConfig.setProvider(provider);
                tenantProviderConfig.setTenantId(tokenResponse.tenantId().get());
                tenantProviderConfig.setEnabled(true);
                tenantProviderConfig.setCreatedBy(tokenResponse.id());
                tenantProviderConfig.setUpdatedBy(tokenResponse.id());
                tenantProviderConfigRepository.save(tenantProviderConfig);
            }
        }
        return new Message("Providers configured successfully");

    }

    public List<GetProviders> getTenantProviders(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.tenantId() == null)
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        return tenantProviderConfigRepository.findByTenantId(tokenResponse.tenantId().get()).stream()
                .map(tenantProviderConfig -> new GetProviders(tenantProviderConfig.getProvider().getId(),
                        tenantProviderConfig.getProvider().getProviderName(),
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

    public Message requestProviderOnboarding(String token, ProviderOnBoardingInput providerOnboardingRequest) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null )
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        if (providerOnboardingRequestRepository.findByRequestedUserId(tokenResponse.id()) != null)
            throw new CustomException(HttpStatus.ALREADY_REPORTED, "Provider onboarding already requested,please wait for approval");

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
}
// todo -> enable or disable teant providers, remove tenant providers
