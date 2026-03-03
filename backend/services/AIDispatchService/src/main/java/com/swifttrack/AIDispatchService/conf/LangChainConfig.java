package com.swifttrack.AIDispatchService.conf;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain configuration for the AI Dispatch Service.
 * 
 * Wires Spring AI's ChatClient to the Ollama-backed ChatModel.
 * 
 * ChatClient is the high-level Spring AI abstraction that handles:
 * - Prompt template composition (system + user messages)
 * - Variable injection via .param()
 * - Model execution via .call()
 * - Structured output parsing via .entity()
 * 
 * The underlying ChatModel is auto-configured by spring-ai-starter-model-ollama
 * using properties from application.yaml:
 *   - base-url: http://localhost:11434
 *   - model: qwen2.5:3b-instruct
 *   - temperature: 0.1
 */
@Configuration
public class LangChainConfig {

    /**
     * ChatClient bean backed by Ollama ChatModel.
     * 
     * This is the single entry point for all LLM interactions.
     * Business code should never call ChatModel directly.
     */
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
