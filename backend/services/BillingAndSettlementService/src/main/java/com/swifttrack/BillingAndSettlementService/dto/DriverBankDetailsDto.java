package com.swifttrack.BillingAndSettlementService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverBankDetailsDto {
    private String accountNumber;
    private String ifscCode;
    private String upiId;
    private String accountHolderName;
    private String bankName;
}
