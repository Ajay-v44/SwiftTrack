package com.swifttrack.AIDispatchService.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.swifttrack.AIDispatchService.dto.DispatchRequest;
import com.swifttrack.AIDispatchService.dto.DispatchResponse;
import com.swifttrack.AIDispatchService.dto.DriverMemorySummary;
import com.swifttrack.AIDispatchService.dto.DriverProfile;
import com.swifttrack.AIDispatchService.dto.LlmDecision;
import com.swifttrack.AIDispatchService.langchain.DispatchChainExecutor;
import com.swifttrack.AIDispatchService.langchain.DispatchChainExecutor.ChainResult;
import com.swifttrack.AIDispatchService.langchain.DriverDataSerializer;
import com.swifttrack.AIDispatchService.observability.LangfuseClient;
import com.swifttrack.AIDispatchService.repositories.DriverMemoryRepository;
import com.swifttrack.AIDispatchService.repositories.DriverProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Core dispatch orchestration service.
 * 
 * This service handles ONLY business logic:
 * - Data retrieval (profiles, memories)
 * - Data serialization
 * - Timeout enforcement
 * - Candidate validation
 * - Fallback logic
 * - Observability
 * 
 * AI orchestration (prompt lifecycle, variable injection, model execution,
 * structured output parsing) is fully delegated to DispatchChainExecutor
 * which uses Spring AI's LangChain abstractions.
 * 
 * Clean separation:
 * - DispatchService          → business logic orchestration
 * - DispatchChainExecutor    → AI execution lifecycle (LangChain)
 * - DriverDataSerializer     → business data serialization
 * - LangSmithPromptFetcher   → prompt registry client
 * - LangfuseClient           → observability infrastructure
 */
@Service
@RequiredArgsConstructor
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    // ─── Business Dependencies ──────────────────────────────────────────────
    private final DriverProfileRepository driverProfileRepository;
    private final DriverMemoryRepository driverMemoryRepository;
    private final DriverDataSerializer dataSerializer;

    // ─── AI Orchestration ───────────────────────────────────────────────────
    private final DispatchChainExecutor chainExecutor;

    // ─── Infrastructure ─────────────────────────────────────────────────────
    private final LangfuseClient langfuseClient;

    @Value("${dispatch.max-drivers:5}")
    private int maxDrivers;

    @Value("${dispatch.max-memory-per-driver:3}")
    private int maxMemoryPerDriver;

    @Value("${dispatch.pipeline-timeout-ms:400}")
    private long pipelineTimeoutMs;

    /**
     * Execute the full AI dispatch pipeline.
     *
     * @param request incoming dispatch request with driver IDs
     * @return dispatch response with selected driver
     */
    public DispatchResponse dispatch(DispatchRequest request) {
        Instant pipelineStart = Instant.now();
        String traceId = UUID.randomUUID().toString();

        List<UUID> driverIds = request.driverIds().stream()
                .limit(maxDrivers)
                .toList();

        log.info("Dispatch pipeline started: traceId={}, drivers={}", traceId, driverIds.size());
        langfuseClient.createTrace(traceId, driverIds);

        try {
            // ─── Step 1: Fetch Driver Profiles (Business Logic) ─────────────────
            Instant step1Start = Instant.now();
            List<DriverProfile> profiles = driverProfileRepository.fetchDriverProfiles(driverIds);
            Instant step1End = Instant.now();

            if (profiles.isEmpty()) {
                log.warn("No driver profiles found, executing fallback");
                return buildFallbackFromIds(driverIds, traceId, pipelineStart, "No driver profiles found");
            }

            langfuseClient.logSpan(traceId, "fetch_driver_profiles",
                    Map.of("driver_ids", driverIds.stream().map(UUID::toString).toList()),
                    Map.of("profiles_found", profiles.size()),
                    step1Start, step1End, null);

            // ─── Step 2: Retrieve Driver Memories — RAG (Business Logic) ────────
            Instant step2Start = Instant.now();
            Map<UUID, List<DriverMemorySummary>> memoriesMap = retrieveDriverMemories(profiles);
            Instant step2End = Instant.now();

            int totalMemories = memoriesMap.values().stream().mapToInt(List::size).sum();
            langfuseClient.logSpan(traceId, "retrieve_driver_memory",
                    Map.of("driver_count", profiles.size(), "max_per_driver", maxMemoryPerDriver),
                    Map.of("total_memories_retrieved", totalMemories),
                    step2Start, step2End, null);

            // ─── Step 3: Serialize Data (Business Logic) ────────────────────────
            String driverProfilesJson = dataSerializer.serializeProfiles(profiles);
            String driverMemoriesJson = dataSerializer.serializeMemories(memoriesMap);

            // ─── Step 4: Check Timeout Budget ───────────────────────────────────
            long elapsed = Duration.between(pipelineStart, Instant.now()).toMillis();
            if (elapsed > pipelineTimeoutMs) {
                log.warn("Pipeline timeout exceeded before inference ({}ms > {}ms)",
                        elapsed, pipelineTimeoutMs);
                return buildFallbackFromProfiles(profiles, traceId, pipelineStart,
                        "Pipeline timeout before inference");
            }

            // ─── Step 5: Execute LangChain Dispatch Chain (AI Orchestration) ────
            Instant step5Start = Instant.now();
            ChainResult result = chainExecutor.executeDispatchChain(driverProfilesJson, driverMemoriesJson);
            Instant step5End = Instant.now();

            langfuseClient.logGeneration(traceId,
                    chainExecutor.getSystemPromptText() + "\n\n" + chainExecutor.getDecisionPromptText(),
                    result.rawOutput(),
                    "qwen2.5:3b-instruct",
                    result.latencyMs(),
                    step5Start, step5End);

            // ─── Step 6: Validation Retry (if LangChain parsing failed) ─────────
            LlmDecision decision = result.decision();

            if (decision == null && result.rawOutput() != null) {
                log.info("LangChain structured parsing failed, attempting validation chain");
                Instant step6Start = Instant.now();

                ChainResult validationResult = chainExecutor.executeValidationChain(result.rawOutput());
                Instant step6End = Instant.now();

                langfuseClient.logSpan(traceId, "validation",
                        Map.of("raw_output", result.rawOutput()),
                        Map.of("corrected_output",
                                validationResult.rawOutput() != null ? validationResult.rawOutput() : "",
                                "success", validationResult.parsed()),
                        step6Start, step6End, null);

                decision = validationResult.decision();
            }

            // ─── Step 7: Validate Selected Driver ∈ Candidate List ──────────────
            if (decision != null) {
                String selectedDriverId = decision.driverId();
                boolean validDriver = driverIds.stream()
                        .anyMatch(id -> id.toString().equals(selectedDriverId));

                if (!validDriver) {
                    log.warn("LLM selected unknown driver_id '{}', falling back", selectedDriverId);
                    decision = null;
                }
            }

            // ─── Step 8: Fallback if Still Invalid ──────────────────────────────
            if (decision == null) {
                return buildFallbackFromProfiles(profiles, traceId, pipelineStart,
                        "LLM output validation failed");
            }

            // ─── Success Response ───────────────────────────────────────────────
            long totalLatency = Duration.between(pipelineStart, Instant.now()).toMillis();

            DispatchResponse response = DispatchResponse.builder()
                    .driverId(decision.driverId())
                    .confidence(decision.confidence())
                    .reason(decision.reason())
                    .fallback(false)
                    .latencyMs(totalLatency)
                    .build();

            langfuseClient.updateTraceOutput(traceId, response,
                    Map.of("total_latency_ms", totalLatency,
                            "fallback", false,
                            "inference_latency_ms", result.latencyMs()));

            log.info("Dispatch completed: driver={}, confidence={}, latency={}ms",
                    response.getDriverId(), response.getConfidence(), totalLatency);

            return response;

        } catch (Exception e) {
            log.error("Dispatch pipeline error: {}", e.getMessage(), e);

            langfuseClient.logSpan(traceId, "pipeline_error",
                    Map.of("error", e.getMessage()),
                    Map.of(),
                    pipelineStart, Instant.now(),
                    Map.of("exception_class", e.getClass().getSimpleName()));

            return buildFallbackFromIds(driverIds, traceId, pipelineStart,
                    "Pipeline error: " + e.getMessage());
        }
    }

    // ─── Business Logic Helpers ─────────────────────────────────────────────

    /**
     * Retrieve RAG memories for all profiled drivers.
     * pgvector similarity search with recency fallback.
     */
    private Map<UUID, List<DriverMemorySummary>> retrieveDriverMemories(List<DriverProfile> profiles) {
        Map<UUID, List<DriverMemorySummary>> memoriesMap = new HashMap<>();

        for (DriverProfile profile : profiles) {
            try {
                List<DriverMemorySummary> memories = driverMemoryRepository
                        .findTopMemories(profile.getDriverId(), maxMemoryPerDriver);

                if (memories.isEmpty()) {
                    memories = driverMemoryRepository
                            .findRecentMemories(profile.getDriverId(), maxMemoryPerDriver);
                }

                memoriesMap.put(profile.getDriverId(), memories);
            } catch (Exception e) {
                log.warn("Failed to retrieve memories for driver {}: {}",
                        profile.getDriverId(), e.getMessage());
                memoriesMap.put(profile.getDriverId(), List.of());
            }
        }

        return memoriesMap;
    }

    // ─── Fallback Logic ─────────────────────────────────────────────────────

    /**
     * Deterministic fallback using driver profiles:
     * lowest cancellation_rate → shortest distance.
     */
    private DispatchResponse buildFallbackFromProfiles(
            List<DriverProfile> profiles, String traceId,
            Instant pipelineStart, String fallbackReason) {

        Instant fbStart = Instant.now();

        DriverProfile selected = profiles.stream()
                .min(Comparator.comparingDouble(DriverProfile::getCancellationRate)
                        .thenComparing(DriverProfile::getDistance))
                .orElse(profiles.getFirst());

        long totalLatency = Duration.between(pipelineStart, Instant.now()).toMillis();

        DispatchResponse response = DispatchResponse.builder()
                .driverId(selected.getDriverId().toString())
                .confidence(0.5)
                .reason("Fallback: " + fallbackReason +
                        ". Selected driver with lowest cancellation rate (" +
                        String.format("%.2f", selected.getCancellationRate()) + ")")
                .fallback(true)
                .latencyMs(totalLatency)
                .build();

        langfuseClient.logSpan(traceId, "fallback_if_triggered",
                Map.of("reason", fallbackReason, "candidate_count", profiles.size()),
                Map.of("selected_driver", selected.getDriverId().toString()),
                fbStart, Instant.now(),
                Map.of("fallback_strategy", "lowest_cancellation_rate_then_shortest_distance"));

        langfuseClient.updateTraceOutput(traceId, response,
                Map.of("total_latency_ms", totalLatency,
                        "fallback", true, "fallback_reason", fallbackReason));

        log.warn("Fallback dispatch: driver={}, reason={}", selected.getDriverId(), fallbackReason);
        return response;
    }

    /**
     * Emergency fallback when no profiles exist.
     */
    private DispatchResponse buildFallbackFromIds(
            List<UUID> driverIds, String traceId,
            Instant pipelineStart, String fallbackReason) {

        long totalLatency = Duration.between(pipelineStart, Instant.now()).toMillis();
        UUID selectedId = driverIds.isEmpty() ? UUID.randomUUID() : driverIds.getFirst();

        DispatchResponse response = DispatchResponse.builder()
                .driverId(selectedId.toString())
                .confidence(0.3)
                .reason("Emergency fallback: " + fallbackReason +
                        ". Selected first available driver.")
                .fallback(true)
                .latencyMs(totalLatency)
                .build();

        langfuseClient.logSpan(traceId, "fallback_if_triggered",
                Map.of("reason", fallbackReason, "only_ids", true),
                Map.of("selected_driver", selectedId.toString()),
                pipelineStart, Instant.now(),
                Map.of("fallback_strategy", "first_available_driver"));

        langfuseClient.updateTraceOutput(traceId, response,
                Map.of("total_latency_ms", totalLatency,
                        "fallback", true, "fallback_reason", fallbackReason));

        return response;
    }
}
