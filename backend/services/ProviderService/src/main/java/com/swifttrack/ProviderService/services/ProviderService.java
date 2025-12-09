package com.swifttrack.ProviderService.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.ProviderService.dto.CreateProviderAndServicableAreas;
import com.swifttrack.ProviderService.dto.CreateServicableAreas;
import com.swifttrack.ProviderService.dto.GetProviders;
import com.swifttrack.ProviderService.models.Provider;
import com.swifttrack.ProviderService.models.ProviderServicableAreas;
import com.swifttrack.ProviderService.repositories.ProviderRepository;
import com.swifttrack.ProviderService.repositories.ProviderServicableAreasRepository;
import com.swifttrack.enums.UserType;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.exception.CustomException;

@Service
public class ProviderService {
    ProviderRepository providerRepository;
    ProviderServicableAreasRepository providerServicableAreasRepository;
    AuthInterface authInterface;

    public ProviderService(ProviderRepository providerRepository,
            AuthInterface authInterface,
            ProviderServicableAreasRepository providerServicableAreasRepository) {
        this.providerRepository = providerRepository;
        this.authInterface = authInterface;
        this.providerServicableAreasRepository = providerServicableAreasRepository;
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
        // if(tokenResponse !=null&&(tokenResponse.userType().get()!=UserType.SYSTEM_ADMIN || tokenResponse.userType().get()!=UserType.SYSTEM_USER))
        //     throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        
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
}
