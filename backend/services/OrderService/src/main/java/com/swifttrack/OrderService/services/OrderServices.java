package com.swifttrack.OrderService.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.FeignClient.ProviderInterface;
import com.swifttrack.OrderService.dto.ModelQuoteInput;
import com.swifttrack.dto.map.ApiResponse;
import com.swifttrack.dto.map.DistanceResult;
import com.swifttrack.dto.map.NormalizedLocation;
import com.swifttrack.dto.GetProviders;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

@Service
public class OrderServices {
    ProviderInterface providerInterface;
    MapInterface mapInterface;

    public OrderServices(ProviderInterface providerInterface, MapInterface mapInterface) {
        this.providerInterface = providerInterface;
        this.mapInterface = mapInterface;
    }

    public QuoteResponse getQuote(String token, QuoteInput quoteInput) {
        ApiResponse<NormalizedLocation> dropoffLocation = mapInterface.reverseGeocode(quoteInput.dropoffLat(),
                quoteInput.dropoffLng());
        ApiResponse<NormalizedLocation> pickupLocation = mapInterface.reverseGeocode(quoteInput.pickupLat(),
                quoteInput.pickupLng());
        ApiResponse<DistanceResult> distance = mapInterface.calculateDistance(quoteInput.pickupLat(),
                quoteInput.pickupLng(), quoteInput.dropoffLat(), quoteInput.dropoffLng());

        List<ModelQuoteInput> modelQuoteInput = new ArrayList<>();
        if (dropoffLocation.getData().getState().equals(pickupLocation.getData().getState())) {

        }
        List<GetProviders> providers = providerInterface.getTenantProviders(token);
        return providerInterface.getQuote(token, quoteInput);
    }

}
