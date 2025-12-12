package com.swifttrack.ProviderService.adapters.uber;

import com.swifttrack.ProviderService.adapters.uber.dto.UberQuoteRequest;
import com.swifttrack.ProviderService.adapters.uber.dto.UberQuoteResponse;
import com.swifttrack.ProviderService.adapters.uber.dto.UberTokenResponse;
import com.swifttrack.ProviderService.conf.DeliveryProvider;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;
import com.swifttrack.http.ApiResponse;
import com.swifttrack.http.ExternalApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class UberDirectAdapter implements DeliveryProvider {

    private final ExternalApiClient externalApiClient;
    private final RestTemplate restTemplate;
        
    @Value("${UBER_DIRECT_CUSTOMER_ID}")
    private String customerId;
        
    @Value("${UBER_DIRECT_CLIENT_ID}")
    private String clientId;
        
    @Value("${UBER_DIRECT_CLIENT_SECRET}")
    private String clientSecret;
        
    @Value("${UBER_DIRECT_AUTH_URL}")
    private String authUrl;
        
    @Value("${UBER_DIRECT_BASE_URL}")
    private String baseUrl;
        
    @Value("${UBER_DIRECT_QUOTES_ENDPOINT}")
    private String quotesEndpoint;

    private String token;
    private Long tokenExpireTime;

    public UberDirectAdapter(ExternalApiClient externalApiClient, RestTemplate restTemplate) {
        this.externalApiClient = externalApiClient;
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves and caches OAuth token from Uber API
     * Uses multipart/form-data as required by Uber OAuth endpoint
     */
    private void getToken() {
        if (token == null || tokenExpireTime == null || tokenExpireTime < System.currentTimeMillis()) {
            log.info("Fetching new Uber Direct OAuth token");

            try {
                // Build multipart form data
                MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                body.add("client_id", clientId);
                body.add("client_secret", clientSecret);
                body.add("grant_type", "client_credentials");
                body.add("scope", "eats.deliveries direct.organizations");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

                // Use RestTemplate directly to avoid JSON serialization
                ResponseEntity<UberTokenResponse> response = restTemplate.postForEntity(
                        authUrl,
                        request,
                        UberTokenResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    UberTokenResponse tokenResponse = response.getBody();
                    this.token = tokenResponse.getAccessToken();
                    log.info("Uber Direct token: {}", this.token);
                    this.tokenExpireTime = System.currentTimeMillis() + (tokenResponse.getExpiresIn() * 1000);
                    log.info("Uber Direct token fetched successfully, expires in {} seconds",
                            tokenResponse.getExpiresIn());
                } else {
                    log.error("Failed to fetch Uber Direct token: HTTP {}", response.getStatusCode());
                    throw new RuntimeException("Failed to authenticate with Uber Direct API");
                }
            } catch (Exception e) {
                log.error("Exception while fetching Uber Direct token: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to authenticate with Uber Direct API: " + e.getMessage());
            }
        }
    }

    @Override
    public QuoteResponse getQuote(QuoteInput quoteInput) {
        getToken();
        
        try {
            log.info("Fetching Uber Direct quote");
            
            // Build request with static data and dynamic times
            Instant now = Instant.now();
            UberQuoteRequest request = new UberQuoteRequest();
            request.setPickupAddress("Doddanagamangala Rd, off Hosa Road, Lavakusha Nagar, Pragathi Nagar, Electronic City, Bengaluru, Karnataka 560100");
            request.setDropoffAddress("Annapoorneshwari Complex 6th Main, Survey No. 37/1, Hosur Rd, Singasandra, Bengaluru, Karnataka 560068");
            request.setPickupLatitude(quoteInput.pickupLat());
            request.setPickupLongitude(quoteInput.pickupLng());
            request.setDropoffLatitude(quoteInput.dropoffLat());
            request.setDropoffLongitude(quoteInput.dropoffLng());
            
            // Pickup: now and deadline in 1 hour
            request.setPickupReadyDt(now.toString());
            request.setPickupDeadlineDt(now.plus(1, ChronoUnit.HOURS).toString());
            
            // Dropoff: in 3 hours and deadline in 4 hours
            request.setDropoffReadyDt(now.plus(1, ChronoUnit.HOURS).toString());
            request.setDropoffDeadlineDt(now.plus(4, ChronoUnit.HOURS).toString());
            
            request.setPickupPhoneNumber("+918921057655");
            request.setDropoffPhoneNumber("+918921057654");
            request.setManifestTotalValue(123);
            request.setExternalStoreId("external-store-001");
            
            // Make API call to Uber
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            HttpEntity<UberQuoteRequest> httpRequest = new HttpEntity<>(request, headers);
            
            // Build the full API URL from .env values
            String fullUrl = baseUrl + quotesEndpoint.replace("{customerId}", customerId);
            
            ResponseEntity<UberQuoteResponse> response = restTemplate.postForEntity(
                fullUrl,
                httpRequest,
                UberQuoteResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UberQuoteResponse quoteResponse = response.getBody();
                log.info("Uber Direct quote fetched successfully. Quote ID: {}", quoteResponse.getId());
                
                // Convert Uber response to internal QuoteResponse
                // Fee is in paise, convert to rupees
                float priceInRupees = quoteResponse.getFeeInRupees();
                String currency = quoteResponse.getCurrencyType() != null ? quoteResponse.getCurrencyType() : "INR";
                
                return new QuoteResponse(priceInRupees, currency);
            } else {
                log.error("Failed to fetch Uber Direct quote: HTTP {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch Uber Direct quote");
            }
        } catch (Exception e) {
            log.error("Exception while fetching Uber Direct quote: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch quote: " + e.getMessage());
        }
    }

}
