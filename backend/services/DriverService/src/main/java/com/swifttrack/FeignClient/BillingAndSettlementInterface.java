package com.swifttrack.FeignClient;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.enums.BillingAndSettlement.AccountType;

@FeignClient(name = "billingandsettlementservice", url = "http://localhost:8080/billingandsettlementservice/api/accounts")
public interface BillingAndSettlementInterface {

    @PostMapping("/v1/createAccount")
    public ResponseEntity<?> createAccount(@RequestHeader("token") String token,
            @RequestParam("userId") UUID userId,
            @RequestParam("accountType") AccountType accountType);

    @PostMapping("/v1/admin")
    public ResponseEntity<?> createAccountByAdmin(@RequestHeader("token") String token,
            @RequestParam("userId") UUID userId,
            @RequestParam("accountType") AccountType accountType);

}
