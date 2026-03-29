package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.OrderService.dto.BillingAccountSnapshot;
import com.swifttrack.dto.billingDto.BindQuoteOrderRequest;
import com.swifttrack.dto.billingDto.OrderDebitSummaryResponse;
import com.swifttrack.dto.billingDto.QuoteRequest;
import com.swifttrack.dto.billingDto.QuoteResponse;

@FeignClient(name = "billingandsettlementservice", url = "http://localhost:8080/billingandsettlementservice")
public interface BillingInterface {

    @PostMapping("/api/billing/getQuote")
    ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request);

    @PostMapping("/api/billing/bind-order")
    ResponseEntity<Void> bindOrder(@RequestBody BindQuoteOrderRequest request);

    @GetMapping("/api/accounts/v1/getMyAccount")
    ResponseEntity<BillingAccountSnapshot> getMyAccount(@RequestHeader("token") String token);

    @GetMapping("/api/accounts/v1/internal/orderDebit")
    ResponseEntity<OrderDebitSummaryResponse> getOrderDebitSummary(
            @RequestParam("accountId") java.util.UUID accountId,
            @RequestParam("orderId") java.util.UUID orderId);
}
