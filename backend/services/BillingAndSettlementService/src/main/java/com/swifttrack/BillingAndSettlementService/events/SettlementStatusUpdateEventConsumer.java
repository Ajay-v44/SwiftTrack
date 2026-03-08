package com.swifttrack.BillingAndSettlementService.events;

import com.swifttrack.BillingAndSettlementService.services.SettlementService;
import com.swifttrack.events.SettlementStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Kafka consumer for settlement status updates.
 *
 * Listens to the "settlement.status.update" topic published by the Payment Gateway.
 * Depending on the status, it marks the settlement as successfully SETTLED
 * or handles failure by reversing the ledger debit.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementStatusUpdateEventConsumer {

    private final SettlementService settlementService;

    // Use a token-less constant or system token bypass for automated callbacks
    private static final String SYSTEM_TOKEN = "SYSTEM_BATCH_PROCESS"; 

    @KafkaListener(
            topics = "settlement.status.update",
            groupId = "billing-service-group",
            properties = {
                    "spring.json.value.default.type=com.swifttrack.events.SettlementStatusUpdateEvent"
            }
    )
    public void handleSettlementStatusUpdate(SettlementStatusUpdateEvent event) {
        log.info("Received settlement status update for settlementId={} status={}", 
                event.getSettlementId(), event.getStatus());

        try {
            if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                settlementService.markSettled(event.getSettlementId(), event.getTransactionReference());
                log.info("Successfully marked settlementId={} as SETTLED.", event.getSettlementId());
            } else if ("FAILED".equalsIgnoreCase(event.getStatus())) {
                log.warn("Settlement for settlementId={} failed. Reason: {}. Initiating reversal...", 
                        event.getSettlementId(), event.getFailureReason());
                // System user ID for automated callbacks
                UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
                settlementService.markFailedInternal(SYSTEM_USER_ID, event.getSettlementId());
                log.info("Successfully processed failure and reversal for settlementId={}", event.getSettlementId());
            } else {
                log.warn("Unknown settlement status received: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("Error processing settlement status update for settlementId={}: {}", 
                    event.getSettlementId(), e.getMessage(), e);
            throw e; // To allow Kafka to retry
        }
    }
}
