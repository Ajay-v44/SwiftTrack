import org.springframework.context.annotation.Configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;


@Configuration
public class OpenApiConfig {
@Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8003");
        server.setDescription("Tenant Service Local Development");

        Contact contact = new Contact();
        contact.setName("SwiftTrack Team");
        contact.setEmail("support@swifttrack.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Tenant Service API")
                .version("1.0.0")
                .description("Tenant management and authentication gateway service for SwiftTrack platform. " +
                        "Provides unified API for tenant operations including user authentication, tenant configuration, and service integration.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .addServersItem(server)
                .info(info);
    }
}
