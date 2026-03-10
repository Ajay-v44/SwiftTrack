package com.swifttrack.OrderService.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.FeignClient.ProviderInterface;
import com.swifttrack.FeignClient.TenantInterface;
import com.swifttrack.FeignClient.BillingInterface;
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
import com.swifttrack.dto.tenantDto.TenantDeliveryConf;
import com.swifttrack.dto.billingDto.QuoteRequest;

import jakarta.transaction.Transactional;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.TokenResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;

@Service
@Transactional
public class OrderServices {
        ProviderInterface providerInterface;
        MapInterface mapInterface;
        TenantInterface tenantInterface;
        BillingInterface billingInterface;
        OrderRepository orderRepository;
        RestTemplate restTemplate;
        OrderQuoteSessionRepository orderQuoteSessionRepository;
        OrderQuoteRepository orderQuoteRepository;
        AuthInterface authInterface;
        OrderTrackingStateRepository orderTrackingStateRepository;
        private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

        @Value("${ML_SERVICES_URL:}")
        String mlServicesUrl;
        @Value("${ML_MODEL_THRESHOLD:0.0}")
        String mlThreshold;

        public OrderServices(ProviderInterface providerInterface, MapInterface mapInterface,
                        TenantInterface tenantInterface, BillingInterface billingInterface,
                        OrderRepository orderRepository, RestTemplate restTemplate,
                        OrderQuoteSessionRepository orderQuoteSessionRepository,
                        OrderQuoteRepository orderQuoteRepository, AuthInterface authInterface,
                        OrderTrackingStateRepository orderTrackingStateRepository,
                        org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
                this.providerInterface = providerInterface;
                this.mapInterface = mapInterface;
                this.tenantInterface = tenantInterface;
                this.billingInterface = billingInterface;
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
                        if (tokenResponse == null || tokenResponse.tenantId().isEmpty()) {
                                throw new RuntimeException("Tenant details are required to fetch quote");
                        }

                        OrderQuoteSession orderQuoteSession = new OrderQuoteSession();
                        orderQuoteSession.setTenantId(tokenResponse.tenantId().get());
                        orderQuoteSession.setExpiresAt(LocalDateTime.now().plusHours(1));
                        orderQuoteSession.setStatus(QuoteSessionStatus.ACTIVE);
                        orderQuoteSession.setCreatedAt(LocalDateTime.now());
                        orderQuoteSession = orderQuoteSessionRepository.save(orderQuoteSession);

                        List<TenantDeliveryConf> deliveryConfig = Optional
                                        .ofNullable(tenantInterface.getTenantDeliveryConfiguration(token))
                                        .map(ResponseEntity::getBody)
                                        .orElse(Collections.emptyList())
                                        .stream()
                                        .sorted(Comparator.comparingInt(TenantDeliveryConf::priority))
                                        .toList();

                        if (deliveryConfig.isEmpty()) {
                                deliveryConfig = List.of(
                                                new TenantDeliveryConf("EXTERNAL_PROVIDERS", 1),
                                                new TenantDeliveryConf("LOCAL_DRIVERS", 2),
                                                new TenantDeliveryConf("TENANT_DRIVERS", 3));
                        }

                        boolean hasExternalOption = deliveryConfig.stream()
                                        .anyMatch(config -> "EXTERNAL_PROVIDERS".equalsIgnoreCase(config.optionType()));

                        CompletableFuture<ApiResponse<DistanceResult>> distanceFuture = CompletableFuture
                                        .supplyAsync(() -> mapInterface.calculateDistance(quoteInput.pickupLat(),
                                                        quoteInput.pickupLng(),
                                                        quoteInput.dropoffLat(), quoteInput.dropoffLng()));

                        CompletableFuture<ApiResponse<NormalizedLocation>> dropoffFuture = hasExternalOption
                                        ? CompletableFuture.supplyAsync(
                                                        () -> mapInterface.reverseGeocode(quoteInput.dropoffLat(),
                                                                        quoteInput.dropoffLng()))
                                        : CompletableFuture.completedFuture(null);

                        CompletableFuture<ApiResponse<NormalizedLocation>> pickupFuture = hasExternalOption
                                        ? CompletableFuture.supplyAsync(
                                                        () -> mapInterface.reverseGeocode(quoteInput.pickupLat(),
                                                                        quoteInput.pickupLng()))
                                        : CompletableFuture.completedFuture(null);

                        CompletableFuture<List<GetProviders>> providersFuture = hasExternalOption
                                        ? CompletableFuture.supplyAsync(() -> providerInterface.getTenantProviders(token))
                                        : CompletableFuture.completedFuture(Collections.emptyList());

                        CompletableFuture.allOf(distanceFuture, dropoffFuture, pickupFuture, providersFuture)
                                        .get(5, TimeUnit.SECONDS);

                        ApiResponse<DistanceResult> distance = distanceFuture.get();
                        ApiResponse<NormalizedLocation> dropoffLocation = dropoffFuture.get();
                        ApiResponse<NormalizedLocation> pickupLocation = pickupFuture.get();
                        List<GetProviders> providers = providersFuture.get();

                        BigDecimal distanceKm = BigDecimal.valueOf(distance.getData().getDistanceMeters())
                                        .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

                        for (TenantDeliveryConf config : deliveryConfig) {
                                String selectedType = config.optionType().toUpperCase();
                                try {
                                        if ("EXTERNAL_PROVIDERS".equals(selectedType)) {
                                                ExternalProviderSelection selection = selectExternalProvider(token,
                                                                quoteInput, providers, distance, pickupLocation,
                                                                dropoffLocation);
                                                if (selection == null) {
                                                        continue;
                                                }

                                                com.swifttrack.dto.billingDto.QuoteResponse billingQuote = billingInterface
                                                                .getQuote(new QuoteRequest(
                                                                                orderQuoteSession.getId(),
                                                                                Optional.of(BigDecimal.valueOf(
                                                                                                selection.quoteResponse()
                                                                                                                .price())),
                                                                                Optional.of(selection.providerCode()),
                                                                                Optional.empty(),
                                                                                tokenResponse.id(),
                                                                                selectedType))
                                                                .getBody();

                                                if (billingQuote == null || billingQuote.tenantCharge() == null) {
                                                        continue;
                                                }

                                                QuoteResponse finalQuoteResponse = new QuoteResponse(
                                                                billingQuote.tenantCharge().floatValue(),
                                                                selection.quoteResponse().currency());
                                                saveOrderQuote(orderQuoteSession, selectedType,
                                                                selection.providerCode(),
                                                                finalQuoteResponse.currency(),
                                                                billingQuote.tenantCharge(),
                                                                selection.aiScore());
                                                return new OrderQuoteResponse(finalQuoteResponse, orderQuoteSession.getId(),
                                                                selectedType, selection.providerCode());
                                        }

                                        if ("LOCAL_DRIVERS".equals(selectedType) || "TENANT_DRIVERS".equals(
                                                        selectedType)) {
                                                com.swifttrack.dto.billingDto.QuoteResponse billingQuote = billingInterface
                                                                .getQuote(new QuoteRequest(
                                                                                orderQuoteSession.getId(),
                                                                                Optional.empty(),
                                                                                Optional.empty(),
                                                                                Optional.of(distanceKm),
                                                                                tokenResponse.id(),
                                                                                selectedType))
                                                                .getBody();

                                                if (billingQuote == null || billingQuote.tenantCharge() == null) {
                                                        continue;
                                                }

                                                QuoteResponse finalQuoteResponse = new QuoteResponse(
                                                                billingQuote.tenantCharge().floatValue(),
                                                                "INR");
                                                saveOrderQuote(orderQuoteSession, selectedType, null,
                                                                finalQuoteResponse.currency(),
                                                                billingQuote.tenantCharge(),
                                                                null);
                                                return new OrderQuoteResponse(finalQuoteResponse, orderQuoteSession.getId(),
                                                                selectedType, null);
                                        }
                                } catch (Exception optionError) {
                                        System.err.println("Quote option failed for " + selectedType + ": "
                                                        + optionError.getMessage());
                                }
                        }

                        throw new RuntimeException("No quote could be generated for configured options");

                } catch (TimeoutException e) {
                        throw new RuntimeException("Timeout while fetching data from external services", e);
                } catch (Exception e) {
                        throw new RuntimeException("Error occurred while processing quote: " + e.getMessage(), e);
                }
        }

        private void saveOrderQuote(OrderQuoteSession orderQuoteSession, String selectedType, String providerCode,
                        String currency, BigDecimal tenantPrice, BigDecimal aiScore) {
                OrderQuote orderQuote = new OrderQuote();
                orderQuote.setProviderCode(providerCode);
                orderQuote.setSelectedType(selectedType);
                orderQuote.setQuoteSession(orderQuoteSession);
                orderQuote.setPrice(tenantPrice);
                orderQuote.setCurrency(currency);
                orderQuote.setAiScore(aiScore);
                orderQuote.setIsSelected(true);
                orderQuoteRepository.save(orderQuote);
        }

        private ExternalProviderSelection selectExternalProvider(String token, QuoteInput quoteInput,
                        List<GetProviders> providers, ApiResponse<DistanceResult> distance,
                        ApiResponse<NormalizedLocation> pickupLocation,
                        ApiResponse<NormalizedLocation> dropoffLocation) {
                List<ModelQuoteInput> modelQuoteInputList = buildProviderCandidates(providers, distance, pickupLocation,
                                dropoffLocation);
                if (modelQuoteInputList.isEmpty()) {
                        return null;
                }

                Prediction prediction = getMlPrediction(modelQuoteInputList);
                String providerCode = null;
                BigDecimal aiScore = null;

                if (prediction != null && prediction.getSuccessProbability() > parseMlThreshold()) {
                        providerCode = prediction.getProvider();
                        aiScore = BigDecimal.valueOf(prediction.getSuccessProbability());
                }

                if (providerCode == null || providerCode.isBlank()) {
                        ModelQuoteInput fallbackProvider = modelQuoteInputList.stream()
                                        .min(Comparator.comparingInt(ModelQuoteInput::provider_load))
                                        .orElse(modelQuoteInputList.get(0));
                        providerCode = fallbackProvider.provider();
                }

                QuoteResponse quoteResponse = providerInterface.getQuote(token, providerCode, quoteInput);
                return new ExternalProviderSelection(providerCode, quoteResponse, aiScore);
        }

        private List<ModelQuoteInput> buildProviderCandidates(List<GetProviders> providers,
                        ApiResponse<DistanceResult> distance,
                        ApiResponse<NormalizedLocation> pickupLocation,
                        ApiResponse<NormalizedLocation> dropoffLocation) {
                if (providers == null || pickupLocation == null || dropoffLocation == null || distance == null) {
                        return Collections.emptyList();
                }

                List<ModelQuoteInput> modelQuoteInputList = new ArrayList<>();
                String pickupState = pickupLocation.getData().getState();
                String dropoffState = dropoffLocation.getData().getState();
                String pickupCountry = pickupLocation.getData().getCountry();
                String dropoffCountry = dropoffLocation.getData().getCountry();

                if (dropoffState.equals(pickupState)) {
                        for (GetProviders provider : providers) {
                                if (provider.supportsHyperlocal() && provider.supportsIntercity()) {
                                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                                        modelQuoteInputList.add(new ModelQuoteInput(
                                                        provider.providerCode(),
                                                        distance.getData().getDistanceMeters(), 2,
                                                        count > 10, count));
                                }
                        }
                } else if (dropoffCountry.equals(pickupCountry)) {
                        for (GetProviders provider : providers) {
                                if (provider.supportsIntercity()) {
                                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                                        modelQuoteInputList.add(new ModelQuoteInput(
                                                        provider.providerCode(),
                                                        distance.getData().getDistanceMeters(), 2,
                                                        count > 10, count));
                                }
                        }
                } else {
                        throw new RuntimeException("International delivery Coming soon");
                }
                return modelQuoteInputList;
        }

        private Prediction getMlPrediction(List<ModelQuoteInput> modelQuoteInputList) {
                if (modelQuoteInputList.isEmpty() || mlServicesUrl == null || mlServicesUrl.isBlank()) {
                        return null;
                }

                String mlUrl = mlServicesUrl + "/predict/assignment";
                MLPredictionRequest mlRequest = new MLPredictionRequest(modelQuoteInputList);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("accept", "application/json");
                HttpEntity<MLPredictionRequest> requestEntity = new HttpEntity<>(mlRequest, headers);

                try {
                        ResponseEntity<MLPredictionResponse> response = restTemplate.postForEntity(
                                        mlUrl, requestEntity, MLPredictionResponse.class);
                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                                        && response.getBody().getPredictions() != null) {
                                return response.getBody().getPredictions().stream()
                                                .max(Comparator.comparing(Prediction::getSuccessProbability))
                                                .orElse(null);
                        }
                } catch (Exception e) {
                        System.err.println("Failed to call ML Service: " + e.getMessage());
                }
                return null;
        }

        private double parseMlThreshold() {
                        try {
                                return Double.parseDouble(mlThreshold);
                        } catch (Exception e) {
                                return 0.0;
                        }
        }

        private record ExternalProviderSelection(String providerCode, QuoteResponse quoteResponse, BigDecimal aiScore) {
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

        @CacheEvict(value = { "orderStatus", "orders" }, key = "#orderId")
        public Message cancelOrder(String token, UUID orderId, String providerCode) {
                TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
                if (orderRepository.findById(orderId, userDetails.id()).isPresent()) {
                        providerInterface.cancelOrder(token, orderId, providerCode);
                        orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);
                        return new Message("Order cancelled successfully");
                }
                throw new RuntimeException("Order not found");
        }

        @Cacheable(value = "driverOrders", key = "#request.orderIds().toString()")
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
                        Double pickupLat = (pickup != null && pickup.getLatitude() != null)
                                        ? pickup.getLatitude().doubleValue()
                                        : null;
                        Double pickupLng = (pickup != null && pickup.getLongitude() != null)
                                        ? pickup.getLongitude().doubleValue()
                                        : null;
                        Double dropoffLat = (dropoff != null && dropoff.getLatitude() != null)
                                        ? dropoff.getLatitude().doubleValue()
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

        @Cacheable(value = "orderStatus", key = "#orderId")
        public String getOrderStatus(String token, UUID orderId) {
                OrderTrackingState orderTrackingState = orderTrackingStateRepository.findById(orderId).orElse(null);
                if (orderTrackingState == null) {
                        return null;
                }
                return orderTrackingState.getCurrentStatus().name();
        }

        @Cacheable(value = "orders", key = "#orderId")
        public com.swifttrack.dto.orderDto.OrderDetailsResponse getOrderById(String token, UUID orderId) {
                TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

                OrderLocation pickup = order.getLocations().stream()
                                .filter(loc -> loc.getLocationType() == LocationType.PICKUP)
                                .findFirst().orElse(null);

                OrderLocation dropoff = order.getLocations().stream()
                                .filter(loc -> loc.getLocationType() == LocationType.DROP)
                                .findFirst().orElse(null);

                String city = (pickup != null) ? pickup.getCity() : null;
                String state = (pickup != null) ? pickup.getState() : null;
                Double pickupLat = (pickup != null && pickup.getLatitude() != null) ? pickup.getLatitude().doubleValue()
                                : null;
                Double pickupLng = (pickup != null && pickup.getLongitude() != null)
                                ? pickup.getLongitude().doubleValue()
                                : null;
                Double dropoffLat = (dropoff != null && dropoff.getLatitude() != null)
                                ? dropoff.getLatitude().doubleValue()
                                : null;
                Double dropoffLng = (dropoff != null && dropoff.getLongitude() != null)
                                ? dropoff.getLongitude().doubleValue()
                                : null;

                String orderStatus = order.getOrderStatus().name();

                return new com.swifttrack.dto.orderDto.OrderDetailsResponse(
                                order.getId(),
                                order.getCustomerReferenceId(),
                                orderStatus,
                                city,
                                state,
                                pickupLat,
                                pickupLng,
                                dropoffLat,
                                dropoffLng);
        }
}
