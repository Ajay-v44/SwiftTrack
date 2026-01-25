package com.swifttrack.ProviderService.adapters.porter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.swifttrack.ProviderService.adapters.porter.dto.PorterCreateOrderRequest;
import com.swifttrack.ProviderService.adapters.porter.dto.PorterCreateOrderResponse;
import com.swifttrack.ProviderService.adapters.porter.dto.PorterGetQuoteInp;
import com.swifttrack.ProviderService.adapters.porter.dto.PorterQuoteResponse;
import com.swifttrack.ProviderService.conf.DeliveryProvider;
import com.swifttrack.ProviderService.utils.GetUUID;

import java.util.List;
import java.util.Random;
import java.math.BigDecimal;
import java.util.ArrayList;

import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;
import com.swifttrack.http.ExternalApiClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PorterAdapter implements DeliveryProvider {
    private final ExternalApiClient externalApiClient;
    private final RestTemplate restTemplate;
    private final GetUUID getUUID;

    @Value("${PORTER_API_URL}")
    private String porterApiUrl;
    @Value("${PORTER_API_KEY}")
    private String porterApiKey;

    @Value("${ENV}")
    private String env;

    public PorterAdapter(ExternalApiClient externalApiClient, RestTemplate restTemplate, GetUUID getUUID) {
        this.externalApiClient = externalApiClient;
        this.restTemplate = restTemplate;
        this.getUUID = getUUID;
    }

    @Override
    public QuoteResponse getQuote(QuoteInput quoteInput) {
        try {
            // Build request object
            PorterGetQuoteInp porterGetQuoteInp = new PorterGetQuoteInp();
            porterGetQuoteInp.setPickupDetails(
                    new PorterGetQuoteInp.PickupDetails(quoteInput.pickupLat(), quoteInput.pickupLng()));
            porterGetQuoteInp.setDropDetails(
                    new PorterGetQuoteInp.DropDetails(quoteInput.dropoffLat(), quoteInput.dropoffLng()));

            // Create customer with mobile details
            PorterGetQuoteInp.Customer.Mobile mobile = new PorterGetQuoteInp.Customer.Mobile("+91", "9999999999");
            porterGetQuoteInp.setCustomer(new PorterGetQuoteInp.Customer("test", mobile));

            String fullUrl = porterApiUrl + "/v1/get_quote";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", porterApiKey);

            HttpEntity<PorterGetQuoteInp> request = new HttpEntity<>(porterGetQuoteInp, headers);
            ResponseEntity<PorterQuoteResponse> response = restTemplate.postForEntity(fullUrl, request,
                    PorterQuoteResponse.class);

            // Check if response is successful
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PorterQuoteResponse quoteResponse = response.getBody();

                // Look for 2 Wheeler vehicle type
                if (quoteResponse.getVehicles() != null) {
                    for (PorterQuoteResponse.Vehicle vehicle : quoteResponse.getVehicles()) {
                        if ("2 Wheeler".equals(vehicle.getType()) && vehicle.getFare() != null) {
                            // Convert minor_amount from paise to rupees
                            float priceInRupees = vehicle.getFare().getMinorAmount() / 100.0f;
                            String currency = vehicle.getFare().getCurrency() != null ? vehicle.getFare().getCurrency()
                                    : "INR";
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

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        try {
            if (env.equals("dev")) {
                Random random = new Random();
                return new CreateOrderResponse("PD-" + random.nextInt(1000), "PORTER", new BigDecimal(120));
            }
            String fullUrl = porterApiUrl + "/v1/orders/create";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", porterApiKey);

            PorterCreateOrderRequest porterRequest = new PorterCreateOrderRequest();
            porterRequest.setRequestId(createOrderRequest.idempotencyKey());

            List<PorterCreateOrderRequest.Instruction> instructions = new ArrayList<>();

            instructions.add(new PorterCreateOrderRequest.Instruction("text",
                    createOrderRequest.deliveryInstructions()));
            porterRequest.setDeliveryInstructions(new PorterCreateOrderRequest.DeliveryInstructions(instructions));

            PorterCreateOrderRequest.ContactDetails pickupContact = new PorterCreateOrderRequest.ContactDetails(
                    createOrderRequest.pickup().contact().name(),
                    createOrderRequest.pickup().contact().phone());
            PorterCreateOrderRequest.Address pickupAddress = new PorterCreateOrderRequest.Address(
                    createOrderRequest.pickup().address().line1() + " , "
                            + createOrderRequest.pickup().address().line2(),
                    createOrderRequest.pickup().address().line1(),
                    createOrderRequest.pickup().address().latitude(),
                    createOrderRequest.pickup().address().longitude(),
                    pickupContact);
            porterRequest.setPickupDetails(new PorterCreateOrderRequest.LocationDetails(pickupAddress));

            PorterCreateOrderRequest.ContactDetails dropContact = new PorterCreateOrderRequest.ContactDetails(
                    createOrderRequest.dropoff().contact().name(),
                    createOrderRequest.dropoff().contact().phone());
            PorterCreateOrderRequest.Address dropAddress = new PorterCreateOrderRequest.Address(
                    null,
                    createOrderRequest.dropoff().address().line1(),
                    createOrderRequest.dropoff().address().latitude(),
                    createOrderRequest.dropoff().address().longitude(),
                    dropContact);
            porterRequest.setDropDetails(new PorterCreateOrderRequest.LocationDetails(dropAddress));

            porterRequest.setAdditionalComments(createOrderRequest.orderReference());
            return new CreateOrderResponse("PR-" + getUUID.getUUID(), "PORTER", new BigDecimal(120));

            // HttpEntity<PorterCreateOrderRequest> request = new
            // HttpEntity<>(porterRequest, headers);
            // ResponseEntity<PorterCreateOrderResponse> response =
            // restTemplate.postForEntity(fullUrl, request,
            // PorterCreateOrderResponse.class);
            // log.info("Porter order created successfully: {}", response.getBody());
            // if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            // {
            // return new CreateOrderResponse(response.getBody().getOrderId(), "PORTER", new
            // BigDecimal(120));
            // } else {
            // log.error("Failed to create Porter order: HTTP {}",
            // response.getStatusCode());
            // throw new RuntimeException("Failed to create Porter order: HTTP " +
            // response.getStatusCode());
            // }
        } catch (Exception e) {
            log.error("Exception while creating Porter order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Porter order: " + e.getMessage());
        }
    }

    @Override
    public Message cancelOrder(String orderId) {
        try {
            if ((env.equals("dev"))) {
                return new Message("Order cancelled successfully");
            }
            String fullUrl = porterApiUrl + "/v1/orders/" + orderId + "/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", porterApiKey);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<?> response = restTemplate.postForEntity(fullUrl, request, Object.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new Message("Order cancelled successfully");
            } else {
                log.error("Failed to cancel Porter order: HTTP {}", response.getStatusCode());
                throw new RuntimeException("Failed to cancel Porter order: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Exception while canceling Porter order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cancel Porter order: " + e.getMessage());
        }
    }
}