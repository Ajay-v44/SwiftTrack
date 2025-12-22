package com.swifttrack.ProviderService.adapters.uber;

import com.swifttrack.ProviderService.adapters.uber.dto.UberCreateOrderRequest;
import com.swifttrack.ProviderService.adapters.uber.dto.UberCreateOrderResponse;
import com.swifttrack.ProviderService.adapters.uber.dto.UberQuoteRequest;
import com.swifttrack.ProviderService.adapters.uber.dto.UberQuoteResponse;
import com.swifttrack.ProviderService.adapters.uber.dto.UberTokenResponse;
import com.swifttrack.ProviderService.conf.DeliveryProvider;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
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

    @Value("${UBER_DIRECT_DELIVERIES_ENDPOINT}")
    private String deliveriesEndpoint;

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
            request.setPickupAddress(
                    "Doddanagamangala Rd, off Hosa Road, Lavakusha Nagar, Pragathi Nagar, Electronic City, Bengaluru, Karnataka 560100");
            request.setDropoffAddress(
                    "Annapoorneshwari Complex 6th Main, Survey No. 37/1, Hosur Rd, Singasandra, Bengaluru, Karnataka 560068");
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
                    UberQuoteResponse.class);

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

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        log.info("Creating Uber Direct order");
        getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        UberCreateOrderRequest uberRequest = UberCreateOrderRequest.builder()
                .idempotencyKey(createOrderRequest.idempotencyKey())
                .externalId(createOrderRequest.orderReference())
                .manifestReference(createOrderRequest.orderReference())
                .quoteId(createOrderRequest.quoteId())
                .externalStoreId(
                        createOrderRequest.externalMetadata() != null ? createOrderRequest.externalMetadata().storeId()
                                : "external-store-001")
                .manifestTotalValue(
                        createOrderRequest.packageInfo() != null ? createOrderRequest.packageInfo().totalValue() : 0)
                .tip(createOrderRequest.deliveryPreferences() != null
                        ? createOrderRequest.deliveryPreferences().tipAmount()
                        : 0)

                // Pickup details
                .pickupAddress(createOrderRequest.pickup().address().line1())
                .pickupName(createOrderRequest.pickup().contact().name())
                .pickupPhoneNumber(createOrderRequest.pickup().contact().phone())
                .pickupLatitude(createOrderRequest.pickup().address().latitude())
                .pickupLongitude(createOrderRequest.pickup().address().longitude())
                .pickupBusinessName(createOrderRequest.pickup().businessName())
                .pickupNotes(createOrderRequest.pickup().notes())
                .pickupReadyDt(createOrderRequest.timeWindows().pickupReadyAt().toString())
                .pickupDeadlineDt(createOrderRequest.timeWindows().pickupDeadlineAt().toString())

                // Dropoff details
                .dropoffAddress(createOrderRequest.dropoff().address().line1())
                .dropoffName(createOrderRequest.dropoff().contact().name())
                .dropoffPhoneNumber(createOrderRequest.dropoff().contact().phone())
                .dropoffLatitude(createOrderRequest.dropoff().address().latitude())
                .dropoffLongitude(createOrderRequest.dropoff().address().longitude())
                .dropoffBusinessName(createOrderRequest.dropoff().businessName())
                .dropoffNotes(createOrderRequest.dropoff().notes())
                .dropoffReadyDt(createOrderRequest.timeWindows().dropoffReadyAt().toString())
                .dropoffDeadlineDt(createOrderRequest.timeWindows().dropoffDeadlineAt().toString())

                // Preferences & Verifications
                .deliverableAction(createOrderRequest.deliveryPreferences() != null
                        && createOrderRequest.deliveryPreferences().deliverableAction() != null
                                ? (createOrderRequest.deliveryPreferences()
                                        .deliverableAction() == CreateOrderRequest.DeliverableAction.MEET_AT_DOOR
                                                ? "deliverable_action_meet_at_door"
                                                : "deliverable_action_leave_at_door")
                                : "deliverable_action_meet_at_door")
                .undeliverableAction(createOrderRequest.deliveryPreferences() != null
                        && createOrderRequest.deliveryPreferences().undeliverableAction() != null
                                ? (createOrderRequest.deliveryPreferences()
                                        .undeliverableAction() == CreateOrderRequest.UndeliverableAction.LEAVE_AT_LOCATION
                                                ? "leave_at_door"
                                                : "return")
                                : "leave_at_door")
                .requiresId(createOrderRequest.dropoff().verification() != null
                        && createOrderRequest.dropoff().verification().requireId())
                .requiresDropoffSignature(createOrderRequest.dropoff().verification() != null
                        && createOrderRequest.dropoff().verification().requireSignature())

                // Placeholder / Static data for remaining required fields
                .testSpecifications(UberCreateOrderRequest.TestSpecifications.builder()
                        .roboCourierSpecification(
                                UberCreateOrderRequest.RoboCourierSpecification.builder().mode("auto").build())
                        .build())
                .build();

        // Map items if present
        if (createOrderRequest.items() != null && !createOrderRequest.items().isEmpty()) {
            uberRequest.setManifestItems(createOrderRequest.items().stream()
                    .map(item -> UberCreateOrderRequest.ManifestItem.builder()
                            .name(item.name())
                            .quantity(item.quantity())
                            .price(item.price())
                            .weight(item.weightGrams())
                            .mustBeUpright(item.mustBeUpright())
                            .size("medium") // Default static value
                            .dimensions(item.dimensionsCm() != null ? UberCreateOrderRequest.Dimensions.builder()
                                    .length(item.dimensionsCm().length())
                                    .height(item.dimensionsCm().height())
                                    .depth(item.dimensionsCm().width()) // using width for depth
                                    .build() : null)
                            .build())
                    .toList());
        }

        HttpEntity<UberCreateOrderRequest> httpRequest = new HttpEntity<>(uberRequest, headers);

        try {
            // Build the full API URL from .env values
            String fullUrl = baseUrl + deliveriesEndpoint.replace("{customerId}", customerId);

            ResponseEntity<UberCreateOrderResponse> response = restTemplate.postForEntity(
                    fullUrl,
                    httpRequest,
                    UberCreateOrderResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UberCreateOrderResponse orderResponse = response.getBody();
                log.info("Uber Direct order created successfully. Order ID: {}", orderResponse.getId());
                return new CreateOrderResponse(orderResponse.getId());
            } else {
                log.error("Failed to create Uber Direct order: HTTP {}", response.getStatusCode());
                throw new RuntimeException("Failed to create Uber Direct order: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Exception while creating Uber Direct order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Uber Direct order: " + e.getMessage());
        }
    }
}
