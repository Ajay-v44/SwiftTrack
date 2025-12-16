package com.swifttrack.FeignClient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.swifttrack.dto.GetProviders;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

@FeignClient(name = "provider-service", url = "http://localhost:8006")
public interface ProviderInterface {

    @GetMapping("/api/provider/orders/v1/getQuote")
    public QuoteResponse getQuote(@RequestHeader String token, @RequestBody QuoteInput quoteInput);

    @GetMapping("/api/providers/v1/getTenantProviders")
    public List<GetProviders> getTenantProviders(@RequestHeader String token);
}
