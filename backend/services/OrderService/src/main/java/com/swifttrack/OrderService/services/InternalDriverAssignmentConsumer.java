package com.swifttrack.OrderService.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.swifttrack.FeignClient.BillingInterface;
import com.swifttrack.FeignClient.DriverInterface;
import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.FeignClient.ProviderInterface;
import com.swifttrack.FeignClient.TenantInterface;
import com.swifttrack.OrderService.dto.AssignNearestDriversRequest;
import com.swifttrack.OrderService.dto.AssignNearestDriversResponse;
import com.swifttrack.OrderService.models.Order;
import com.swifttrack.OrderService.models.enums.OrderStatus;
import com.swifttrack.OrderService.repositories.OrderRepository;
import com.swifttrack.dto.GetProviders;
import com.swifttrack.dto.billingDto.QuoteRequest;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;
import com.swifttrack.dto.tenantDto.TenantDeliveryConf;
import com.swifttrack.enums.BillingAndSettlement.BookingChannel;
import com.swifttrack.events.InternalDriverAssignmentEvent;
import com.swifttrack.events.OrderCreatedEvent;

@Service
public class InternalDriverAssignmentConsumer {
    private static final int MAX_RETRY_ATTEMPTS = 4;
    private static final long RETRY_DELAY_SECONDS = 30;

    private final DriverInterface driverInterface;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TenantInterface tenantInterface;
    private final BillingInterface billingInterface;
    private final ProviderInterface providerInterface;
    private final MapInterface mapInterface;
    private final ObjectMapper objectMapper;

    public InternalDriverAssignmentConsumer(DriverInterface driverInterface, OrderRepository orderRepository,
            KafkaTemplate<String, Object> kafkaTemplate, TenantInterface tenantInterface,
            BillingInterface billingInterface, ProviderInterface providerInterface, MapInterface mapInterface,
            ObjectMapper objectMapper) {
        this.driverInterface = driverInterface;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.tenantInterface = tenantInterface;
        this.billingInterface = billingInterface;
        this.providerInterface = providerInterface;
        this.mapInterface = mapInterface;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-driver-assignment", groupId = "order-service-group", properties = {
            "spring.json.value.default.type=com.swifttrack.events.InternalDriverAssignmentEvent" })
    public void handleInternalDriverAssignment(InternalDriverAssignmentEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || isTerminal(order)) {
            return;
        }

        List<String> options = resolveOptions(event, order);
        int startIndex = resolveStartIndex(event, options, order);
        BigDecimal distanceKm = resolveDistanceKm(event, order);

        for (int idx = startIndex; idx < options.size(); idx++) {
            String option = options.get(idx).toUpperCase();
            try {
                boolean assigned = tryAssignByOption(order, option, distanceKm,
                        idx == startIndex ? event.getExcludedDriverId() : null);
                if (assigned) {
                    return;
                }
            } catch (Exception optionError) {
                System.err.println("Dispatch option failed for order " + order.getId() + " option " + option + ": "
                        + optionError.getMessage());
            }
        }

        if (shouldKeepRetrying(order, options, event)) {
            InternalDriverAssignmentEvent retryEvent = InternalDriverAssignmentEvent.builder()
                    .orderId(event.getOrderId())
                    .tenantId(event.getTenantId())
                    .selectedType(order.getSelectedType())
                    .deliveryOptions(options)
                    .optionIndex(0)
                    .pickupLat(event.getPickupLat())
                    .pickupLng(event.getPickupLng())
                    .distanceKm(distanceKm != null ? distanceKm.doubleValue() : null)
                    .excludedDriverId(event.getExcludedDriverId())
                    .attempt(event.getAttempt() + 1)
                    .build();

            CompletableFuture.runAsync(
                    () -> kafkaTemplate.send("order-driver-assignment", retryEvent),
                    CompletableFuture.delayedExecutor(RETRY_DELAY_SECONDS, TimeUnit.SECONDS));
            return;
        }

        order.setOrderStatus(OrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private boolean tryAssignByOption(Order order, String option, BigDecimal distanceKm, java.util.UUID excludedDriverId) {
        if ("TENANT_DRIVERS".equals(option) || "LOCAL_DRIVERS".equals(option)) {
            if ((order.getSelectedType() == null || !option.equalsIgnoreCase(order.getSelectedType()))
                    && !repriceInternalOrder(order, option, distanceKm)) {
                return false;
            }
            boolean tenantDriver = "TENANT_DRIVERS".equals(option);
            AssignNearestDriversRequest request = new AssignNearestDriversRequest(
                    order.getPickupLatitude().doubleValue(),
                    order.getPickupLongitude().doubleValue(),
                    order.getId(),
                    tenantDriver ? "TENANT_DRIVER" : "PLATFORM_DRIVER",
                    tenantDriver ? order.getTenantId() : null,
                    excludedDriverId);

            AssignNearestDriversResponse response = driverInterface.assignNearestDriversInternal(request).getBody();
            if (response != null && response.assigned() && response.assignedDriverId() != null) {
                order.setAssignedDriverId(response.assignedDriverId());
                order.setSelectedProviderCode(response.assignedDriverId().toString());
                order.setOrderStatus(OrderStatus.ASSIGNED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                return true;
            }
            return false;
        }

        if ("EXTERNAL_PROVIDERS".equals(option)) {
            return switchToExternalProvider(order);
        }
        return false;
    }

    private boolean repriceInternalOrder(Order order, String option, BigDecimal distanceKm) {
        if (distanceKm == null || order.getQuoteSessionId() == null) {
            return false;
        }

        com.swifttrack.dto.billingDto.QuoteResponse billingQuote = billingInterface
                .getQuote(new QuoteRequest(
                        order.getQuoteSessionId(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(distanceKm),
                        order.getCreatedBy(),
                        option))
                .getBody();

        if (billingQuote == null || billingQuote.tenantCharge() == null) {
            return false;
        }

        order.setSelectedType(option);
        order.setAssignedDriverId(null);
        order.setSelectedProviderCode(null);
        order.setProviderOrderId(null);
        order.setPaymentAmount(billingQuote.tenantCharge().setScale(2, RoundingMode.HALF_UP));
        order.setOrderStatus(OrderStatus.CREATED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return true;
    }

    private boolean switchToExternalProvider(Order order) {
        List<GetProviders> providers = order.getTenantId() != null
                ? providerInterface.getTenantProvidersByTenantId(order.getTenantId())
                : providerInterface.getProviders();
        if (providers == null || providers.isEmpty()) {
            return false;
        }

        GetProviders chosenProvider = providers.stream()
                .filter(provider -> provider.providerCode() != null && !provider.providerCode().isBlank())
                .min(Comparator.comparingInt(provider -> orderRepository.countActiveOrdersByProvider(provider.providerCode())))
                .orElse(null);

        if (chosenProvider == null || order.getPickupLatitude() == null || order.getPickupLongitude() == null
                || order.getDropLatitude() == null || order.getDropLongitude() == null
                || order.getQuoteSessionId() == null) {
            return false;
        }

        QuoteInput quoteInput = new QuoteInput(
                order.getPickupLatitude().doubleValue(),
                order.getPickupLongitude().doubleValue(),
                order.getDropLatitude().doubleValue(),
                order.getDropLongitude().doubleValue());

        QuoteResponse providerQuote = providerInterface.getQuoteInternal(chosenProvider.providerCode(), quoteInput);
        if (providerQuote == null) {
            return false;
        }
        com.swifttrack.dto.billingDto.QuoteResponse billingQuote = billingInterface
                .getQuote(new QuoteRequest(
                        order.getQuoteSessionId(),
                        Optional.of(BigDecimal.valueOf(providerQuote.price())),
                        Optional.of(chosenProvider.providerCode()),
                        Optional.empty(),
                        order.getCreatedBy(),
                        "EXTERNAL_PROVIDERS"))
                .getBody();

        if (billingQuote == null || billingQuote.tenantCharge() == null) {
            return false;
        }

        com.swifttrack.dto.orderDto.CreateOrderRequest createOrderRequest = buildProviderCreateOrderRequest(order,
                chosenProvider.providerCode(), providerQuote);
        if (createOrderRequest == null) {
            return false;
        }

        com.swifttrack.dto.orderDto.CreateOrderResponse providerOrder = providerInterface
                .createOrderInternal(chosenProvider.providerCode(), createOrderRequest);
        if (providerOrder == null || providerOrder.orderId() == null || providerOrder.providerCode() == null) {
            return false;
        }

        order.setSelectedType("EXTERNAL_PROVIDERS");
        order.setAssignedDriverId(null);
        order.setSelectedProviderCode(providerOrder.providerCode());
        order.setProviderOrderId(providerOrder.orderId());
        order.setPaymentAmount(billingQuote.tenantCharge().setScale(2, RoundingMode.HALF_UP));
        order.setOrderStatus(OrderStatus.CREATED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
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
                .dropoffLng(order.getDropLongitude().doubleValue())
                .build();
        kafkaTemplate.send("order-created", event);
        return true;
    }

    private com.swifttrack.dto.orderDto.CreateOrderRequest buildProviderCreateOrderRequest(Order order,
            String providerCode, QuoteResponse providerQuote) {
        com.swifttrack.dto.orderDto.CreateOrderRequest original = deserializeCreateOrderPayload(order);
        if (original == null || original.pickup() == null || original.dropoff() == null || original.pickup().address() == null
                || original.dropoff().address() == null) {
            return null;
        }

        String effectiveQuoteId = providerQuote.quoteId() != null && !providerQuote.quoteId().isBlank()
                ? providerQuote.quoteId()
                : original.quoteId();
        if ("UBER_DIRECT".equalsIgnoreCase(providerCode)
                && (effectiveQuoteId == null || effectiveQuoteId.isBlank())) {
            return null;
        }

        return new com.swifttrack.dto.orderDto.CreateOrderRequest(
                original.idempotencyKey() != null ? original.idempotencyKey() : order.getId().toString(),
                order.getTenantId() != null ? order.getTenantId().toString() : original.tenantId(),
                effectiveQuoteId,
                original.orderReference() != null ? original.orderReference() : order.getCustomerReferenceId(),
                original.orderType() != null ? original.orderType()
                        : com.swifttrack.dto.orderDto.CreateOrderRequest.OrderType.ON_DEMAND,
                original.paymentType(),
                normalizeLocationPoint(original.pickup(), order.getPickupLatitude().doubleValue(),
                        order.getPickupLongitude().doubleValue()),
                normalizeLocationPoint(original.dropoff(), order.getDropLatitude().doubleValue(),
                        order.getDropLongitude().doubleValue()),
                original.items(),
                original.packageInfo(),
                normalizeTimeWindows(original.timeWindows()),
                original.deliveryPreferences(),
                original.externalMetadata(),
                original.deliveryInstructions());
    }

    private com.swifttrack.dto.orderDto.CreateOrderRequest deserializeCreateOrderPayload(Order order) {
        if (order.getCreateOrderPayload() == null || order.getCreateOrderPayload().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(order.getCreateOrderPayload(),
                    com.swifttrack.dto.orderDto.CreateOrderRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    private com.swifttrack.dto.orderDto.CreateOrderRequest.LocationPoint normalizeLocationPoint(
            com.swifttrack.dto.orderDto.CreateOrderRequest.LocationPoint locationPoint,
            double latitude,
            double longitude) {
        com.swifttrack.dto.orderDto.CreateOrderRequest.Address address = locationPoint.address();
        com.swifttrack.dto.orderDto.CreateOrderRequest.Address normalizedAddress = new com.swifttrack.dto.orderDto.CreateOrderRequest.Address(
                address.line1(),
                address.line2(),
                address.city(),
                address.state(),
                address.country(),
                address.pincode(),
                address.locality(),
                latitude,
                longitude);
        return new com.swifttrack.dto.orderDto.CreateOrderRequest.LocationPoint(
                normalizedAddress,
                locationPoint.contact(),
                locationPoint.businessName(),
                locationPoint.notes(),
                locationPoint.verification());
    }

    private com.swifttrack.dto.orderDto.CreateOrderRequest.TimeWindows normalizeTimeWindows(
            com.swifttrack.dto.orderDto.CreateOrderRequest.TimeWindows timeWindows) {
        if (timeWindows != null) {
            return timeWindows;
        }
        Instant now = Instant.now();
        return new com.swifttrack.dto.orderDto.CreateOrderRequest.TimeWindows(
                now,
                now.plus(1, ChronoUnit.HOURS),
                now.plus(1, ChronoUnit.HOURS),
                now.plus(4, ChronoUnit.HOURS));
    }

    private BigDecimal resolveDistanceKm(InternalDriverAssignmentEvent event, Order order) {
        if (event.getDistanceKm() != null) {
            return BigDecimal.valueOf(event.getDistanceKm());
        }
        if (order.getPickupLatitude() == null || order.getPickupLongitude() == null || order.getDropLatitude() == null
                || order.getDropLongitude() == null) {
            return null;
        }
        ApiResponse<DistanceResult> distance = mapInterface.calculateDistance(
                order.getPickupLatitude().doubleValue(),
                order.getPickupLongitude().doubleValue(),
                order.getDropLatitude().doubleValue(),
                order.getDropLongitude().doubleValue());

        return BigDecimal.valueOf(distance.getData().getDistanceMeters())
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    private List<String> resolveOptions(InternalDriverAssignmentEvent event, Order order) {
        if (event.getDeliveryOptions() != null && !event.getDeliveryOptions().isEmpty()) {
            return event.getDeliveryOptions().stream().map(String::toUpperCase).toList();
        }

        if (order.getTenantId() == null) {
            return List.of(Optional.ofNullable(order.getSelectedType()).orElse("LOCAL_DRIVERS").toUpperCase());
        }

        List<TenantDeliveryConf> config = Optional
                .ofNullable(tenantInterface.getTenantDeliveryConfigurationByTenantId(order.getTenantId()))
                .map(org.springframework.http.ResponseEntity::getBody)
                .orElse(java.util.Collections.emptyList())
                .stream()
                .sorted(Comparator.comparingInt(TenantDeliveryConf::priority))
                .toList();

        if (config.isEmpty()) {
            return List.of("EXTERNAL_PROVIDERS", "LOCAL_DRIVERS", "TENANT_DRIVERS");
        }
        return config.stream().map(conf -> conf.optionType().toUpperCase()).toList();
    }

    private boolean shouldKeepRetrying(Order order, List<String> options, InternalDriverAssignmentEvent event) {
        if (isSwifttrackOnlyNonTenantOrder(order, options)) {
            return true;
        }
        return event.getAttempt() < MAX_RETRY_ATTEMPTS;
    }

    private boolean isSwifttrackOnlyNonTenantOrder(Order order, List<String> options) {
        return order.getBookingChannel() != BookingChannel.TENANT
                && options != null
                && options.size() == 1
                && "LOCAL_DRIVERS".equalsIgnoreCase(options.get(0));
    }

    private int resolveStartIndex(InternalDriverAssignmentEvent event, List<String> options, Order order) {
        if (event.getOptionIndex() != null && event.getOptionIndex() >= 0 && event.getOptionIndex() < options.size()) {
            return event.getOptionIndex();
        }
        int idx = options.indexOf(Optional.ofNullable(order.getSelectedType()).orElse(""));
        return Math.max(idx, 0);
    }

    private boolean isTerminal(Order order) {
        return order.getOrderStatus() == OrderStatus.CANCELLED
                || order.getOrderStatus() == OrderStatus.DELIVERED
                || order.getOrderStatus() == OrderStatus.FAILED;
    }
}
