package com.swifttrack.BillingAndSettlementService.conf;

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
        server.setUrl("http://localhost:8002");
        server.setDescription("Billing and Settlement Service Local Development");

        Contact contact = new Contact();
        contact.setName("SwiftTrack Team");
        contact.setEmail("support@swifttrack.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Billing and Settlement Service API")
                .version("1.0.0")
                .description("Billing and settlement management service for SwiftTrack platform. " +
                        "Provides APIs for invoice generation, payment processing, reconciliation, and settlement management.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .addServersItem(server)
                .info(info);
    }
}
