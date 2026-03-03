package com.swifttrack.AIDispatchService.conf;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8010");
        server.setDescription("AI Dispatch Service Local Development");

        Contact contact = new Contact();
        contact.setName("SwiftTrack Team");
        contact.setEmail("support@swifttrack.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("AI Dispatch Service API")
                .version("1.0.0")
                .description("AI-based driver selection service for SwiftTrack platform. " +
                        "Uses LangChain + Ollama for LLM inference, LangSmith for prompt registry, " +
                        "Langfuse for observability, and pgvector for RAG retrieval.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .addServersItem(server)
                .info(info);
    }
}
