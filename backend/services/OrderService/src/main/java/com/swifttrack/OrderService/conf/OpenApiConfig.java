package com.swifttrack.OrderService.conf;

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
        server.setUrl("http://localhost:8005");
        server.setDescription("Order Service Local Development");

        Contact contact = new Contact();
        contact.setName("SwiftTrack Team");
        contact.setEmail("support@swifttrack.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Order Service API")
                .version("1.0.0")
                .description("Order management service for SwiftTrack platform. " +
                        "Provides APIs for order creation, tracking, status management, and order history.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .addServersItem(server)
                .info(info);
    }
}
