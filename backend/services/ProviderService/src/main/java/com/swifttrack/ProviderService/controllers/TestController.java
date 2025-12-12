package com.swifttrack.ProviderService.controllers;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.ProviderService.adapters.porter.PorterAdapter;
import com.swifttrack.ProviderService.adapters.uber.UberDirectAdapter;
import com.swifttrack.dto.providerDto.QuoteInput;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class TestController {
    UberDirectAdapter uberDirectAdapter;
    PorterAdapter porterAdapter;    
    public TestController(UberDirectAdapter uberDirectAdapter, PorterAdapter porterAdapter) {
        this.uberDirectAdapter = uberDirectAdapter;
        this.porterAdapter = porterAdapter;
    }
    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(porterAdapter.getQuote(quoteInput));
    }
}
