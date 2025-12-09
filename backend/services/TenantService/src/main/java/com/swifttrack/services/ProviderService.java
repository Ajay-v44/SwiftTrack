package com.swifttrack.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.ProviderInterface;
import com.swifttrack.dto.GetProviders;

@Service
public class ProviderService {
    ProviderInterface providerInterface;

    public ProviderService(ProviderInterface providerInterface) {
        this.providerInterface = providerInterface;
    }

    public List<GetProviders> getProviders() {
        return providerInterface.getProviders();
    }

}
