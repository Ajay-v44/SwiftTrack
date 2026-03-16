package com.swifttrack.AdminService.conf;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8009");
        server.setDescription("Admin Service Local Development");

        Contact contact = new Contact();
        contact.setName("SwiftTrack Team");
        contact.setEmail("support@swifttrack.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("SwiftTrack Admin Service API")
                .version("1.0.0")
                .description("Centralized administration portal for the SwiftTrack platform. " +
                        "Provides full control over users, drivers, providers, orders, billing, " +
                        "settlements, AI dispatch configuration, and platform monitoring. " +
                        "Access restricted to SUPER_ADMIN, SYSTEM_ADMIN, and ADMIN_USER roles.")
                .contact(contact)
                .license(license);

        SecurityScheme tokenScheme = new SecurityScheme()
                .name("token")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("SwiftTrack JWT token (from AuthService login)");

        return new OpenAPI()
                .addServersItem(server)
                .info(info)
                .components(new Components().addSecuritySchemes("token", tokenScheme))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }
}
