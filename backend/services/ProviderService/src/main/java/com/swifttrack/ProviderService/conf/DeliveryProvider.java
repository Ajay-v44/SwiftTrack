package com.swifttrack.ProviderService.conf;

import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

public interface DeliveryProvider {
    QuoteResponse getQuote(QuoteInput quoteInput);

}
