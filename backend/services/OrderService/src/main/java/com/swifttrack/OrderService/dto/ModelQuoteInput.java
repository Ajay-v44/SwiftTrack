package com.swifttrack.OrderService.dto;

public record ModelQuoteInput(
                String provider,
                double distance_km,
                double traffic_level,
                boolean is_peak_hour,
                int provider_load) {

}
