package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.dto.MarginConfigAddRequestCreateDto;
import com.swifttrack.BillingAndSettlementService.dto.MarginConfigAddRequestResponse;
import com.swifttrack.BillingAndSettlementService.dto.MarginConfigAddRequestStatusUpdateDto;
import com.swifttrack.BillingAndSettlementService.models.MarginConfigAddRequest;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginRequestStatus;
import com.swifttrack.BillingAndSettlementService.repositories.MarginConfigAddRequestRepository;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.UserType;
import com.swifttrack.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarginConfigAddRequestService {

    private final MarginConfigAddRequestRepository marginConfigAddRequestRepository;
    private final AccountService accountService;

    @Transactional
    public MarginConfigAddRequestResponse createRequest(String token, MarginConfigAddRequestCreateDto request) {
        TokenResponse tokenResponse = accountService.resolveTokenResponse(token);
        validateTenantAdmin(tokenResponse);
        validateCreateRequest(request);

        UUID tenantId = tokenResponse.tenantId()
                .orElseThrow(() -> new CustomException(HttpStatus.FORBIDDEN, "Tenant admin token missing tenantId"));

        marginConfigAddRequestRepository
                .findFirstByTenantIdAndStatusOrderByUpdatedAtDesc(tenantId, MarginRequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new CustomException(HttpStatus.BAD_REQUEST,
                            "A pending request already exists. Cancel it before creating a new one.");
                });

        MarginConfigAddRequest newRequest = MarginConfigAddRequest.builder()
                .tenantId(tenantId)
                .requestedBy(tokenResponse.id())
                .key(request.getKey())
                .value(request.getValue())
                .baseFare(request.getBaseFare())
                .perKmRate(request.getPerKmRate())
                .status(MarginRequestStatus.PENDING)
                .build();

        MarginConfigAddRequest saved = marginConfigAddRequestRepository.save(newRequest);
        log.info("Created margin config add request id={} tenantId={} requestedBy={}",
                saved.getId(), tenantId, tokenResponse.id());
        return toResponse(saved);
    }

    @Transactional
    public MarginConfigAddRequestResponse updateStatus(String token, UUID requestId,
            MarginConfigAddRequestStatusUpdateDto updateRequest) {
        TokenResponse tokenResponse = accountService.resolveTokenResponse(token);
        MarginRequestStatus requestedStatus = extractAllowedTargetStatus(updateRequest);

        MarginConfigAddRequest request = marginConfigAddRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Margin config add request not found"));

        if (request.getStatus() != MarginRequestStatus.PENDING) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Only pending requests can be updated");
        }

        boolean isSuperAdminOrSystem = isSuperAdminOrSystem(tokenResponse);
        boolean isTenantAdmin = isTenantAdmin(tokenResponse);

        if (isSuperAdminOrSystem) {
            request.setStatus(requestedStatus);
            request.setActedBy(tokenResponse.id());
        } else if (isTenantAdmin) {
            UUID tokenTenantId = tokenResponse.tenantId()
                    .orElseThrow(
                            () -> new CustomException(HttpStatus.FORBIDDEN, "Tenant admin token missing tenantId"));
            if (!tokenTenantId.equals(request.getTenantId())) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admin can only update own tenant requests");
            }
            if (requestedStatus != MarginRequestStatus.CANCELLED) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admin can only cancel pending requests");
            }
            request.setStatus(MarginRequestStatus.CANCELLED);
            request.setActedBy(tokenResponse.id());
        } else {
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        MarginConfigAddRequest saved = marginConfigAddRequestRepository.save(request);
        log.info("Updated margin config add request id={} status={} actedBy={}",
                saved.getId(), saved.getStatus(), tokenResponse.id());
        return toResponse(saved);
    }

    public List<MarginConfigAddRequestResponse> getRequestsByStatus(String token, MarginRequestStatus status) {
        TokenResponse tokenResponse = accountService.resolveTokenResponse(token);
        if (!isSuperAdminOrSystem(tokenResponse)) {
            UUID tokenTenantId = tokenResponse.tenantId()
                    .orElseThrow(
                            () -> new CustomException(HttpStatus.FORBIDDEN, "Tenant admin token missing tenantId"));
            List<MarginConfigAddRequest> requests = marginConfigAddRequestRepository
                    .findByTenantIdOrderByCreatedAtDesc(tokenTenantId);
            return requests.stream().map(this::toResponse).toList();
        }

        List<MarginConfigAddRequest> requests = status == null
                ? marginConfigAddRequestRepository.findAllByOrderByCreatedAtDesc()
                : marginConfigAddRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        return requests.stream().map(this::toResponse).toList();
    }

    private MarginRequestStatus extractAllowedTargetStatus(MarginConfigAddRequestStatusUpdateDto updateRequest) {
        if (updateRequest == null || updateRequest.getStatus() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "status is required");
        }
        if (updateRequest.getStatus() == MarginRequestStatus.PENDING) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Status change to PENDING is not allowed");
        }
        return updateRequest.getStatus();
    }

    private void validateCreateRequest(MarginConfigAddRequestCreateDto request) {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getKey() == null || request.getKey().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "key is required");
        }
        if (request.getValue() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "value is required");
        }
        if (request.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "value cannot be negative");
        }
        if (request.getBaseFare() != null && request.getBaseFare().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "baseFare cannot be negative");
        }
        if (request.getPerKmRate() != null && request.getPerKmRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "perKmRate cannot be negative");
        }
    }

    private void validateTenantAdmin(TokenResponse tokenResponse) {
        if (!isTenantAdmin(tokenResponse)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Only TENANT_ADMIN can create margin config requests");
        }
    }

    private boolean isTenantAdmin(TokenResponse tokenResponse) {
        return tokenResponse.userType().isPresent() && tokenResponse.userType().get() == UserType.TENANT_ADMIN;
    }

    private boolean isSuperAdminOrSystem(TokenResponse tokenResponse) {
        if (tokenResponse.userType().isEmpty()) {
            return false;
        }
        UserType userType = tokenResponse.userType().get();
        return userType == UserType.SUPER_ADMIN || userType == UserType.SYSTEM_USER;
    }

    private MarginConfigAddRequestResponse toResponse(MarginConfigAddRequest request) {
        return MarginConfigAddRequestResponse.builder()
                .id(request.getId())
                .tenantId(request.getTenantId())
                .requestedBy(request.getRequestedBy())
                .key(request.getKey())
                .value(request.getValue())
                .baseFare(request.getBaseFare())
                .perKmRate(request.getPerKmRate())
                .status(request.getStatus())
                .actedBy(request.getActedBy())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
