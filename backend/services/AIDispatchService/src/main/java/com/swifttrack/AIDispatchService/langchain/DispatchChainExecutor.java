package com.swifttrack.AIDispatchService.langchain;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import com.swifttrack.AIDispatchService.dto.LlmDecision;

import lombok.RequiredArgsConstructor;

/**
 * LangChain-powered AI execution engine for the dispatch pipeline.
 * 
 * Replaces the old manual approach (PromptAssembler + OllamaInferenceClient)
 * with
 * Spring AI's ChatClient abstraction which handles:
 * 
 * 1. Prompt composition — SystemPromptTemplate + UserMessage with variables
 * 2. Variable injection — via .param() instead of .replace()
 * 3. Model execution — via .call() backed by Ollama ChatModel
 * 4. Structured output parsing — via BeanOutputConverter + .entity()
 * 
 * Business logic (data fetching, fallback, Langfuse) stays in DispatchService.
 * This class ONLY handles the AI execution lifecycle.
 */
@Component
@RequiredArgsConstructor
public class DispatchChainExecutor {

    private static final Logger log = LoggerFactory.getLogger(DispatchChainExecutor.class);

    private final ChatClient chatClient;
    private final LangSmithPromptFetcher promptFetcher;

    /**
     * Execution result wrapping the decision + metadata.
     */
    public record ChainResult(
            LlmDecision decision,
            String rawOutput,
            long latencyMs,
            boolean parsed) {
    }

    /**
     * Execute the full LangChain dispatch chain:
     * 
     * 1. Fetch system prompt template from LangSmith
     * 2. Fetch decision prompt template from LangSmith
     * 3. Create ChatPromptTemplate using system + user messages
     * 4. Inject variables: driver_profiles, driver_memories
     * 5. Execute model via ChatClient (backed by Ollama)
     * 6. Parse structured JSON response via BeanOutputConverter → LlmDecision
     *
     * @param driverProfilesJson serialized driver profiles
     * @param driverMemoriesJson serialized driver memory summaries
     * @return ChainResult with decision, raw output, and latency
     */
    public ChainResult executeDispatchChain(String driverProfilesJson, String driverMemoriesJson) {
        Instant start = Instant.now();

        try {
            // Step 1 & 2: Fetch prompt templates from LangSmith
            String systemTemplate = promptFetcher.fetchPrompt("dispatch_system_v1");
            String decisionTemplate = promptFetcher.fetchPrompt("dispatch_decision_v1");

            // Step 3-6: LangChain templates from Hub contain raw JSON braces which crack
            // Spring's PromptTemplate parser.
            // We manually resolve parameters and inject literal Messages to bypass the
            // parser constraint.
            String resolvedDecision = decisionTemplate
                    .replace("{driver_profiles}", driverProfilesJson != null ? driverProfilesJson : "[]")
                    .replace("{driver_memory}", driverMemoriesJson != null ? driverMemoriesJson : "{}")
                    .replace("{tenant_policies}", "Standard matching rules apply")
                    .replace("{pickup_zone}", "Default Zone")
                    .replace("{order_priority}", "Standard")
                    .replace("{order_value}", "Medium");

            LlmDecision decision = chatClient.prompt()
                    .messages(
                            new org.springframework.ai.chat.messages.SystemMessage(systemTemplate),
                            new org.springframework.ai.chat.messages.UserMessage(resolvedDecision))
                    .call()
                    .entity(LlmDecision.class);

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            log.info("Dispatch chain completed in {}ms", latencyMs);

            // Capture raw output for observability (reconstruct from decision)
            String rawOutput = formatDecisionAsJson(decision);

            return new ChainResult(decision, rawOutput, latencyMs, true);

        } catch (Exception e) {
            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            log.warn("Dispatch chain failed after {}ms: {}", latencyMs, e.getMessage());

            // Attempt to extract raw output from the exception for validation retry
            String rawOutput = extractRawOutputFromException(e);
            return new ChainResult(null, rawOutput, latencyMs, false);
        }
    }

    /**
     * Validation chain: re-run malformed output through dispatch_validator_v1.
     * 
     * Uses LangChain structured parsing to attempt correction.
     *
     * @param rawOutput the malformed LLM output
     * @return ChainResult with corrected decision
     */
    public ChainResult executeValidationChain(String rawOutput) {
        Instant start = Instant.now();

        try {
            String systemTemplate = promptFetcher.fetchPrompt("dispatch_system_v1");
            String validatorTemplate = promptFetcher.fetchPrompt("dispatch_validator_v1");

            String resolvedValidator = validatorTemplate
                    .replace("{model_output}", rawOutput != null ? rawOutput : "")
                    .replace("{question}", "Please parse and fix the JSON.");

            LlmDecision decision = chatClient.prompt()
                    .messages(
                            new org.springframework.ai.chat.messages.SystemMessage(systemTemplate),
                            new org.springframework.ai.chat.messages.UserMessage(resolvedValidator))
                    .call()
                    .entity(LlmDecision.class);

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            log.info("Validation chain completed in {}ms", latencyMs);

            String correctedOutput = formatDecisionAsJson(decision);
            return new ChainResult(decision, correctedOutput, latencyMs, true);

        } catch (Exception e) {
            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            log.warn("Validation chain failed after {}ms: {}", latencyMs, e.getMessage());
            return new ChainResult(null, rawOutput, latencyMs, false);
        }
    }

    /**
     * Get the system prompt text for Langfuse observability logging.
     */
    public String getSystemPromptText() {
        return promptFetcher.fetchPrompt("dispatch_system_v1");
    }

    /**
     * Get the decision prompt text for Langfuse observability logging.
     */
    public String getDecisionPromptText() {
        return promptFetcher.fetchPrompt("dispatch_decision_v1");
    }

    // ─── Private Helpers ────────────────────────────────────────────────────

    private String formatDecisionAsJson(LlmDecision decision) {
        if (decision == null)
            return null;
        return String.format(
                "{\"driver_id\":\"%s\",\"confidence\":%.2f,\"reason\":\"%s\"}",
                decision.driverId(),
                decision.confidence(),
                decision.reason() != null ? decision.reason().replace("\"", "\\\"") : "");
    }

    private String extractRawOutputFromException(Exception e) {
        // Spring AI wraps raw output in ConversionException messages
        String message = e.getMessage();
        if (message != null && message.contains("{")) {
            int start = message.indexOf('{');
            int end = message.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return message.substring(start, end + 1);
            }
        }
        return message;
    }
}
