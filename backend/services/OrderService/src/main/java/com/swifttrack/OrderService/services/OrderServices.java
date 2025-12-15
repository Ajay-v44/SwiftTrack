package com.swifttrack.OrderService.services;

import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.ProviderInterface;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

@Service
public class OrderServices {
    ProviderInterface providerInterface;

    public OrderServices(ProviderInterface providerInterface) {
        this.providerInterface = providerInterface;
    }

    public QuoteResponse getQuote(String token, QuoteInput quoteInput) {
        return providerInterface.getQuote(token, quoteInput);
    }

}
