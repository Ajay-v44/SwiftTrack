package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.PricingSnapshot;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import com.swifttrack.BillingAndSettlementService.models.enums.PricingSource;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

        private final LedgerService ledgerService;
        private final AccountService accountService;
        private final PricingSnapshotService pricingSnapshotService;

        @Transactional(isolation = Isolation.SERIALIZABLE)
        public PricingSnapshot processExternalProviderOrder(String token, UUID orderId, UUID tenantId,
                        UUID providerId, BigDecimal providerCost,
                        BigDecimal platformMargin) {
                UUID createdBy = accountService.resolveUserId(token);
                BigDecimal tenantCharge = providerCost.add(platformMargin);

                // 1. Create pricing snapshot
                PricingSnapshot snapshot = pricingSnapshotService.createSnapshot(
                                orderId, providerCost, null, platformMargin, tenantCharge, PricingSource.PROVIDER);

                // 2. Ensure accounts exist
                Account tenantAccount = accountService.getAccountByUserIdAndType(tenantId, AccountType.TENANT)
                                .orElseGet(() -> accountService.createAccountInternal(tenantId, AccountType.TENANT,
                                                createdBy));
                Account providerAccount = accountService.getAccountByUserIdAndType(providerId, AccountType.PROVIDER)
                                .orElseGet(() -> accountService.createAccountInternal(providerId, AccountType.PROVIDER,
                                                createdBy));
                Account platformAccount = getPlatformAccount(createdBy);

                // 3. Record ledger entries
                String baseKey = "ORDER-" + orderId;

                ledgerService.debit(tenantAccount.getId(), tenantCharge, ReferenceType.ORDER,
                                orderId, orderId, "Order charge for external provider delivery",
                                baseKey + "-TENANT-DEBIT", createdBy);

                ledgerService.credit(providerAccount.getId(), providerCost, ReferenceType.ORDER,
                                orderId, orderId, "Provider payout for delivery",
                                baseKey + "-PROVIDER-CREDIT", createdBy);

                ledgerService.credit(platformAccount.getId(), platformMargin, ReferenceType.ORDER,
                                orderId, orderId, "Platform margin on external provider order",
                                baseKey + "-PLATFORM-CREDIT", createdBy);

                log.info("Processed external provider billing for orderId={} tenantCharge={} providerCost={} margin={} by={}",
                                orderId, tenantCharge, providerCost, platformMargin, createdBy);

                return snapshot;
        }

        /**
         * Process billing for a Tenant Driver order.
         *
         * Flow: Distance × Rate → Platform margin → Tenant charged
         * Tenant DEBIT, Driver CREDIT, Platform CREDIT
         */
        @Transactional(isolation = Isolation.SERIALIZABLE)
        public PricingSnapshot processTenantDriverOrder(String token, UUID orderId, UUID tenantId,
                        UUID driverId, BigDecimal driverCost,
                        BigDecimal platformMargin) {
                UUID createdBy = accountService.resolveUserId(token);
                BigDecimal tenantCharge = driverCost.add(platformMargin);

                PricingSnapshot snapshot = pricingSnapshotService.createSnapshot(
                                orderId, null, driverCost, platformMargin, tenantCharge, PricingSource.TENANT_DRIVER);

                Account tenantAccount = accountService.getAccountByUserIdAndType(tenantId, AccountType.TENANT)
                                .orElseGet(() -> accountService.createAccountInternal(tenantId, AccountType.TENANT,
                                                createdBy));
                Account driverAccount = accountService.getAccountByUserIdAndType(driverId, AccountType.DRIVER)
                                .orElseGet(() -> accountService.createAccountInternal(driverId, AccountType.DRIVER,
                                                createdBy));
                Account platformAccount = getPlatformAccount(createdBy);

                String baseKey = "ORDER-" + orderId;

                ledgerService.debit(tenantAccount.getId(), tenantCharge, ReferenceType.ORDER,
                                orderId, orderId, "Order charge for tenant driver delivery",
                                baseKey + "-TENANT-DEBIT", createdBy);

                ledgerService.credit(driverAccount.getId(), driverCost, ReferenceType.ORDER,
                                orderId, orderId, "Driver payout for tenant delivery",
                                baseKey + "-DRIVER-CREDIT", createdBy);

                ledgerService.credit(platformAccount.getId(), platformMargin, ReferenceType.ORDER,
                                orderId, orderId, "Platform margin on tenant driver order",
                                baseKey + "-PLATFORM-CREDIT", createdBy);

                log.info("Processed tenant driver billing for orderId={} tenantCharge={} driverCost={} margin={} by={}",
                                orderId, tenantCharge, driverCost, platformMargin, createdBy);

                return snapshot;
        }

        /**
         * Process billing for a SwiftTrack Gig Driver order.
         *
         * Flow: Base pay + per-km pay → Platform commission → Tenant charged
         * Tenant DEBIT, Driver CREDIT, Platform CREDIT
         */
        @Transactional(isolation = Isolation.SERIALIZABLE)
        public PricingSnapshot processGigDriverOrder(String token, UUID orderId, UUID tenantId,
                        UUID driverId, BigDecimal driverEarning,
                        BigDecimal platformCommission) {
                UUID createdBy = accountService.resolveUserId(token);
                BigDecimal tenantCharge = driverEarning.add(platformCommission);

                PricingSnapshot snapshot = pricingSnapshotService.createSnapshot(
                                orderId, null, driverEarning, platformCommission, tenantCharge,
                                PricingSource.GIG_DRIVER);

                Account tenantAccount = accountService.getAccountByUserIdAndType(tenantId, AccountType.TENANT)
                                .orElseGet(() -> accountService.createAccountInternal(tenantId, AccountType.TENANT,
                                                createdBy));
                Account driverAccount = accountService.getAccountByUserIdAndType(driverId, AccountType.DRIVER)
                                .orElseGet(() -> accountService.createAccountInternal(driverId, AccountType.DRIVER,
                                                createdBy));
                Account platformAccount = getPlatformAccount(createdBy);

                String baseKey = "ORDER-" + orderId;

                ledgerService.debit(tenantAccount.getId(), tenantCharge, ReferenceType.ORDER,
                                orderId, orderId, "Order charge for gig driver delivery",
                                baseKey + "-TENANT-DEBIT", createdBy);

                ledgerService.credit(driverAccount.getId(), driverEarning, ReferenceType.ORDER,
                                orderId, orderId, "Gig driver earning for delivery",
                                baseKey + "-DRIVER-CREDIT", createdBy);

                ledgerService.credit(platformAccount.getId(), platformCommission, ReferenceType.ORDER,
                                orderId, orderId, "Platform commission on gig driver order",
                                baseKey + "-PLATFORM-CREDIT", createdBy);

                log.info("Processed gig driver billing for orderId={} tenantCharge={} driverEarning={} commission={} by={}",
                                orderId, tenantCharge, driverEarning, platformCommission, createdBy);

                return snapshot;
        }

        /**
         * Get or create the SwiftTrack platform account (singleton).
         */
        private Account getPlatformAccount(UUID createdBy) {
                UUID platformUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                return accountService.getAccountByUserIdAndType(platformUserId, AccountType.PLATFORM)
                                .orElseGet(() -> accountService.createAccountInternal(platformUserId,
                                                AccountType.PLATFORM, createdBy));
        }
}
