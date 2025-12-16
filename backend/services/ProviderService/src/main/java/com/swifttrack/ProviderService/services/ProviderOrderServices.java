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

    public QuoteResponse getQuote(String token, QuoteInput quoteInput) {

        return null;

    }

}
