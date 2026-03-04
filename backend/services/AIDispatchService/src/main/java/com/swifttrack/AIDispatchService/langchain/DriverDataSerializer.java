package com.swifttrack.AIDispatchService.langchain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifttrack.AIDispatchService.dto.DriverMemorySummary;
import com.swifttrack.AIDispatchService.dto.DriverProfile;

/**
 * Serializes driver business data into clean JSON strings for LLM consumption.
 * 
 * This is a BUSINESS LOGIC component — it knows how to format driver data.
 * It does NOT know about prompts, templates, or LLM execution.
 * 
 * Separation of concerns:
 * - DriverDataSerializer: business data → JSON strings
 * - DispatchChainExecutor: prompts + model + parsing (LangChain)
 */
@Component
public class DriverDataSerializer {

    private static final Logger log = LoggerFactory.getLogger(DriverDataSerializer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serialize driver profiles into a clean JSON array.
     * Only exposes metrics the LLM needs — never raw DB columns.
     *
     * @param profiles list of structured driver profiles
     * @return pretty-printed JSON array string
     */
    public String serializeProfiles(List<DriverProfile> profiles) {
        try {
            var cleanProfiles = profiles.stream()
                    .map(p -> Map.of(
                            "driver_id", p.getDriverId().toString(),
                            "distance_km", p.getDistance(),
                            "acceptance_rate", String.format("%.2f", p.getAcceptanceRate()),
                            "cancellation_rate", String.format("%.2f", p.getCancellationRate()),
                            "sla_adherence", String.format("%.2f", p.getSlaAdherence()),
                            "rating", String.format("%.1f", p.getRating()),
                            "idle_time_minutes", p.getIdleTimeMinutes()))
                    .toList();

            return objectMapper.writeValueAsString(cleanProfiles);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize driver profiles", e);
            return profiles.toString();
        }
    }

    /**
     * Serialize driver memory summaries grouped by driver ID.
     * Only exposes summary text — never embeddings or internal IDs.
     *
     * @param memoriesMap driver ID → list of memory summaries
     * @return pretty-printed JSON object string
     */
    public String serializeMemories(Map<UUID, List<DriverMemorySummary>> memoriesMap) {
        try {
            var cleanMemories = memoriesMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            e -> e.getValue().stream()
                                    .map(DriverMemorySummary::getSummary)
                                    .toList()));

            return objectMapper.writeValueAsString(cleanMemories);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize driver memories", e);
            return memoriesMap.toString();
        }
    }
}
