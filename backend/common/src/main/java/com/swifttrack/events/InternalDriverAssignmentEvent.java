package com.swifttrack.events;

import java.util.UUID;
import java.util.List;

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
    private List<String> deliveryOptions;
    private Integer optionIndex;
    private Double pickupLat;
    private Double pickupLng;
    private Double distanceKm;
    private UUID excludedDriverId;
    private int attempt;
}
