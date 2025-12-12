package com.swifttrack.ProviderService.adapters.porter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.swifttrack.ProviderService.adapters.porter.dto.PorterGetQuoteInp;
import com.swifttrack.ProviderService.adapters.porter.dto.PorterQuoteResponse;
import com.swifttrack.ProviderService.conf.DeliveryProvider;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;
import com.swifttrack.http.ExternalApiClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PorterAdapter implements DeliveryProvider {
    private final ExternalApiClient externalApiClient;
    private final RestTemplate restTemplate;

    @Value("${PORTER_API_URL}")
    private String porterApiUrl;
    @Value("${PORTER_API_KEY}")
    private String porterApiKey;

    public PorterAdapter(ExternalApiClient externalApiClient, RestTemplate restTemplate) {
        this.externalApiClient = externalApiClient;
        this.restTemplate = restTemplate;
    }

    @Override
    public QuoteResponse getQuote(QuoteInput quoteInput) {
        try {
            // Build request object
            PorterGetQuoteInp porterGetQuoteInp = new PorterGetQuoteInp();
            porterGetQuoteInp.setPickupDetails(new PorterGetQuoteInp.PickupDetails(quoteInput.pickupLat(), quoteInput.pickupLng()));
            porterGetQuoteInp.setDropDetails(new PorterGetQuoteInp.DropDetails(quoteInput.dropoffLat(), quoteInput.dropoffLng()));
            
            // Create customer with mobile details
            PorterGetQuoteInp.Customer.Mobile mobile = new PorterGetQuoteInp.Customer.Mobile("+91", "9999999999");
            porterGetQuoteInp.setCustomer(new PorterGetQuoteInp.Customer("test", mobile));

            String fullUrl = porterApiUrl + "/v1/get_quote";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", porterApiKey);

            HttpEntity<PorterGetQuoteInp> request = new HttpEntity<>(porterGetQuoteInp, headers);
            ResponseEntity<PorterQuoteResponse> response = restTemplate.postForEntity(fullUrl, request, PorterQuoteResponse.class);
            
            // Check if response is successful
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PorterQuoteResponse quoteResponse = response.getBody();
                
                // Look for 2 Wheeler vehicle type
                if (quoteResponse.getVehicles() != null) {
                    for (PorterQuoteResponse.Vehicle vehicle : quoteResponse.getVehicles()) {
                        if ("2 Wheeler".equals(vehicle.getType()) && vehicle.getFare() != null) {
                            // Convert minor_amount from paise to rupees
                            float priceInRupees = vehicle.getFare().getMinorAmount() / 100.0f;
                            String currency = vehicle.getFare().getCurrency() != null ? vehicle.getFare().getCurrency() : "INR";
                            return new QuoteResponse(priceInRupees, currency);
                        }
                    }
                }
                
                log.error("No 2 Wheeler vehicle found in Porter response");
                throw new RuntimeException("No 2 Wheeler vehicle found in Porter response");
            } else {
                log.error("Failed to fetch Porter quote: HTTP {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch quote from Porter: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Exception while fetching Porter quote: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch quote from Porter: " + e.getMessage());
        }
    }
}