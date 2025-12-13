# SwiftTrack Map Service

A production-ready geospatial microservice providing Google Maps-like functionality using **100% free and open-source tools**.

## 🌟 Features

### Geospatial APIs
- **Forward Geocoding** - Convert addresses to coordinates
- **Reverse Geocoding** - Convert coordinates to addresses
- **Directions API** - Get routes with turn-by-turn instructions
- **Distance Matrix API** - Calculate distances between multiple points
- **ETA Calculation** - Estimated time of arrival with traffic
- **Snap-to-Road** - Snap GPS coordinates to road network
- **Serviceability Check** - Validate if points are within service areas

### Travel Modes
- 🚗 **Driving** - Car/vehicle routing
- 🚶 **Walking** - Pedestrian routing
- 🚴 **Bike** - Bicycle routing
- 📦 **Delivery** - Delivery vehicle mode with stops

### Performance
- ⚡ Redis-based caching
- 🔄 Async WebClient calls
- 🔁 Automatic retry with fallback
- 📈 Rate limit protection

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.x |
| Language | Java 21 |
| HTTP Client | WebClient (reactive) |
| Caching | Redis |
| Geocoding | Nominatim (OSM) |
| Routing | OSRM / GraphHopper |
| Geometry | JTS Topology Suite |
| API Docs | SpringDoc OpenAPI |

## 📦 External Services

This service integrates with:

### Nominatim (Geocoding)
- Forward and reverse geocoding
- Public API: https://nominatim.openstreetmap.org
- Can be self-hosted for production

### OSRM (Routing)
- Route calculation and directions
- Distance matrix
- Snap-to-road (match API)
- Public API: https://router.project-osrm.org
- Can be self-hosted for production

### GraphHopper (Alternative Routing)
- Optional alternative to OSRM
- Better for some use cases
- Requires local installation or API key

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Redis (for caching)
- Common module built (`mvn install` in `common/`)

### Running Locally

```bash
# Clone the repository
cd services/MapService

# Set up environment variables
cp .env .env.local
# Edit .env.local with your configuration

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package -DskipTests
java -jar target/MapService-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f map-service
```

## 📡 API Endpoints

### Geocoding

```http
# Reverse Geocoding
GET /map/reverse?lat=12.9716&lng=77.5946

# Forward Geocoding
GET /map/search?query=MG Road, Bangalore&limit=5
```

### Routing

```http
# Get Directions
POST /map/directions
{
  "origin": { "lat": 12.9716, "lng": 77.5946 },
  "destination": { "lat": 12.9816, "lng": 77.6046 },
  "mode": "DRIVING"
}

# Simple directions (GET)
GET /map/directions?origin_lat=12.9716&origin_lng=77.5946&dest_lat=12.9816&dest_lng=77.6046&mode=DRIVING
```

### Distance Matrix

```http
POST /map/matrix
{
  "origins": [
    { "lat": 12.9716, "lng": 77.5946 }
  ],
  "destinations": [
    { "lat": 12.9816, "lng": 77.6046 },
    { "lat": 12.9916, "lng": 77.6146 }
  ],
  "mode": "DRIVING"
}
```

### ETA Calculation

```http
POST /map/eta
{
  "origin": { "lat": 12.9716, "lng": 77.5946 },
  "destination": { "lat": 12.9816, "lng": 77.6046 },
  "mode": "DELIVERY"
}
```

### Snap-to-Road

```http
POST /map/snap
{
  "path": [
    { "lat": 12.9716, "lng": 77.5946 },
    { "lat": 12.9720, "lng": 77.5950 }
  ],
  "radiusMeters": 50
}
```

### Serviceability Check

```http
POST /map/inside-area
{
  "point": { "lat": 12.9716, "lng": 77.5946 },
  "polygon": [
    { "lat": 12.9, "lng": 77.5 },
    { "lat": 12.9, "lng": 77.7 },
    { "lat": 13.1, "lng": 77.7 },
    { "lat": 13.1, "lng": 77.5 }
  ]
}
```

## 📖 API Documentation

When running, access Swagger UI at:
- Local: http://localhost:8006/swagger-ui.html
- Via Gateway: http://localhost:8080/mapservice/swagger-ui.html

## ⚙️ Configuration

Key configuration options in `application.yaml`:

```yaml
map:
  nominatim:
    base-url: https://nominatim.openstreetmap.org
    use-local: false  # Set to true for local Nominatim
    
  osrm:
    base-url: https://router.project-osrm.org
    use-local: false  # Set to true for local OSRM
    
  routing-engine: osrm  # or graphhopper
  
  cache:
    geocode-ttl: 86400      # 24 hours
    route-ttl: 3600         # 1 hour
    matrix-ttl: 1800        # 30 minutes
    eta-ttl: 600            # 10 minutes
```

## 🏗️ Project Structure

```
src/main/java/com/swifttrack/map/
├── MapServiceApplication.java
├── client/
│   ├── NominatimClient.java
│   ├── OsrmClient.java
│   └── GraphHopperClient.java
├── config/
│   ├── CacheConfig.java
│   ├── MapServiceProperties.java
│   ├── OpenApiConfig.java
│   └── WebClientConfig.java
├── controller/
│   ├── HealthController.java
│   └── MapController.java
├── dto/
│   ├── Coordinates.java
│   ├── EtaResponse.java
│   ├── MatrixResponse.java
│   ├── NormalizedLocation.java
│   ├── RouteResponse.java
│   ├── SnapToRoadResponse.java
│   ├── request/
│   └── response/
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ...
├── service/
│   ├── EtaService.java
│   ├── GeocodingService.java
│   ├── MatrixService.java
│   ├── RoutingService.java
│   ├── ServiceabilityService.java
│   └── SnapToRoadService.java
└── util/
    ├── CacheKeyGenerator.java
    ├── GeoUtils.java
    └── PolylineUtils.java
```

## 🔌 Integration with SwiftTrack Services

This service is used by:

| Service | Use Case |
|---------|----------|
| **Order Service** | Distance calculation, ETA scoring |
| **Provider Service** | Serviceability validation, route quotes |
| **Assignment Engine** | ML features (distances, ETA, road types) |
| **Tracking Service** | Snap-to-road, real-time ETA |

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run integration tests
mvn verify -P integration-test
```

## 📊 Metrics & Monitoring

Health endpoints:
- `/health` - Basic health check
- `/info` - Service information
- `/ready` - Readiness probe

Actuator endpoints (if enabled):
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

## 🔒 Security Considerations

1. **Rate Limiting**: Public OSM APIs are rate-limited. Use local instances for production.
2. **Caching**: All responses are cached to reduce API calls.
3. **User-Agent**: Always send proper User-Agent to OSM APIs (configured by default).

## 📝 License

MIT License - See LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📞 Support

For issues or questions, contact the SwiftTrack team.
