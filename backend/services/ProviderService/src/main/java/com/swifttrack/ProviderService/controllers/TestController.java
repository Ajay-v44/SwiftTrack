package com.swifttrack.ProviderService.controllers;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.ProviderService.adapters.uber.UberDirectAdapter;
import com.swifttrack.dto.providerDto.QuoteInput;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class TestController {
    UberDirectAdapter uberDirectAdapter;
    public TestController(UberDirectAdapter uberDirectAdapter) {
        this.uberDirectAdapter = uberDirectAdapter;
    }
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(uberDirectAdapter.getQuote(new QuoteInput("123", "456", "789", "012")));
    }
}
