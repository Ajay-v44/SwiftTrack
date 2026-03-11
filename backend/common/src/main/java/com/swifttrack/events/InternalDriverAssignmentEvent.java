package com.swifttrack.events;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalDriverAssignmentEvent {
    private UUID orderId;
    private UUID tenantId;
    private String selectedType;
    private Double pickupLat;
    private Double pickupLng;
    private int attempt;
}
