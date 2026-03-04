package com.swifttrack.AIDispatchService.langchain;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Utility to fetch prompt templates from LangSmith's prompt registry.
 * 
 * LangSmith is used ONLY as prompt storage.
 * We do NOT execute models via LangSmith.
 * 
 * Prompts fetched:
 * - dispatch_system_v1 (system context)
 * - dispatch_decision_v1 (decision instructions)
 * - dispatch_validator_v1 (output validation instructions)
 */
@Component
public class LangSmithPromptFetcher {

    private static final Logger log = LoggerFactory.getLogger(LangSmithPromptFetcher.class);

    @Value("${langsmith.api-key}")
    private String apiKey;

    @Value("${langsmith.base-url:https://api.smith.langchain.com}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient httpClient;

    /**
     * Local cache: prompt name → CacheEntry.
     * Caches prompts for a specific duration to balance latency and freshness.
     */
    private record CacheEntry(String template, long expireAt) {
    }

    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes TTL

    private final Map<String, CacheEntry> promptCache = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Fetch a prompt template by name from LangSmith API.
     * Uses the /prompts/{prompt_name} endpoint.
     * Results are cached in-memory after first successful fetch.
     *
     * @param promptName the registered prompt name (e.g. "dispatch_system_v1")
     * @return the prompt template string
     */
    public String fetchPrompt(String promptName) {
        // Check cache first
        CacheEntry cached = promptCache.get(promptName);
        if (cached != null && System.currentTimeMillis() < cached.expireAt()) {
            log.debug("LangSmith prompt cache hit: {}", promptName);
            return cached.template();
        }

        try {
            String url = baseUrl.replace("api.smith", "api.hub") + "/commits/-/" + promptName + "/latest";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("LangSmith API returned {} for prompt '{}'. Using fallback.",
                            response.code(), promptName);
                    return getFallbackPrompt(promptName);
                }

                String body = response.body() != null ? response.body().string() : null;
                if (body == null) {
                    log.warn("Empty response from LangSmith for prompt '{}'", promptName);
                    return getFallbackPrompt(promptName);
                }

                String template = extractTemplate(body, promptName);
                promptCache.put(promptName, new CacheEntry(template, System.currentTimeMillis() + CACHE_TTL_MS));
                log.info("Successfully fetched LangSmith prompt: {}", promptName);
                return template;
            }
        } catch (IOException e) {
            log.error("Failed to fetch prompt '{}' from LangSmith: {}", promptName, e.getMessage());
            return getFallbackPrompt(promptName);
        }
    }

    private void findTemplates(JsonNode node, StringBuilder sb) {
        if (node.isObject()) {
            if (node.has("template") && node.get("template").isTextual()) {
                sb.append(node.get("template").asText()).append("\n");
            }
            node.elements().forEachRemaining(child -> findTemplates(child, sb));
        } else if (node.isArray()) {
            node.elements().forEachRemaining(child -> findTemplates(child, sb));
        }
    }

    /**
     * Parse the LangSmith API response to extract the template string.
     */
    private String extractTemplate(String responseBody, String promptName) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            StringBuilder sb = new StringBuilder();
            findTemplates(root, sb);

            if (!sb.isEmpty()) {
                return sb.toString().trim();
            }

            log.warn("Could not parse LangSmith response structure for '{}', using raw body", promptName);
            return responseBody;
        } catch (Exception e) {
            log.error("Error parsing LangSmith response for '{}': {}", promptName, e.getMessage());
            return getFallbackPrompt(promptName);
        }
    }

    /**
     * Hardcoded fallback prompts in case LangSmith is unreachable.
     */
    public String getFallbackPrompt(String promptName) {
        return switch (promptName) {
            case "dispatch_system_v1" -> FALLBACK_SYSTEM_PROMPT;
            case "dispatch_decision_v1" -> FALLBACK_DECISION_PROMPT;
            case "dispatch_validator_v1" -> FALLBACK_VALIDATOR_PROMPT;
            default -> "You are a helpful assistant.";
        };
    }

    /**
     * Invalidate prompt cache (useful for testing or forced refresh).
     */
    public void invalidateCache() {
        promptCache.clear();
        log.info("LangSmith prompt cache invalidated");
    }

    // ─── Fallback Prompt Templates ─────────────────────────────────────────

    private static final String FALLBACK_SYSTEM_PROMPT = """
            You are an AI dispatch optimizer for a logistics platform called SwiftTrack.
            Your role is to analyze driver metrics and select the optimal driver for order assignment.

            You must consider:
            - Driver distance to pickup (lower is better)
            - Acceptance rate (higher is better)
            - Cancellation rate (lower is better)
            - SLA adherence (higher is better)
            - Driver rating (higher is better)
            - Idle time (longer idle time = higher priority to keep drivers active)
            - Historical behavioral memory summaries (RAG context)

            You must respond with ONLY a JSON object. No markdown. No commentary. No extra text.
            """;

    private static final String FALLBACK_DECISION_PROMPT = """
            Analyze the following driver profiles and their behavioral memory summaries.
            Select the single most optimal driver for dispatch.

            DRIVER PROFILES:
            {driver_profiles}

            DRIVER MEMORY SUMMARIES:
            {driver_memories}

            SCORING WEIGHTS:
            - Distance: 25%
            - Acceptance Rate: 20%
            - Cancellation Rate: 20%
            - SLA Adherence: 15%
            - Rating: 10%
            - Idle Time: 10%

            Respond with ONLY this JSON format, nothing else:
            {
              "driver_id": "<selected_driver_uuid>",
              "confidence": <0.0-1.0>,
              "reason": "<brief technical explanation>"
            }
            """;

    private static final String FALLBACK_VALIDATOR_PROMPT = """
            The following text was supposed to be a valid JSON object with exactly these fields:
            - driver_id (string, UUID format)
            - confidence (number, 0.0 to 1.0)
            - reason (string, brief explanation)

            The raw output was:
            {raw_output}

            Extract or fix the JSON. Respond with ONLY the corrected JSON object. Nothing else.
            {
              "driver_id": "<uuid>",
              "confidence": <0.0-1.0>,
              "reason": "<explanation>"
            }
            """;
}
