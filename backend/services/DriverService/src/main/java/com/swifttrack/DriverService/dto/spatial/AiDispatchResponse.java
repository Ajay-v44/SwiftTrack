package com.swifttrack.DriverService.dto.spatial;

public record AiDispatchResponse(
        String driver_id,
        Double confidence,
        String reason,
        Boolean fallback,
        Long latency_ms) {

    public boolean accepted() {
        return driver_id != null && !driver_id.isBlank() && !Boolean.TRUE.equals(fallback);
    }
}
