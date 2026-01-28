package com.swifttrack.DriverService.conf;

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
                server.setUrl("http://localhost:8007");
                server.setDescription("Driver Service Local Development");

                Contact contact = new Contact();
                contact.setName("SwiftTrack Team");
                contact.setEmail("support@swifttrack.com");

                License license = new License();
                license.setName("Apache 2.0");
                license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

                Info info = new Info()
                                .title("Driver Service API")
                                .version("1.0.0")
                                .description("Driver management and authentication gateway service for SwiftTrack platform. "
                                                +
                                                "Provides unified API for driver operations including user authentication, driver configuration, and service integration.")
                                .contact(contact)
                                .license(license);

                return new OpenAPI()
                                .addServersItem(server)
                                .info(info);
        }
}
