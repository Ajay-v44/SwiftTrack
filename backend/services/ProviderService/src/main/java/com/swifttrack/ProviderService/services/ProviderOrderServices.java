package com.swifttrack.ProviderService.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.swifttrack.ProviderService.adapters.porter.PorterAdapter;
import com.swifttrack.ProviderService.adapters.uber.UberDirectAdapter;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

@Service
public class ProviderOrderServices {
    PorterAdapter porterAdapter;
    UberDirectAdapter uberDirectAdapter;

    public ProviderOrderServices(PorterAdapter porterAdapter, UberDirectAdapter uberDirectAdapter) {
        this.porterAdapter = porterAdapter;
        this.uberDirectAdapter = uberDirectAdapter;
    }

    public QuoteResponse getQuote(String token, String providerCode, QuoteInput quoteInput) {

        if (providerCode.toUpperCase().equals("PORTER")) {
            return porterAdapter.getQuote(quoteInput);
        } else if (providerCode.toUpperCase().equals("UBER_DIRECT")) {
            return uberDirectAdapter.getQuote(quoteInput);
        } else {
            throw new RuntimeException("Invalid provider code");
        }

    }

}
