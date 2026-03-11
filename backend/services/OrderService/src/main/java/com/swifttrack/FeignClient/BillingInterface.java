package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.swifttrack.dto.billingDto.BindQuoteOrderRequest;
import com.swifttrack.dto.billingDto.QuoteRequest;
import com.swifttrack.dto.billingDto.QuoteResponse;

@FeignClient(name = "billingandsettlementservice", url = "http://localhost:8080/billingandsettlementservice")
public interface BillingInterface {

    @PostMapping("/api/billing/getQuote")
    ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request);

    @PostMapping("/api/billing/bind-order")
    ResponseEntity<Void> bindOrder(@RequestBody BindQuoteOrderRequest request);
}
