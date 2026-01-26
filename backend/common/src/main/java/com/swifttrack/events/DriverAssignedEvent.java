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
public class DriverAssignedEvent {
    private UUID orderId;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
    private String providerCode;
}
