package com.swifttrack.OrderService.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.FeignClient.ProviderInterface;
import com.swifttrack.OrderService.dto.MLPredictionRequest;
import com.swifttrack.OrderService.dto.MLPredictionResponse;
import com.swifttrack.OrderService.dto.MLPredictionResponse.Prediction;
import com.swifttrack.OrderService.dto.ModelQuoteInput;
import com.swifttrack.OrderService.models.Order;
import com.swifttrack.OrderService.models.OrderLocation;
import com.swifttrack.OrderService.models.OrderQuote;
import com.swifttrack.OrderService.models.OrderQuoteSession;
import com.swifttrack.OrderService.models.OrderTrackingState;
import com.swifttrack.OrderService.models.enums.LocationType;
import com.swifttrack.OrderService.models.enums.OrderStatus;
import com.swifttrack.OrderService.models.enums.OrderType;
import com.swifttrack.OrderService.models.enums.QuoteSessionStatus;
import com.swifttrack.OrderService.repositories.OrderQuoteRepository;
import com.swifttrack.OrderService.repositories.OrderQuoteSessionRepository;
import com.swifttrack.OrderService.repositories.OrderRepository;
import com.swifttrack.OrderService.repositories.OrderTrackingStateRepository;
import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.dto.map.NormalizedLocation;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
import com.swifttrack.dto.orderDto.FinalCreateOrderResponse;
import com.swifttrack.dto.orderDto.GetOrdersForDriver;
import com.swifttrack.dto.orderDto.GetOrdersRequest;
import com.swifttrack.dto.orderDto.OrderQuoteResponse;
import com.swifttrack.events.OrderCreatedEvent;
import com.swifttrack.dto.GetProviders;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

import jakarta.transaction.Transactional;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.TokenResponse;

@Service
@Transactional
public class OrderServices {
    ProviderInterface providerInterface;
    MapInterface mapInterface;
    OrderRepository orderRepository;
    RestTemplate restTemplate;
    OrderQuoteSessionRepository orderQuoteSessionRepository;
    OrderQuoteRepository orderQuoteRepository;
    AuthInterface authInterface;
    OrderTrackingStateRepository orderTrackingStateRepository;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Value("")
    String mlServicesUrl;
    @Value("ML_MODEL_THRESHOLD")
    String mlThreshold;

    public OrderServices(ProviderInterface providerInterface, MapInterface mapInterface,
            OrderRepository orderRepository, RestTemplate restTemplate,
            OrderQuoteSessionRepository orderQuoteSessionRepository,
            OrderQuoteRepository orderQuoteRepository, AuthInterface authInterface,
            OrderTrackingStateRepository orderTrackingStateRepository,
            org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
        this.providerInterface = providerInterface;
        this.mapInterface = mapInterface;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.orderQuoteSessionRepository = orderQuoteSessionRepository;
        this.orderQuoteRepository = orderQuoteRepository;
        this.authInterface = authInterface;
        this.orderTrackingStateRepository = orderTrackingStateRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderQuoteResponse getQuote(String token, QuoteInput quoteInput) {
        try {
            TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();

            OrderQuoteSession orderQuoteSession = new OrderQuoteSession();
            orderQuoteSession.setTenantId(tokenResponse.tenantId().get());
            orderQuoteSession.setExpiresAt(LocalDateTime.now().plusHours(1));
            orderQuoteSession.setStatus(QuoteSessionStatus.ACTIVE);
            orderQuoteSession.setCreatedAt(LocalDateTime.now());
            orderQuoteSession = orderQuoteSessionRepository.save(orderQuoteSession);

            CompletableFuture<ApiResponse<NormalizedLocation>> dropoffFuture = CompletableFuture
                    .supplyAsync(() -> mapInterface.reverseGeocode(quoteInput.dropoffLat(), quoteInput.dropoffLng()));

            CompletableFuture<ApiResponse<NormalizedLocation>> pickupFuture = CompletableFuture
                    .supplyAsync(() -> mapInterface.reverseGeocode(quoteInput.pickupLat(), quoteInput.pickupLng()));

            CompletableFuture<ApiResponse<DistanceResult>> distanceFuture = CompletableFuture
                    .supplyAsync(() -> mapInterface.calculateDistance(quoteInput.pickupLat(), quoteInput.pickupLng(),
                            quoteInput.dropoffLat(), quoteInput.dropoffLng()));

            CompletableFuture<List<GetProviders>> providersFuture = CompletableFuture
                    .supplyAsync(() -> providerInterface.getTenantProviders(token));

            // Wait for all to complete with a timeout of 5 seconds
            CompletableFuture.allOf(dropoffFuture, pickupFuture, distanceFuture, providersFuture)
                    .get(5, TimeUnit.SECONDS);

            ApiResponse<NormalizedLocation> dropoffLocation = dropoffFuture.get();
            ApiResponse<NormalizedLocation> pickupLocation = pickupFuture.get();
            ApiResponse<DistanceResult> distance = distanceFuture.get();
            List<GetProviders> providers = providersFuture.get();

            List<ModelQuoteInput> modelQuoteInputList = new ArrayList<>();
            System.out.println("dropoffLocation: " + dropoffLocation.getData().getState());
            System.out.println("pickupLocation: " + pickupLocation.getData().getState());
            System.out.println("distance: " + distance.getData());
            // same state and local delivery
            if (dropoffLocation.getData().getState().equals(pickupLocation.getData().getState())) {
                for (GetProviders provider : providers) {
                    if (provider.supportsHyperlocal() && provider.supportsIntercity()) {
                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                        ModelQuoteInput modelQuoteInput = new ModelQuoteInput(provider.providerCode(),
                                distance.getData().getDistanceMeters(), 2, count > 10 ? true : false, count);
                        modelQuoteInputList.add(modelQuoteInput);
                    }
                }
            } else if (dropoffLocation.getData().getCountry().equals(pickupLocation.getData().getCountry())) {
                for (GetProviders provider : providers) {
                    if (provider.supportsIntercity()) {
                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                        ModelQuoteInput modelQuoteInput = new ModelQuoteInput(provider.providerName(),
                                distance.getData().getDistanceMeters(), 2, count > 10 ? true : false, count);
                        modelQuoteInputList.add(modelQuoteInput);
                    }
                }
            } else {
                throw new RuntimeException("International delivery Coming soon");
            }
            System.out.println(modelQuoteInputList);

            // Call ML Service for predictions
            String mlUrl = mlServicesUrl + "/predict/assignment";
            MLPredictionRequest mlRequest = new MLPredictionRequest(modelQuoteInputList);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<MLPredictionRequest> requestEntity = new HttpEntity<>(
                    mlRequest, headers);

            try {
                ResponseEntity<MLPredictionResponse> response = restTemplate
                        .postForEntity(mlUrl, requestEntity, MLPredictionResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    MLPredictionResponse predictionResponse = response.getBody();
                    System.out.println("ML Response: " + predictionResponse);
                    Prediction prediction = predictionResponse.getPredictions().stream()
                            .max(Comparator.comparing(Prediction::getSuccessProbability)).orElse(null);

                    if (prediction != null && prediction.getSuccessProbability() > Double.parseDouble(mlThreshold)) {
                        System.out.println("Selected provider via ML: " + prediction.getProvider());
                        QuoteResponse quoteResponse = providerInterface.getQuote(token, prediction.getProvider(),
                                quoteInput);
                        OrderQuote orderQuote = new OrderQuote();
                        orderQuote.setProviderCode(prediction.getProvider());
                        orderQuote.setQuoteSession(orderQuoteSession);
                        orderQuote.setPrice(BigDecimal.valueOf(quoteResponse.price()));
                        orderQuote.setCurrency("INR");
                        orderQuote.setAiScore(BigDecimal.valueOf(prediction.getSuccessProbability()));
                        orderQuote.setIsSelected(true);
                        orderQuoteRepository.save(orderQuote);
                        return new OrderQuoteResponse(quoteResponse, orderQuoteSession.getId());
                    } else {
                        System.out.println("ML output below threshold or empty. Falling back to default logic.");
                    }

                } else {
                    System.err.println("ML Service returned non-success status: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Failed to call ML Service: " + e.getMessage());
                // Non-blocking failure: proceed with default logic if ML fails
            }

            // Fallback logic: Select provider with minimum load (active orders)
            // If modelQuoteInputList is empty, this means no providers supported the route.
            if (modelQuoteInputList.isEmpty()) {
                throw new RuntimeException("No suitable providers found for this route.");
            }

            ModelQuoteInput fallbackProvider = modelQuoteInputList.stream()
                    .min(Comparator.comparingInt(ModelQuoteInput::provider_load))
                    .orElse(modelQuoteInputList.get(0));
            QuoteResponse quoteResponse = providerInterface.getQuote(token, fallbackProvider.provider(), quoteInput);
            OrderQuote orderQuote = new OrderQuote();
            orderQuote.setProviderCode(fallbackProvider.provider());
            orderQuote.setQuoteSession(orderQuoteSession);
            orderQuote.setPrice(BigDecimal.valueOf(quoteResponse.price()));
            orderQuote.setCurrency("INR");
            orderQuote.setAiScore(BigDecimal.valueOf(fallbackProvider.provider_load()));
            orderQuote.setIsSelected(true);
            orderQuoteRepository.save(orderQuote);
            System.out.println("Selected fallback provider: " + fallbackProvider.provider());
            return new OrderQuoteResponse(quoteResponse, orderQuoteSession.getId());

        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout while fetching data from external services", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while processing quote: " + e.getMessage(), e);
        }
    }

    public FinalCreateOrderResponse createOrder(String token, UUID quoteSessionId,
            CreateOrderRequest createOrderRequest) {
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        CreateOrderResponse response = providerInterface.createOrder(token, quoteSessionId, createOrderRequest);

        Order order = new Order();
        order.setTenantId(userDetails.tenantId().get());
        order.setCustomerReferenceId(createOrderRequest.orderReference());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setPaymentType(createOrderRequest.paymentType());
        order.setPaymentAmount(response.totalAmount());
        order.setSelectedProviderCode(response.providerCode());
        order.setProviderOrderId(response.orderId());
        order.setCreatedBy(userDetails.id());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setOrderType(OrderType.HYPERLOCAL);

        if (createOrderRequest.pickup() != null && createOrderRequest.pickup().address() != null) {
            order.setPickupLatitude(BigDecimal.valueOf(createOrderRequest.pickup().address().latitude()));
            order.setPickupLongitude(BigDecimal.valueOf(createOrderRequest.pickup().address().longitude()));
        }
        if (createOrderRequest.dropoff() != null && createOrderRequest.dropoff().address() != null) {
            order.setDropLatitude(BigDecimal.valueOf(createOrderRequest.dropoff().address().latitude()));
            order.setDropLongitude(BigDecimal.valueOf(createOrderRequest.dropoff().address().longitude()));
        }

        orderRepository.save(order);

        OrderCreatedEvent.OrderCreatedEventBuilder eventBuilder = OrderCreatedEvent
                .builder()
                .orderId(order.getId())
                .customerReferenceId(order.getCustomerReferenceId())
                .providerCode(order.getSelectedProviderCode())
                .amount(order.getPaymentAmount())
                .tenantId(order.getTenantId().toString())
                .createdAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().name())
                .pickupLat(order.getPickupLatitude().doubleValue())
                .pickupLng(order.getPickupLongitude().doubleValue())
                .dropoffLat(order.getDropLatitude().doubleValue())
                .dropoffLng(order.getDropLongitude().doubleValue());

        if (createOrderRequest.pickup() != null && createOrderRequest.pickup().address() != null) {
            var pAddr = createOrderRequest.pickup().address();
            eventBuilder.pickupCity(pAddr.city())
                    .pickupState(pAddr.state())
                    .pickupCountry(pAddr.country())
                    .pickupPincode(pAddr.pincode())
                    .pickupLocality(pAddr.locality());
        }

        if (createOrderRequest.dropoff() != null && createOrderRequest.dropoff().address() != null) {
            var dAddr = createOrderRequest.dropoff().address();
            eventBuilder.dropCity(dAddr.city())
                    .dropState(dAddr.state())
                    .dropCountry(dAddr.country())
                    .dropPincode(dAddr.pincode())
                    .dropLocality(dAddr.locality());
        }

        kafkaTemplate.send("order-created", eventBuilder.build());

        return new FinalCreateOrderResponse(order.getId(), response.providerCode(), response.totalAmount());
    }

    public Message cancelOrder(String token, UUID orderId, String providerCode) {
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        if (orderRepository.findById(orderId, userDetails.id()).isPresent()) {
            providerInterface.cancelOrder(token, orderId, providerCode);
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            return new Message("Order cancelled successfully");
        }
        throw new RuntimeException("Order not found");
    }

    public List<GetOrdersForDriver> getOrdersForDriver(String token,
            GetOrdersRequest request) {
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        List<Order> orders = orderRepository.findByIdIn(request.orderIds());
        List<GetOrdersForDriver> result = new ArrayList<>();

        for (Order order : orders) {
            OrderLocation pickup = order.getLocations().stream()
                    .filter(loc -> loc
                            .getLocationType() == LocationType.PICKUP)
                    .findFirst().orElse(null);

            OrderLocation dropoff = order.getLocations().stream()
                    .filter(loc -> loc.getLocationType() == LocationType.DROP)
                    .findFirst().orElse(null);

            String city = (pickup != null) ? pickup.getCity() : null;
            String state = (pickup != null) ? pickup.getState() : null;
            Double pickupLat = (pickup != null && pickup.getLatitude() != null) ? pickup.getLatitude().doubleValue()
                    : null;
            Double pickupLng = (pickup != null && pickup.getLongitude() != null) ? pickup.getLongitude().doubleValue()
                    : null;
            Double dropoffLat = (dropoff != null && dropoff.getLatitude() != null) ? dropoff.getLatitude().doubleValue()
                    : null;
            Double dropoffLng = (dropoff != null && dropoff.getLongitude() != null)
                    ? dropoff.getLongitude().doubleValue()
                    : null;

            String orderStatus = order.getOrderStatus().name();

            result.add(new GetOrdersForDriver(
                    order.getId(),
                    order.getCustomerReferenceId(),
                    orderStatus,
                    city,
                    state,
                    pickupLat,
                    pickupLng,
                    dropoffLat,
                    dropoffLng));
        }
        return result;
    }

    public String getOrderStatus(String token, UUID orderId) {
        OrderTrackingState orderTrackingState = orderTrackingStateRepository.findById(orderId).orElse(null);
        if (orderTrackingState == null) {
            return null;
        }
        return orderTrackingState.getCurrentStatus().name();
    }
}
