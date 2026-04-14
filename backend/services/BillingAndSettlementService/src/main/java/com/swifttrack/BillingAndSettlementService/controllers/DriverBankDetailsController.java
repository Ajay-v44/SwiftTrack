package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.dto.DriverBankDetailsDto;
import com.swifttrack.BillingAndSettlementService.models.DriverBankDetails;
import com.swifttrack.BillingAndSettlementService.repositories.DriverBankDetailsRepository;
import com.swifttrack.BillingAndSettlementService.services.AccountService;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing/v1/driver-bank-details")
@RequiredArgsConstructor
public class DriverBankDetailsController {

    private final DriverBankDetailsRepository repository;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<DriverBankDetailsDto> getDetails(@RequestHeader("token") String token,
                                                           @RequestParam(required = false) UUID driverId) {
        TokenResponse tokenResponse = accountService.resolveTokenResponse(token);
        
        UUID targetDriverId;
        
        boolean isAdmin = tokenResponse.userType().isPresent() && 
            (tokenResponse.userType().get() == UserType.SUPER_ADMIN ||
             tokenResponse.userType().get() == UserType.SYSTEM_ADMIN ||
             tokenResponse.userType().get() == UserType.TENANT_ADMIN);

        if (isAdmin && driverId != null) {
            targetDriverId = driverId;
        } else {
            targetDriverId = tokenResponse.id();
        }

        DriverBankDetails details = repository.findByDriverId(targetDriverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank details not found"));

        DriverBankDetailsDto dto = DriverBankDetailsDto.builder()
                .accountNumber(details.getAccountNumber())
                .ifscCode(details.getIfscCode())
                .upiId(details.getUpiId())
                .accountHolderName(details.getAccountHolderName())
                .bankName(details.getBankName())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<DriverBankDetailsDto> saveOrUpdateDetails(@RequestHeader("token") String token,
                                                                    @RequestBody DriverBankDetailsDto request) {
        TokenResponse tokenResponse = accountService.resolveTokenResponse(token);
        UUID targetDriverId = tokenResponse.id();

        DriverBankDetails details = repository.findByDriverId(targetDriverId).orElse(DriverBankDetails.builder()
                .driverId(targetDriverId)
                .build());

        details.setAccountNumber(request.getAccountNumber());
        details.setIfscCode(request.getIfscCode());
        details.setUpiId(request.getUpiId());
        details.setAccountHolderName(request.getAccountHolderName());
        details.setBankName(request.getBankName());

        repository.save(details);

        return ResponseEntity.ok(request);
    }
}
