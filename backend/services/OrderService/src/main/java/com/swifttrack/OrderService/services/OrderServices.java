package com.swifttrack.OrderService.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.swifttrack.FeignClient.MapInterface;
import com.swifttrack.FeignClient.ProviderInterface;
import com.swifttrack.OrderService.dto.MLPredictionRequest;
import com.swifttrack.OrderService.dto.MLPredictionResponse;
import com.swifttrack.OrderService.dto.MLPredictionResponse.Prediction;
import com.swifttrack.OrderService.dto.ModelQuoteInput;
import com.swifttrack.OrderService.repositories.OrderRepository;
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
    OrderRepository orderRepository;
    RestTemplate restTemplate;
    @Value("${ML_SERVICES_URL}")
    String mlServicesUrl;
    @Value("${ML_MODEL_THRESHOLD}")
    double mlThreshold;

    public OrderServices(ProviderInterface providerInterface, MapInterface mapInterface,
            OrderRepository orderRepository, RestTemplate restTemplate) {
        this.providerInterface = providerInterface;
        this.mapInterface = mapInterface;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    public QuoteResponse getQuote(String token, QuoteInput quoteInput) {
        try {
            CompletableFuture<ApiResponse<NormalizedLocation>> dropoffFuture = CompletableFuture
                    .supplyAsync(() -> mapInterface.reverseGeocode(quoteInput.dropoffLat(), quoteInput.dropoffLng()));

            CompletableFuture<ApiResponse<NormalizedLocation>> pickupFuture = CompletableFuture
                    .supplyAsync(() -> mapInterface.reverseGeocode(quoteInput.pickupLat(), quoteInput.pickupLng()));

            CompletableFuture<ApiResponse<DistanceResult>> distanceFuture = CompletableFuture
                    .supplyAsync(() -> mapInterface.calculateDistance(quoteInput.pickupLat(), quoteInput.pickupLng(),
                            quoteInput.dropoffLat(), quoteInput.dropoffLng()));

            CompletableFuture<List<GetProviders>> providersFuture = CompletableFuture
                    .supplyAsync(() -> providerInterface.getTenantProviders(token));

            // Wait for all to complete with a timeout of 5 seconds
            CompletableFuture.allOf(dropoffFuture, pickupFuture, distanceFuture, providersFuture)
                    .get(5, TimeUnit.SECONDS);

            ApiResponse<NormalizedLocation> dropoffLocation = dropoffFuture.get();
            ApiResponse<NormalizedLocation> pickupLocation = pickupFuture.get();
            ApiResponse<DistanceResult> distance = distanceFuture.get();
            List<GetProviders> providers = providersFuture.get();

            List<ModelQuoteInput> modelQuoteInputList = new ArrayList<>();
            System.out.println("dropoffLocation: " + dropoffLocation.getData().getState());
            System.out.println("pickupLocation: " + pickupLocation.getData().getState());
            System.out.println("distance: " + distance.getData());
            // same state and local delivery
            if (dropoffLocation.getData().getState().equals(pickupLocation.getData().getState())) {
                for (GetProviders provider : providers) {
                    if (provider.supportsHyperlocal() && provider.supportsIntercity()) {
                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                        ModelQuoteInput modelQuoteInput = new ModelQuoteInput(provider.providerCode(),
                                distance.getData().getDistanceMeters(), 2, count > 10 ? true : false, count);
                        modelQuoteInputList.add(modelQuoteInput);
                    }
                }
            } else if (dropoffLocation.getData().getCountry().equals(pickupLocation.getData().getCountry())) {
                for (GetProviders provider : providers) {
                    if (provider.supportsIntercity()) {
                        int count = orderRepository.countActiveOrdersByProvider(provider.providerCode());
                        ModelQuoteInput modelQuoteInput = new ModelQuoteInput(provider.providerName(),
                                distance.getData().getDistanceMeters(), 2, count > 10 ? true : false, count);
                        modelQuoteInputList.add(modelQuoteInput);
                    }
                }
            } else {
                throw new RuntimeException("International delivery Coming soon");
            }
            System.out.println(modelQuoteInputList);

            // Call ML Service for predictions
            String mlUrl = mlServicesUrl + "/predict/assignment";
            MLPredictionRequest mlRequest = new MLPredictionRequest(modelQuoteInputList);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<MLPredictionRequest> requestEntity = new HttpEntity<>(
                    mlRequest, headers);

            try {
                ResponseEntity<MLPredictionResponse> response = restTemplate
                        .postForEntity(mlUrl, requestEntity, MLPredictionResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    MLPredictionResponse predictionResponse = response.getBody();
                    System.out.println("ML Response: " + predictionResponse);
                    Prediction prediction = predictionResponse.getPredictions().stream()
                            .max(Comparator.comparing(Prediction::getSuccessProbability)).orElse(null);
                    
                    if (prediction != null && prediction.getSuccessProbability() > mlThreshold) {
                         System.out.println("Selected provider via ML: " + prediction.getProvider());
                        return providerInterface.getQuote(token, prediction.getProvider(), quoteInput);
                    } else {
                        System.out.println("ML output below threshold or empty. Falling back to default logic.");
                    }

                } else {
                    System.err.println("ML Service returned non-success status: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Failed to call ML Service: " + e.getMessage());
                // Non-blocking failure: proceed with default logic if ML fails
            }

            // Fallback logic: Select provider with minimum load (active orders)
            // If modelQuoteInputList is empty, this means no providers supported the route.
            if (modelQuoteInputList.isEmpty()) {
                 throw new RuntimeException("No suitable providers found for this route.");
            }
            
            ModelQuoteInput fallbackProvider = modelQuoteInputList.stream()
                .min(Comparator.comparingInt(ModelQuoteInput::provider_load))
                .orElse(modelQuoteInputList.get(0));

            System.out.println("Selected fallback provider: " + fallbackProvider.provider());
            return providerInterface.getQuote(token, fallbackProvider.provider(), quoteInput);

        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout while fetching data from external services", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while processing quote: " + e.getMessage(), e);
        }
    }

}
