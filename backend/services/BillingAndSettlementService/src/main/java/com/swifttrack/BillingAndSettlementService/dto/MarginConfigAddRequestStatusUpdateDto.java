package com.swifttrack.BillingAndSettlementService.dto;

import com.swifttrack.BillingAndSettlementService.models.enums.MarginRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginConfigAddRequestStatusUpdateDto {
    private MarginRequestStatus status;
}
