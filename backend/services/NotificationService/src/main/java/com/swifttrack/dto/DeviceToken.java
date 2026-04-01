package com.swifttrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceToken {
    private String userId;     // Required: e.g. User UUID
    private String tenantId;   // Optional: For tenant members to subscribe to tenant events
    private String token;      // Required: FCM Token
}
