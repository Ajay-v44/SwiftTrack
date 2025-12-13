package com.swifttrack.map.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Map Service
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8006}")
    private String serverPort;
    
    @Bean
    public OpenAPI mapServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SwiftTrack Map Service API")
                .description("""
                    ## Map Service API Documentation
                    
                    This microservice provides comprehensive geospatial APIs powered by OpenStreetMap:
                    
                    ### Features:
                    - **Geocoding**: Convert addresses to coordinates (Forward Geocoding)
                    - **Reverse Geocoding**: Convert coordinates to addresses
                    - **Routing**: Get directions between two points
                    - **Distance Matrix**: Calculate distances/durations between multiple points
                    - **ETA**: Estimated Time of Arrival calculations
                    - **Snap-to-Road**: Snap GPS coordinates to road network
                    - **Serviceability**: Check if a point is inside a service area
                    
                    ### Travel Modes:
                    - `DRIVING` - Car/vehicle routing
                    - `WALKING` - Pedestrian routing
                    - `BIKE` - Bicycle routing
                    - `DELIVERY` - Delivery vehicle routing (slower for stops)
                    
                    ### External Services:
                    - **Nominatim** - Geocoding
                    - **OSRM** - Routing (primary)
                    - **GraphHopper** - Routing (alternative)
                    
                    All responses are cached for optimal performance.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("SwiftTrack Team")
                    .email("team@swifttrack.io")
                    .url("https://swifttrack.io"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local Development Server"),
                new Server()
                    .url("http://localhost:8080/mapservice")
                    .description("Gateway Development Server")
            ));
    }
}
