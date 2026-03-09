package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.dto.MarginConfigAddRequestCreateDto;
import com.swifttrack.BillingAndSettlementService.dto.MarginConfigAddRequestResponse;
import com.swifttrack.BillingAndSettlementService.dto.MarginConfigAddRequestStatusUpdateDto;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginRequestStatus;
import com.swifttrack.BillingAndSettlementService.services.MarginConfigAddRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/margin-config-requests")
@RequiredArgsConstructor
public class MarginConfigAddRequestController {

    private final MarginConfigAddRequestService marginConfigAddRequestService;

    @PostMapping
    public ResponseEntity<MarginConfigAddRequestResponse> createRequest(@RequestHeader("token") String token,
            @RequestBody MarginConfigAddRequestCreateDto request) {
        return ResponseEntity.ok(marginConfigAddRequestService.createRequest(token, request));
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<MarginConfigAddRequestResponse> updateStatus(@RequestHeader("token") String token,
            @PathVariable UUID requestId,
            @RequestBody MarginConfigAddRequestStatusUpdateDto request) {
        return ResponseEntity.ok(marginConfigAddRequestService.updateStatus(token, requestId, request));
    }

    @GetMapping
    public ResponseEntity<List<MarginConfigAddRequestResponse>> getRequests(
            @RequestHeader("token") String token,
            @RequestParam(required = false) MarginRequestStatus status) {
        return ResponseEntity.ok(marginConfigAddRequestService.getRequestsByStatus(token, status));
    }
}
