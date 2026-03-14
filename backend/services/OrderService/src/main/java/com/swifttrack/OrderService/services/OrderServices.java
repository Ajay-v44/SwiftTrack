package com.swifttrack.OrderService.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import com.swifttrack.OrderService.repositories.OrderLocationRepository;
import com.swifttrack.OrderService.repositories.OrderRepository;
import com.swifttrack.OrderService.repositories.OrderTrackingStateRepository;
import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.dto.map.NormalizedLocation;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
import com.swifttrack.dto.orderDto.DeliveryOptionQuote;
import com.swifttrack.dto.orderDto.DeliveryOptionsQuoteResponse;
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
import com.swifttrack.dto.billingDto.BindQuoteOrderRequest;
import com.swifttrack.enums.BillingAndSettlement.BookingChannel;
import com.swifttrack.events.InternalDriverAssignmentEvent;
import com.swifttrack.events.UserCanceledOrderEvent;

import jakarta.transaction.Transactional;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.TokenResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class OrderServices {
        ProviderInterface providerInterface;
        MapInterface mapInterface;
        TenantInterface tenantInterface;
        BillingInterface billingInterface;
        ObjectMapper objectMapper;
        OrderRepository orderRepository;
        OrderLocationRepository orderLocationRepository;
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
                        ObjectMapper objectMapper, OrderRepository orderRepository, OrderLocationRepository orderLocationRepository,
                        RestTemplate restTemplate,
                        OrderQuoteSessionRepository orderQuoteSessionRepository,
                        OrderQuoteRepository orderQuoteRepository, AuthInterface authInterface,
                        OrderTrackingStateRepository orderTrackingStateRepository,
                        org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
                this.providerInterface = providerInterface;
                this.mapInterface = mapInterface;
                this.tenantInterface = tenantInterface;
                this.billingInterface = billingInterface;
                this.objectMapper = objectMapper;
                this.orderRepository = orderRepository;
                this.orderLocationRepository = orderLocationRepository;
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
                        return buildQuote(token, quoteInput, BookingChannel.TENANT, tokenResponse.tenantId().get(),
                                        tokenResponse.id(), null, deliveryConfig, providerInterface.getTenantProviders(token));

                } catch (Exception e) {
                        throw new RuntimeException("Error occurred while processing quote: " + e.getMessage(), e);
                }
        }

        public DeliveryOptionsQuoteResponse getConsumerQuote(String token, QuoteInput quoteInput) {
                TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
                if (tokenResponse == null || tokenResponse.id() == null) {
                        throw new RuntimeException("Authenticated user is required to fetch consumer quote");
                }
                return buildSelectableQuotes(token, quoteInput, BookingChannel.CONSUMER, tokenResponse.id(), null);
        }

        public DeliveryOptionsQuoteResponse getGuestQuote(QuoteInput quoteInput) {
                String guestAccessToken = UUID.randomUUID().toString();
                return buildSelectableQuotes(null, quoteInput, BookingChannel.GUEST, null, guestAccessToken);
        }

        private DeliveryOptionsQuoteResponse buildSelectableQuotes(String token, QuoteInput quoteInput,
                        BookingChannel bookingChannel, UUID ownerUserId, String guestAccessToken) {
                try {
                        OrderQuoteSession orderQuoteSession = new OrderQuoteSession();
                        orderQuoteSession.setTenantId(null);
                        orderQuoteSession.setOwnerUserId(ownerUserId);
                        orderQuoteSession.setBookingChannel(bookingChannel);
                        orderQuoteSession.setGuestAccessToken(guestAccessToken);
                        orderQuoteSession.setExpiresAt(LocalDateTime.now().plusHours(1));
                        orderQuoteSession.setStatus(QuoteSessionStatus.ACTIVE);
                        orderQuoteSession.setCreatedAt(LocalDateTime.now());
                        orderQuoteSession = orderQuoteSessionRepository.save(orderQuoteSession);

                        ApiResponse<DistanceResult> distance = mapInterface.calculateDistance(
                                        quoteInput.pickupLat(), quoteInput.pickupLng(),
                                        quoteInput.dropoffLat(), quoteInput.dropoffLng());
                        BigDecimal distanceKm = BigDecimal.valueOf(distance.getData().getDistanceMeters())
                                        .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

                        ApiResponse<NormalizedLocation> pickupLocation = mapInterface.reverseGeocode(
                                        quoteInput.pickupLat(), quoteInput.pickupLng());
                        ApiResponse<NormalizedLocation> dropoffLocation = mapInterface.reverseGeocode(
                                        quoteInput.dropoffLat(), quoteInput.dropoffLng());

                        List<DeliveryOptionQuote> options = new ArrayList<>();

                        com.swifttrack.dto.billingDto.QuoteResponse driverBillingQuote = billingInterface
                                        .getQuote(new QuoteRequest(
                                                        orderQuoteSession.getId(),
                                                        Optional.empty(),
                                                        Optional.empty(),
                                                        Optional.of(distanceKm),
                                                        ownerUserId,
                                                        "LOCAL_DRIVERS"))
                                        .getBody();
                        if (driverBillingQuote != null && driverBillingQuote.tenantCharge() != null) {
                                OrderQuote driverQuote = saveOrderQuote(orderQuoteSession, "LOCAL_DRIVERS", null, "INR",
                                                driverBillingQuote.tenantCharge(), null, null, false);
                                options.add(new DeliveryOptionQuote(
                                                driverQuote.getId(),
                                                "SWIFTTRACK_DRIVER",
                                                "LOCAL_DRIVERS",
                                                null,
                                                new QuoteResponse(driverBillingQuote.tenantCharge().floatValue(), "INR")));
                        }

                        List<ModelQuoteInput> providerCandidates = buildProviderCandidates(providerInterface.getProviders(),
                                        distance, pickupLocation, dropoffLocation);
                        for (ModelQuoteInput candidate : providerCandidates) {
                                try {
                                        QuoteResponse providerQuote = token != null && !token.isBlank()
                                                        ? providerInterface.getQuote(token, candidate.provider(), quoteInput)
                                                        : providerInterface.getQuoteInternal(candidate.provider(), quoteInput);
                                        if (providerQuote == null) {
                                                continue;
                                        }
                                        com.swifttrack.dto.billingDto.QuoteResponse billingQuote = billingInterface
                                                        .getQuote(new QuoteRequest(
                                                                        orderQuoteSession.getId(),
                                                                        Optional.of(BigDecimal.valueOf(providerQuote.price())),
                                                                        Optional.of(candidate.provider()),
                                                                        Optional.empty(),
                                                                        ownerUserId,
                                                                        "EXTERNAL_PROVIDERS"))
                                                        .getBody();
                                        if (billingQuote == null || billingQuote.tenantCharge() == null) {
                                                continue;
                                        }
                                        OrderQuote savedQuote = saveOrderQuote(orderQuoteSession, "EXTERNAL_PROVIDERS",
                                                        candidate.provider(), providerQuote.currency(),
                                                        billingQuote.tenantCharge(), null, providerQuote.quoteId(), false);
                                        options.add(new DeliveryOptionQuote(
                                                        savedQuote.getId(),
                                                        candidate.provider(),
                                                        "EXTERNAL_PROVIDERS",
                                                        candidate.provider(),
                                                        new QuoteResponse(
                                                                        billingQuote.tenantCharge().floatValue(),
                                                                        providerQuote.currency(),
                                                                        providerQuote.quoteId())));
                                } catch (Exception providerError) {
                                        System.err.println("Quote option failed for provider " + candidate.provider() + ": "
                                                        + providerError.getMessage());
                                }
                        }

                        if (options.isEmpty()) {
                                throw new RuntimeException("No delivery options available");
                        }

                        return new DeliveryOptionsQuoteResponse(orderQuoteSession.getId(), options, guestAccessToken);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to build selectable quotes: " + e.getMessage(), e);
                }
        }

        private OrderQuoteResponse buildQuote(String token, QuoteInput quoteInput, BookingChannel bookingChannel,
                        UUID tenantId, UUID ownerUserId, String guestAccessToken,
                        List<TenantDeliveryConf> deliveryConfig, List<GetProviders> providers) {
                try {
                        OrderQuoteSession orderQuoteSession = new OrderQuoteSession();
                        orderQuoteSession.setTenantId(tenantId);
                        orderQuoteSession.setOwnerUserId(ownerUserId);
                        orderQuoteSession.setBookingChannel(bookingChannel);
                        orderQuoteSession.setGuestAccessToken(guestAccessToken);
                        orderQuoteSession.setExpiresAt(LocalDateTime.now().plusHours(1));
                        orderQuoteSession.setStatus(QuoteSessionStatus.ACTIVE);
                        orderQuoteSession.setCreatedAt(LocalDateTime.now());
                        orderQuoteSession = orderQuoteSessionRepository.save(orderQuoteSession);

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

                        CompletableFuture.allOf(distanceFuture, dropoffFuture, pickupFuture).get(5, TimeUnit.SECONDS);

                        ApiResponse<DistanceResult> distance = distanceFuture.get();
                        ApiResponse<NormalizedLocation> dropoffLocation = dropoffFuture.get();
                        ApiResponse<NormalizedLocation> pickupLocation = pickupFuture.get();
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
                                                                                ownerUserId,
                                                                                selectedType))
                                                                .getBody();

                                                if (billingQuote == null || billingQuote.tenantCharge() == null) {
                                                        continue;
                                                }

                                                QuoteResponse finalQuoteResponse = new QuoteResponse(
                                                                billingQuote.tenantCharge().floatValue(),
                                                                selection.quoteResponse().currency(),
                                                                selection.quoteResponse().quoteId());
                                                saveOrderQuote(orderQuoteSession, selectedType,
                                                                selection.providerCode(),
                                                                finalQuoteResponse.currency(),
                                                                billingQuote.tenantCharge(),
                                                                selection.aiScore(),
                                                                selection.quoteResponse().quoteId(),
                                                                true);
                                                return new OrderQuoteResponse(finalQuoteResponse, orderQuoteSession.getId(),
                                                                selectedType, selection.providerCode(),
                                                                selection.quoteResponse().quoteId());
                                        }

                                        if ("LOCAL_DRIVERS".equals(selectedType)
                                                        || "TENANT_DRIVERS".equals(selectedType)) {
                                                com.swifttrack.dto.billingDto.QuoteResponse billingQuote = billingInterface
                                                                .getQuote(new QuoteRequest(
                                                                                orderQuoteSession.getId(),
                                                                                Optional.empty(),
                                                                                Optional.empty(),
                                                                                Optional.of(distanceKm),
                                                                                ownerUserId,
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
                                                                null,
                                                                null,
                                                                true);
                                                return new OrderQuoteResponse(finalQuoteResponse, orderQuoteSession.getId(),
                                                                selectedType, null, null);
                                        }
                                } catch (Exception optionError) {
                                        System.err.println("Quote option failed for " + selectedType + ": "
                                                        + optionError.getMessage());
                                }
                        }

                        throw new RuntimeException("No quote could be generated for configured options");
                } catch (Exception e) {
                        throw new RuntimeException("Failed to build quote: " + e.getMessage(), e);
                }
        }

        private OrderQuote saveOrderQuote(OrderQuoteSession orderQuoteSession, String selectedType, String providerCode,
                        String currency, BigDecimal tenantPrice, BigDecimal aiScore, String quoteId, boolean isSelected) {
                OrderQuote orderQuote = new OrderQuote();
                orderQuote.setProviderCode(providerCode);
                orderQuote.setQuoteId(quoteId);
                orderQuote.setSelectedType(selectedType);
                orderQuote.setQuoteSession(orderQuoteSession);
                orderQuote.setPrice(tenantPrice);
                orderQuote.setCurrency(currency);
                orderQuote.setAiScore(aiScore);
                orderQuote.setIsSelected(isSelected);
                return orderQuoteRepository.save(orderQuote);
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

                QuoteResponse quoteResponse = token != null && !token.isBlank()
                                ? providerInterface.getQuote(token, providerCode, quoteInput)
                                : providerInterface.getQuoteInternal(providerCode, quoteInput);
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

                if (Objects.equals(dropoffState, pickupState)) {
                        for (GetProviders provider : providers) {
                                if (provider.supportsHyperlocal()) {
                                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                                        modelQuoteInputList.add(new ModelQuoteInput(
                                                        provider.providerCode(),
                                                        distance.getData().getDistanceMeters(), 2,
                                                        count > 10, count));
                                }
                        }
                } else if (Objects.equals(dropoffCountry, pickupCountry)) {
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
                if (userDetails == null || userDetails.tenantId().isEmpty()) {
                        throw new RuntimeException("Invalid user or tenant context");
                }
                return createOrderForContext(token, quoteSessionId, null, createOrderRequest, BookingChannel.TENANT,
                                userDetails.tenantId().get(), userDetails.id(), null);
        }

        public FinalCreateOrderResponse createConsumerOrder(String token, UUID quoteSessionId, UUID selectedQuoteId,
                        CreateOrderRequest createOrderRequest) {
                TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
                if (userDetails == null || userDetails.id() == null) {
                        throw new RuntimeException("Authenticated user is required");
                }
                return createOrderForContext(token, quoteSessionId, selectedQuoteId, createOrderRequest,
                                BookingChannel.CONSUMER, null, userDetails.id(), null);
        }

        public FinalCreateOrderResponse createGuestOrder(UUID quoteSessionId, String guestAccessToken, UUID selectedQuoteId,
                        CreateOrderRequest createOrderRequest) {
                if (guestAccessToken == null || guestAccessToken.isBlank()) {
                        throw new RuntimeException("guestAccessToken is required");
                }
                return createOrderForContext(null, quoteSessionId, selectedQuoteId, createOrderRequest,
                                BookingChannel.GUEST, null, null, guestAccessToken);
        }

        private FinalCreateOrderResponse createOrderForContext(String token, UUID quoteSessionId, UUID selectedQuoteId,
                        CreateOrderRequest createOrderRequest, BookingChannel bookingChannel,
                        UUID tenantId, UUID ownerUserId, String guestAccessToken) {
                OrderQuoteSession quoteSession = orderQuoteSessionRepository
                                .findActiveSessionById(quoteSessionId, LocalDateTime.now())
                                .orElseThrow(() -> new RuntimeException("Quote session not found or expired"));
                validateQuoteSessionOwnership(quoteSession, bookingChannel, tenantId, ownerUserId, guestAccessToken);
                OrderQuote selectedQuote = resolveSelectedQuote(quoteSessionId, selectedQuoteId, bookingChannel);

                String selectedType = Optional.ofNullable(selectedQuote.getSelectedType())
                                .orElse("EXTERNAL_PROVIDERS")
                                .toUpperCase();

                CreateOrderRequest normalizedRequest = enrichCreateOrderRequest(createOrderRequest, selectedQuote,
                                quoteSession, bookingChannel);

                Order order = new Order();
                order.setTenantId(tenantId);
                order.setOwnerUserId(ownerUserId);
                order.setBookingChannel(bookingChannel);
                order.setGuestAccessToken(quoteSession.getGuestAccessToken());
                order.setCustomerReferenceId(normalizedRequest.orderReference());
                order.setOrderStatus(OrderStatus.CREATED);
                order.setPaymentType(normalizedRequest.paymentType());
                order.setPaymentAmount(selectedQuote.getPrice());
                order.setSelectedProviderCode(selectedQuote.getProviderCode());
                order.setQuoteSessionId(quoteSession.getId());
                order.setSelectedType(selectedType);
                order.setCreatedBy(ownerUserId);
                order.setCreatedAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                order.setOrderType(OrderType.HYPERLOCAL);
                try {
                        order.setCreateOrderPayload(objectMapper.writeValueAsString(normalizedRequest));
                } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize create order payload", e);
                }

                if (normalizedRequest.pickup() != null && normalizedRequest.pickup().address() != null) {
                        order.setPickupLatitude(BigDecimal.valueOf(normalizedRequest.pickup().address().latitude()));
                        order.setPickupLongitude(BigDecimal.valueOf(normalizedRequest.pickup().address().longitude()));
                }
                if (normalizedRequest.dropoff() != null && normalizedRequest.dropoff().address() != null) {
                        order.setDropLatitude(BigDecimal.valueOf(normalizedRequest.dropoff().address().latitude()));
                        order.setDropLongitude(BigDecimal.valueOf(normalizedRequest.dropoff().address().longitude()));
                }

                orderRepository.save(order);

                if ("EXTERNAL_PROVIDERS".equals(selectedType)) {
                        CreateOrderResponse providerResponse = createExternalProviderOrder(token, quoteSessionId,
                                        selectedQuote, normalizedRequest, bookingChannel);
                        order.setPaymentAmount(providerResponse.totalAmount());
                        order.setSelectedProviderCode(providerResponse.providerCode());
                        order.setProviderOrderId(providerResponse.orderId());
                        orderRepository.save(order);
                } else {
                        saveOrderLocations(order, normalizedRequest);
                        List<String> deliveryOptions = getDeliveryOptionsForOrder(token, order, bookingChannel);
                        int optionIndex = Math.max(deliveryOptions.indexOf(selectedType), 0);
                        publishInternalAssignmentAfterCommit(order, selectedType, null, deliveryOptions, optionIndex,
                                        null);
                }
                billingInterface.bindOrder(new BindQuoteOrderRequest(quoteSessionId, order.getId()));

                if ("EXTERNAL_PROVIDERS".equals(selectedType)) {
                        OrderCreatedEvent.OrderCreatedEventBuilder eventBuilder = OrderCreatedEvent
                                        .builder()
                                        .orderId(order.getId())
                                        .customerReferenceId(order.getCustomerReferenceId())
                                        .providerCode(order.getSelectedProviderCode())
                                        .amount(order.getPaymentAmount())
                                        .tenantId(order.getTenantId() != null ? order.getTenantId().toString() : null)
                                        .createdAt(order.getCreatedAt())
                                        .orderStatus(order.getOrderStatus().name())
                                        .pickupLat(order.getPickupLatitude().doubleValue())
                                        .pickupLng(order.getPickupLongitude().doubleValue())
                                        .dropoffLat(order.getDropLatitude().doubleValue())
                                        .dropoffLng(order.getDropLongitude().doubleValue());

                        if (normalizedRequest.pickup() != null && normalizedRequest.pickup().address() != null) {
                                var pAddr = normalizedRequest.pickup().address();
                                eventBuilder.pickupCity(pAddr.city())
                                                .pickupState(pAddr.state())
                                                .pickupCountry(pAddr.country())
                                                .pickupPincode(pAddr.pincode())
                                                .pickupLocality(pAddr.locality());
                        }

                        if (normalizedRequest.dropoff() != null && normalizedRequest.dropoff().address() != null) {
                                var dAddr = normalizedRequest.dropoff().address();
                                eventBuilder.dropCity(dAddr.city())
                                                .dropState(dAddr.state())
                                                .dropCountry(dAddr.country())
                                                .dropPincode(dAddr.pincode())
                                                .dropLocality(dAddr.locality());
                        }

                        kafkaTemplate.send("order-created", eventBuilder.build());
                }

                return new FinalCreateOrderResponse(order.getId(), order.getSelectedProviderCode(),
                                order.getPaymentAmount(), resolveChoiceCode(selectedQuote));
        }

        private OrderQuote resolveSelectedQuote(UUID quoteSessionId, UUID selectedQuoteId, BookingChannel bookingChannel) {
                if (bookingChannel == BookingChannel.CONSUMER || bookingChannel == BookingChannel.GUEST) {
                        if (selectedQuoteId == null) {
                                throw new RuntimeException("selectedQuoteId is required");
                        }
                        OrderQuote selectedQuote = orderQuoteRepository.findByIdAndQuoteSessionId(selectedQuoteId, quoteSessionId)
                                        .orElseThrow(() -> new RuntimeException("Selected quote not found for quote session"));
                        selectedQuote.setIsSelected(true);
                        return orderQuoteRepository.save(selectedQuote);
                }
                return orderQuoteRepository.findByQuoteSessionIdAndIsSelectedTrue(quoteSessionId)
                                .orElseThrow(() -> new RuntimeException("No selected quote found for quote session"));
        }

        private void validateQuoteSessionOwnership(OrderQuoteSession quoteSession, BookingChannel bookingChannel,
                        UUID tenantId, UUID ownerUserId, String guestAccessToken) {
                if (quoteSession.getBookingChannel() != bookingChannel) {
                        throw new RuntimeException("Quote session does not belong to the requested booking channel");
                }
                if (bookingChannel == BookingChannel.TENANT
                                && !Optional.ofNullable(quoteSession.getTenantId()).equals(Optional.ofNullable(tenantId))) {
                        throw new RuntimeException("Quote session does not belong to tenant");
                }
                if (bookingChannel == BookingChannel.CONSUMER
                                && !Optional.ofNullable(quoteSession.getOwnerUserId())
                                                .equals(Optional.ofNullable(ownerUserId))) {
                        throw new RuntimeException("Quote session does not belong to user");
                }
                if (bookingChannel == BookingChannel.GUEST
                                && (quoteSession.getGuestAccessToken() == null
                                                || !quoteSession.getGuestAccessToken().equals(guestAccessToken))) {
                        throw new RuntimeException("Invalid guest access token");
                }
        }

        private CreateOrderResponse createExternalProviderOrder(String token, UUID quoteSessionId, OrderQuote selectedQuote,
                        CreateOrderRequest createOrderRequest, BookingChannel bookingChannel) {
                if (bookingChannel == BookingChannel.TENANT) {
                        return providerInterface.createOrder(token, quoteSessionId, createOrderRequest);
                }
                if (selectedQuote.getProviderCode() == null || selectedQuote.getProviderCode().isBlank()) {
                        throw new RuntimeException("Provider code is required for external provider booking");
                }
                return providerInterface.createOrderInternal(selectedQuote.getProviderCode(), createOrderRequest);
        }

        private CreateOrderRequest enrichCreateOrderRequest(CreateOrderRequest request, OrderQuote selectedQuote,
                        OrderQuoteSession quoteSession, BookingChannel bookingChannel) {
                return new CreateOrderRequest(
                                request.idempotencyKey(),
                                bookingChannel == BookingChannel.TENANT && quoteSession.getTenantId() != null
                                                ? quoteSession.getTenantId().toString()
                                                : request.tenantId(),
                                selectedQuote.getQuoteId() != null ? selectedQuote.getQuoteId() : request.quoteId(),
                                request.orderReference(),
                                request.orderType(),
                                request.paymentType(),
                                request.pickup(),
                                request.dropoff(),
                                request.items(),
                                request.packageInfo(),
                                request.timeWindows(),
                                request.deliveryPreferences(),
                                request.externalMetadata(),
                                request.deliveryInstructions());
        }

        private void publishInternalAssignmentAfterCommit(Order order, String selectedType, UUID excludedDriverId,
                        List<String> deliveryOptions, Integer optionIndex, BigDecimal distanceKm) {
                if (order.getPickupLatitude() == null || order.getPickupLongitude() == null) {
                        return;
                }
                InternalDriverAssignmentEvent event = InternalDriverAssignmentEvent.builder()
                                .orderId(order.getId())
                                .tenantId(order.getTenantId())
                                .selectedType(selectedType)
                                .deliveryOptions(deliveryOptions)
                                .optionIndex(optionIndex)
                                .pickupLat(order.getPickupLatitude().doubleValue())
                                .pickupLng(order.getPickupLongitude().doubleValue())
                                .distanceKm(distanceKm != null ? distanceKm.doubleValue() : null)
                                .excludedDriverId(excludedDriverId)
                                .attempt(0)
                                .build();

                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                        kafkaTemplate.send("order-driver-assignment", event);
                                }
                        });
                } else {
                        kafkaTemplate.send("order-driver-assignment", event);
                }
        }

        private List<String> getTenantDeliveryOptions(String token, UUID tenantId) {
                List<TenantDeliveryConf> deliveryConfig = Optional
                                .ofNullable(tenantInterface.getTenantDeliveryConfiguration(token))
                                .map(ResponseEntity::getBody)
                                .orElse(Collections.emptyList())
                                .stream()
                                .sorted(Comparator.comparingInt(TenantDeliveryConf::priority))
                                .toList();

                if (deliveryConfig.isEmpty()) {
                        deliveryConfig = Optional
                                        .ofNullable(tenantInterface.getTenantDeliveryConfigurationByTenantId(tenantId))
                                        .map(ResponseEntity::getBody)
                                        .orElse(Collections.emptyList())
                                        .stream()
                                        .sorted(Comparator.comparingInt(TenantDeliveryConf::priority))
                                        .toList();
                }

                if (deliveryConfig.isEmpty()) {
                        return List.of("EXTERNAL_PROVIDERS", "LOCAL_DRIVERS", "TENANT_DRIVERS");
                }

                return deliveryConfig.stream().map(conf -> conf.optionType().toUpperCase()).toList();
        }

        private List<String> getDeliveryOptionsForOrder(String token, Order order, BookingChannel bookingChannel) {
                if (bookingChannel == BookingChannel.TENANT && order.getTenantId() != null) {
                        return getTenantDeliveryOptions(token, order.getTenantId());
                }
                if ("LOCAL_DRIVERS".equalsIgnoreCase(order.getSelectedType())) {
                        return List.of("LOCAL_DRIVERS");
                }
                return List.of("EXTERNAL_PROVIDERS");
        }

        private void saveOrderLocations(Order order, CreateOrderRequest createOrderRequest) {
                if (createOrderRequest.pickup() != null && createOrderRequest.pickup().address() != null) {
                        var pAddr = createOrderRequest.pickup().address();
                        OrderLocation pickup = new OrderLocation();
                        pickup.setOrder(order);
                        pickup.setLocationType(LocationType.PICKUP);
                        pickup.setLatitude(BigDecimal.valueOf(pAddr.latitude()));
                        pickup.setLongitude(BigDecimal.valueOf(pAddr.longitude()));
                        pickup.setCity(pAddr.city());
                        pickup.setState(pAddr.state());
                        pickup.setCountry(pAddr.country());
                        pickup.setPincode(pAddr.pincode());
                        pickup.setLocality(pAddr.locality());
                        orderLocationRepository.save(pickup);
                }
                if (createOrderRequest.dropoff() != null && createOrderRequest.dropoff().address() != null) {
                        var dAddr = createOrderRequest.dropoff().address();
                        OrderLocation drop = new OrderLocation();
                        drop.setOrder(order);
                        drop.setLocationType(LocationType.DROP);
                        drop.setLatitude(BigDecimal.valueOf(dAddr.latitude()));
                        drop.setLongitude(BigDecimal.valueOf(dAddr.longitude()));
                        drop.setCity(dAddr.city());
                        drop.setState(dAddr.state());
                        drop.setCountry(dAddr.country());
                        drop.setPincode(dAddr.pincode());
                        drop.setLocality(dAddr.locality());
                        orderLocationRepository.save(drop);
                }
        }

        @CacheEvict(value = { "orderStatus", "orders" }, key = "#orderId")
        public Message cancelOrder(String token, UUID orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));
                validateCancelAccess(order, token);
                validateCancellableStatus(order);

                if (isExternalProviderOrder(order)) {
                        cancelExternalProviderOrder(token, order);
                } else if (isInternalDriverOrder(order) && order.getAssignedDriverId() != null) {
                        releaseAssignedDriver(order);
                }

                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                return new Message("Order cancelled successfully");
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

        @Cacheable(value = "orderStatus", key = "#orderId", unless = "#result == null")
        public String getOrderStatus(String token, UUID orderId) {
                OrderTrackingState orderTrackingState = orderTrackingStateRepository.findById(orderId).orElse(null);
                if (orderTrackingState != null) {
                        return orderTrackingState.getCurrentStatus().name();
                }
                return orderRepository.findById(orderId)
                                .map(order -> order.getOrderStatus().name())
                                .orElse(null);
        }

        public String getGuestOrderStatus(UUID orderId, String guestAccessToken) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
                validateGuestOrderAccess(order, guestAccessToken);
                OrderTrackingState orderTrackingState = orderTrackingStateRepository.findById(orderId).orElse(null);
                if (orderTrackingState != null) {
                        return orderTrackingState.getCurrentStatus().name();
                }
                return order.getOrderStatus().name();
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

        public com.swifttrack.dto.orderDto.OrderDetailsResponse getGuestOrderById(UUID orderId, String guestAccessToken) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
                validateGuestOrderAccess(order, guestAccessToken);
                return getOrderDetails(order);
        }

        private com.swifttrack.dto.orderDto.OrderDetailsResponse getOrderDetails(Order order) {
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

                return new com.swifttrack.dto.orderDto.OrderDetailsResponse(
                                order.getId(),
                                order.getCustomerReferenceId(),
                                order.getOrderStatus().name(),
                                city,
                                state,
                                pickupLat,
                                pickupLng,
                                dropoffLat,
                                dropoffLng);
        }

        private void validateGuestOrderAccess(Order order, String guestAccessToken) {
                if (order.getBookingChannel() != BookingChannel.GUEST
                                || order.getGuestAccessToken() == null
                                || !order.getGuestAccessToken().equals(guestAccessToken)) {
                        throw new RuntimeException("Unauthorized guest order access");
                }
        }

        private void validateCancelAccess(Order order, String token) {
                if (order.getBookingChannel() == BookingChannel.GUEST) {
                        validateGuestOrderAccess(order, token);
                        return;
                }

                TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
                if (userDetails == null || userDetails.id() == null) {
                        throw new RuntimeException("Invalid token or user not found");
                }

                if (order.getBookingChannel() == BookingChannel.TENANT) {
                        if (userDetails.tenantId().isEmpty() || order.getTenantId() == null
                                        || !order.getTenantId().equals(userDetails.tenantId().get())) {
                                throw new RuntimeException("Order does not belong to tenant");
                        }
                        return;
                }

                if (order.getOwnerUserId() == null || !order.getOwnerUserId().equals(userDetails.id())) {
                        throw new RuntimeException("Order does not belong to user");
                }
        }

        private void validateCancellableStatus(Order order) {
                if (order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ASSIGNED) {
                        throw new RuntimeException("Order can only be cancelled in CREATED or ASSIGNED status");
                }
        }

        private boolean isExternalProviderOrder(Order order) {
                return "EXTERNAL_PROVIDERS".equalsIgnoreCase(order.getSelectedType());
        }

        private boolean isInternalDriverOrder(Order order) {
                return "LOCAL_DRIVERS".equalsIgnoreCase(order.getSelectedType())
                                || "TENANT_DRIVERS".equalsIgnoreCase(order.getSelectedType());
        }

        private void cancelExternalProviderOrder(String token, Order order) {
                if (order.getProviderOrderId() == null || order.getProviderOrderId().isBlank()
                                || order.getSelectedProviderCode() == null || order.getSelectedProviderCode().isBlank()) {
                        throw new RuntimeException("Provider order details are missing");
                }
                providerInterface.cancelOrder(token, order.getProviderOrderId(), order.getSelectedProviderCode());
        }

        private void releaseAssignedDriver(Order order) {
                UserCanceledOrderEvent cancelEvent = UserCanceledOrderEvent.builder()
                                .orderId(order.getId())
                                .driverId(order.getAssignedDriverId())
                                .reason("Cancelled by user")
                                .build();
                kafkaTemplate.send("user-canceled-order", cancelEvent);
                order.setAssignedDriverId(null);
                if ("LOCAL_DRIVERS".equalsIgnoreCase(order.getSelectedType())
                                || "TENANT_DRIVERS".equalsIgnoreCase(order.getSelectedType())) {
                        order.setSelectedProviderCode(null);
                }
        }

        private String resolveChoiceCode(OrderQuote selectedQuote) {
                if (selectedQuote == null) {
                        return null;
                }
                if ("LOCAL_DRIVERS".equalsIgnoreCase(selectedQuote.getSelectedType())) {
                        return "SWIFTTRACK_DRIVER";
                }
                return selectedQuote.getProviderCode();
        }
}
