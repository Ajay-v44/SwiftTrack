package com.swifttrack.FeignClient;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.GetProviders;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

@FeignClient(name = "providerservice", url = "http://localhost:8080/providerservice")
public interface ProviderInterface {

        @GetMapping("/api/providers/v1/list")
        public List<GetProviders> getProviders();

        @GetMapping("/api/providers/v1/getTenantProviders")
        public List<GetProviders> getTenantProviders(@RequestHeader String token);

        @GetMapping("/api/providers/v1/internal/getTenantProviders")
        public List<GetProviders> getTenantProvidersByTenantId(@RequestParam UUID tenantId);

        @PostMapping("/api/provider/orders/v1/getQuote")
        public QuoteResponse getQuote(@RequestHeader String token, @RequestParam String providerCode,
                        @RequestBody QuoteInput quoteInput);

        @PostMapping(value = "/api/provider/orders/v1/internal/getQuote", consumes = "application/json", produces = "application/json")
        public QuoteResponse getQuoteInternal(@RequestParam String providerCode, @RequestBody QuoteInput quoteInput);

        @PostMapping("/api/provider/orders/v1/createOrder")
        public CreateOrderResponse createOrder(@RequestHeader String token, @RequestParam UUID quoteSessionId,
                        @RequestBody CreateOrderRequest createOrderRequest);

        @PostMapping(value = "/api/provider/orders/v1/internal/createOrder", consumes = "application/json", produces = "application/json")
        public CreateOrderResponse createOrderInternal(@RequestParam String providerCode,
                        @RequestBody CreateOrderRequest createOrderRequest);

        @PostMapping("/api/provider/orders/v1/cancelOrder")
        public Message cancelOrder(@RequestHeader String token, @RequestParam String orderId,
                        @RequestParam String providerCode);
}
