package com.swifttrack.AIDispatchService.observability;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Langfuse observability client for tracing the entire dispatch pipeline.
 * 
 * Instruments spans for:
 * - fetch_prompts
 * - fetch_driver_profiles
 * - retrieve_driver_memory
 * - llm_inference
 * - validation
 * - fallback_if_triggered
 * 
 * Creates one trace per dispatch request.
 * All trace data is sent asynchronously to avoid blocking the dispatch
 * pipeline.
 */
@Component
public class LangfuseClient {

    private static final Logger log = LoggerFactory.getLogger(LangfuseClient.class);

    @Value("${langfuse.secret-key}")
    private String secretKey;

    @Value("${langfuse.public-key}")
    private String publicKey;

    @Value("${langfuse.base-url:https://cloud.langfuse.com}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient httpClient;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    @PostConstruct
    void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Create a new trace for a dispatch request.
     */
    @Async
    public void createTrace(String traceId, List<UUID> driverIds) {
        Map<String, Object> traceBody = Map.of(
                "id", traceId,
                "name", "dispatch_assign",
                "input", Map.of("driver_ids", driverIds.stream().map(UUID::toString).toList()),
                "metadata", Map.of(
                        "service", "ai-dispatch-service",
                        "timestamp", Instant.now().toString()));

        sendToLangfuse("/api/public/traces", traceBody);
    }

    /**
     * Log a span within a trace.
     */
    @Async
    public void logSpan(String traceId, String spanName, Object input, Object output,
            Instant startTime, Instant endTime, Map<String, Object> metadata) {
        Map<String, Object> spanBody = Map.ofEntries(
                Map.entry("traceId", traceId),
                Map.entry("name", spanName),
                Map.entry("startTime", startTime.toString()),
                Map.entry("endTime", endTime.toString()),
                Map.entry("input", input != null ? input : Map.of()),
                Map.entry("output", output != null ? output : Map.of()),
                Map.entry("metadata", metadata != null ? metadata : Map.of()));

        sendToLangfuse("/api/public/spans", spanBody);
    }

    /**
     * Log an LLM generation span with token usage and model details.
     */
    @Async
    public void logGeneration(String traceId, String promptText, String completion,
            String model, long latencyMs, Instant startTime, Instant endTime) {
        Map<String, Object> genBody = Map.ofEntries(
                Map.entry("traceId", traceId),
                Map.entry("name", "llm_inference"),
                Map.entry("model", model),
                Map.entry("startTime", startTime.toString()),
                Map.entry("endTime", endTime.toString()),
                Map.entry("input", promptText),
                Map.entry("output", completion != null ? completion : ""),
                Map.entry("metadata", Map.of(
                        "latency_ms", latencyMs,
                        "temperature", 0.1)));

        sendToLangfuse("/api/public/generations", genBody);
    }

    /**
     * Update the trace with the final output.
     */
    @Async
    public void updateTraceOutput(String traceId, Object output, Map<String, Object> metadata) {
        Map<String, Object> body = Map.of(
                "id", traceId,
                "output", output,
                "metadata", metadata != null ? metadata : Map.of());

        sendToLangfuse("/api/public/traces", body, "POST");
    }

    // ─── HTTP Helper ────────────────────────────────────────────────────────

    private void sendToLangfuse(String path, Map<String, Object> body) {
        sendToLangfuse(path, body, "POST");
    }

    private void sendToLangfuse(String path, Map<String, Object> body, String method) {
        try {
            String json = objectMapper.writeValueAsString(body);
            RequestBody requestBody = RequestBody.create(json, JSON_MEDIA_TYPE);

            String credential = Credentials.basic(publicKey, secretKey);

            Request.Builder builder = new Request.Builder()
                    .url(baseUrl + path)
                    .addHeader("Authorization", credential)
                    .addHeader("Content-Type", "application/json");

            if ("PATCH".equals(method)) {
                builder.patch(requestBody);
            } else {
                builder.post(requestBody);
            }

            try (Response response = httpClient.newCall(builder.build()).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("Langfuse {} {} returned {}: {}",
                            method, path, response.code(),
                            response.body() != null ? response.body().string() : "no body");
                } else {
                    log.debug("Langfuse {} {} succeeded", method, path);
                }
            }
        } catch (IOException e) {
            // Observability should never break the dispatch pipeline
            log.error("Failed to send to Langfuse {}: {}", path, e.getMessage());
        }
    }
}
