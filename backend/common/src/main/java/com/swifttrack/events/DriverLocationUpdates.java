package com.swifttrack.events;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverLocationUpdates {
    private UUID orderId;
    private String status;
    private BigDecimal latitude;
    private BigDecimal longitude;

}
