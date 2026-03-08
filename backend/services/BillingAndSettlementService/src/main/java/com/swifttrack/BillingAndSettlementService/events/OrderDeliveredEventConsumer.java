package com.swifttrack.BillingAndSettlementService.events;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.PricingSnapshot;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import com.swifttrack.BillingAndSettlementService.models.enums.PricingSource;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import com.swifttrack.BillingAndSettlementService.services.AccountService;
import com.swifttrack.BillingAndSettlementService.services.LedgerService;
import com.swifttrack.BillingAndSettlementService.services.PricingSnapshotService;
import com.swifttrack.events.OrderDeliveredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka consumer for order delivery events.
 *
 * Listens to the "order-delivered" topic published by OrderService.
 * When an order is delivered, this consumer:
 *   1. Creates a pricing snapshot for the order
 *   2. Creates/ensures ledger accounts exist for tenant, provider/driver, and platform
 *   3. Records DEBIT on tenant account (money owed)
 *   4. Records CREDIT on provider/driver account (money earned)
 *   5. Records CREDIT on platform account (margin earned)
 *
 * All operations run in a single SERIALIZABLE transaction for atomicity.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderDeliveredEventConsumer {

    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final PricingSnapshotService pricingSnapshotService;

    /**
     * Platform account uses a well-known fixed UUID.
     */
    private static final UUID PLATFORM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * System user ID for automated billing (no JWT token in event-driven flows).
     */
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @KafkaListener(
            topics = "order-delivered",
            groupId = "billing-service-group",
            properties = {
                    "spring.json.value.default.type=com.swifttrack.events.OrderDeliveredEvent"
            }
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Received order-delivered event: orderId={}, tenantId={}, amount={}, source={}",
                event.getOrderId(), event.getTenantId(), event.getAmount(), event.getDeliverySource());

        try {
            UUID orderId = event.getOrderId();
            UUID tenantId = UUID.fromString(event.getTenantId());
            BigDecimal totalAmount = event.getAmount();
            String deliverySource = event.getDeliverySource();

            // Determine pricing source and margins
            PricingSource pricingSource = resolvePricingSource(deliverySource);
            BigDecimal platformMargin = calculatePlatformMargin(totalAmount);
            BigDecimal payeeAmount = totalAmount.subtract(platformMargin);

            // 1. Create immutable pricing snapshot
            PricingSnapshot snapshot;
            if (pricingSource == PricingSource.PROVIDER) {
                snapshot = pricingSnapshotService.createSnapshot(
                        orderId, payeeAmount, null, platformMargin, totalAmount, pricingSource);
            } else {
                snapshot = pricingSnapshotService.createSnapshot(
                        orderId, null, payeeAmount, platformMargin, totalAmount, pricingSource);
            }

            log.info("Created pricing snapshot for orderId={}: tenantCharge={}, payee={}, margin={}",
                    orderId, totalAmount, payeeAmount, platformMargin);

            // 2. Ensure accounts exist
            Account tenantAccount = accountService.getAccountByUserIdAndType(tenantId, AccountType.TENANT)
                    .orElseGet(() -> accountService.createAccountInternal(tenantId, AccountType.TENANT, SYSTEM_USER_ID));

            Account payeeAccount = resolvePayeeAccount(event, pricingSource);

            Account platformAccount = accountService.getAccountByUserIdAndType(PLATFORM_USER_ID, AccountType.PLATFORM)
                    .orElseGet(() -> accountService.createAccountInternal(PLATFORM_USER_ID, AccountType.PLATFORM, SYSTEM_USER_ID));

            // 3. Record ledger entries with idempotency keys
            String baseKey = "ORDER-" + orderId;

            // DEBIT tenant (they owe money)
            ledgerService.debit(
                    tenantAccount.getId(),
                    totalAmount,
                    ReferenceType.ORDER,
                    orderId,
                    orderId,
                    "Order delivery charge — " + pricingSource.name(),
                    baseKey + "-TENANT-DEBIT",
                    SYSTEM_USER_ID
            );

            // CREDIT payee (provider or driver — money owed to them)
            String payeeLabel = pricingSource == PricingSource.PROVIDER
                    ? "Provider payout for delivery"
                    : "Driver earning for delivery";

            ledgerService.credit(
                    payeeAccount.getId(),
                    payeeAmount,
                    ReferenceType.ORDER,
                    orderId,
                    orderId,
                    payeeLabel,
                    baseKey + "-PAYEE-CREDIT",
                    SYSTEM_USER_ID
            );

            // CREDIT platform (margin earned)
            ledgerService.credit(
                    platformAccount.getId(),
                    platformMargin,
                    ReferenceType.ORDER,
                    orderId,
                    orderId,
                    "Platform margin — " + pricingSource.name(),
                    baseKey + "-PLATFORM-CREDIT",
                    SYSTEM_USER_ID
            );

            log.info("✅ Billing complete for orderId={} | Tenant DEBIT={} | Payee CREDIT={} | Platform CREDIT={}",
                    orderId, totalAmount, payeeAmount, platformMargin);

        } catch (Exception e) {
            log.error("❌ Failed to process billing for orderId={}: {}", event.getOrderId(), e.getMessage(), e);
            // Transaction will be rolled back automatically due to SERIALIZABLE isolation
            throw e; // Re-throw to trigger Kafka retry
        }
    }

    /**
     * Resolve the PricingSource enum from the event's deliverySource string.
     */
    private PricingSource resolvePricingSource(String deliverySource) {
        if (deliverySource == null) {
            return PricingSource.PROVIDER; // Default
        }
        return switch (deliverySource.toUpperCase()) {
            case "EXTERNAL_PROVIDER" -> PricingSource.PROVIDER;
            case "TENANT_DRIVER" -> PricingSource.TENANT_DRIVER;
            case "GIG_DRIVER" -> PricingSource.GIG_DRIVER;
            default -> PricingSource.PROVIDER;
        };
    }

    /**
     * Calculate platform margin as 15% of total (configurable via MarginConfig in future).
     */
    private BigDecimal calculatePlatformMargin(BigDecimal totalAmount) {
        return totalAmount.multiply(BigDecimal.valueOf(0.15))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Resolve or create the payee's ledger account (provider or driver) based on delivery source.
     */
    private Account resolvePayeeAccount(OrderDeliveredEvent event, PricingSource pricingSource) {
        if (pricingSource == PricingSource.PROVIDER) {
            // For external providers, use providerCode as a deterministic UUID
            UUID providerUserId = deterministicUUID("provider:" + event.getProviderCode());
            return accountService.getAccountByUserIdAndType(providerUserId, AccountType.PROVIDER)
                    .orElseGet(() -> accountService.createAccountInternal(providerUserId, AccountType.PROVIDER, SYSTEM_USER_ID));
        } else {
            // For drivers (tenant or gig), use the driverId
            UUID driverId = event.getDriverId();
            if (driverId == null) {
                // Fallback: generate from providerCode if driver ID not set
                driverId = deterministicUUID("driver:" + event.getProviderCode());
            }
            return accountService.getAccountByUserIdAndType(driverId, AccountType.DRIVER)
                    .orElseGet(() -> {
                        UUID id = event.getDriverId() != null ? event.getDriverId()
                                : deterministicUUID("driver:" + event.getProviderCode());
                        return accountService.createAccountInternal(id, AccountType.DRIVER, SYSTEM_USER_ID);
                    });
        }
    }

    /**
     * Generate a deterministic UUID from a string for consistent provider/driver account lookup.
     */
    private UUID deterministicUUID(String input) {
        return UUID.nameUUIDFromBytes(input.getBytes());
    }
}
