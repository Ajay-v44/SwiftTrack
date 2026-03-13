package com.swifttrack.dto.providerDto;

public record QuoteResponse(
        float price,
        String currency,
        String quoteId) {

    public QuoteResponse(float price, String currency) {
        this(price, currency, null);
    }
}
