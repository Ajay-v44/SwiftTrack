package com.swifttrack.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementInitiatedEvent {
    private UUID settlementId;
    private UUID payeeAccountId;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;
    private LocalDateTime initiatedAt;
}
