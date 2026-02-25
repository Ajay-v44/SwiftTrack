package com.swifttrack.ProviderService.services;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.OrderQuoteSessionInterface;
import com.swifttrack.ProviderService.adapters.porter.PorterAdapter;
import com.swifttrack.ProviderService.adapters.uber.UberDirectAdapter;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
import com.swifttrack.dto.orderDto.OrderQuoteSessionResponse;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

@Service
public class ProviderOrderServices {
    PorterAdapter porterAdapter;
    UberDirectAdapter uberDirectAdapter;
    OrderQuoteSessionInterface orderQuoteSessionInterface;

    public ProviderOrderServices(PorterAdapter porterAdapter, UberDirectAdapter uberDirectAdapter,
            OrderQuoteSessionInterface orderQuoteSessionInterface) {
        this.porterAdapter = porterAdapter;
        this.uberDirectAdapter = uberDirectAdapter;
        this.orderQuoteSessionInterface = orderQuoteSessionInterface;
    }

    public QuoteResponse getQuote(String token, String providerCode, QuoteInput quoteInput) {
        System.out.println("Provider Code: " + providerCode);
        if (providerCode.toUpperCase().equals("PORTER")) {
            return porterAdapter.getQuote(quoteInput);
        } else if (providerCode.toUpperCase().equals("UBER_DIRECT")) {
            return uberDirectAdapter.getQuote(quoteInput);
        } else {
            throw new RuntimeException("Invalid provider code");
        }

    }

    public CreateOrderResponse createOrder(String token, UUID quoteSessionId, CreateOrderRequest createOrderRequest) {
        List<OrderQuoteSessionResponse> orderQuoteSessionResponse = orderQuoteSessionInterface
                .getOrderQuoteSession(quoteSessionId);
        for (OrderQuoteSessionResponse orderQuoteSession : orderQuoteSessionResponse) {
            if (orderQuoteSession.providerCode().toUpperCase().equals("PORTER")) {
                return porterAdapter.createOrder(createOrderRequest);
            } else if (orderQuoteSession.providerCode().toUpperCase().equals("UBER_DIRECT")) {
                return uberDirectAdapter.createOrder(createOrderRequest);
            } else {
                throw new RuntimeException("Invalid provider code");
            }
        }
        return null;
    }

    public Message cancelOrder(String token, String orderId, String providerCode) {
        if (providerCode.toUpperCase().equals("PORTER")) {
            return porterAdapter.cancelOrder(orderId);
        } else if (providerCode.toUpperCase().equals("UBER_DIRECT")) {
            return uberDirectAdapter.cancelOrder(orderId);
        } else {
            throw new RuntimeException("Invalid provider code");
        }
    }

}
