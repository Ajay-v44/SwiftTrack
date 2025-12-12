# External API Client - Usage Guide

## Overview

The `ExternalApiClient` is a common utility for making external HTTP API calls across all microservices. It provides a clean, reusable interface with built-in error handling and logging.

## Components

### 1. ExternalApiClient
Main utility class for making HTTP requests (GET, POST, PUT, PATCH, DELETE).

### 2. ApiResponse<T>
Generic wrapper that encapsulates response data, status codes, headers, and error messages.

### 3. HttpClientConfig
Configuration class providing RestTemplate beans with configurable timeouts.

## Setup in Your Microservice

### Step 1: Add Common Module Dependency
Ensure your microservice has the common module as a dependency in `pom.xml`:

```xml
<dependency>
    <groupId>com.swifttrack</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Step 2: Enable Component Scanning
Add the `com.swifttrack.http` package to your Spring Boot application scanning:

```java
@SpringBootApplication(scanBasePackages = {
    "com.swifttrack.YourService",
    "com.swifttrack.http"  // Scan common module's HTTP client utilities
})
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

## Usage Examples

### Basic GET Request

```java
@Service
public class ExternalDataService {
    
    private final ExternalApiClient apiClient;
    
    public ExternalDataService(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    public UserDto fetchUser(String userId) {
        ApiResponse<UserDto> response = apiClient.get(
            "https://api.example.com/users/" + userId,
            UserDto.class
        );
        
        if (response.isSuccessful()) {
            return response.getData();
        } else {
            throw new RuntimeException("Failed to fetch user: " + response.getErrorMessage());
        }
    }
}
```

### GET with Custom Headers

```java
public DataDto fetchDataWithAuth(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    
    ApiResponse<DataDto> response = apiClient.get(
        "https://api.example.com/data",
        headers,
        DataDto.class
    );
    
    return response.isSuccessful() ? response.getData() : null;
}
```

### GET with Query Parameters

```java
public List<ProductDto> searchProducts(String query, int page) {
    Map<String, String> params = new HashMap<>();
    params.put("q", query);
    params.put("page", String.valueOf(page));
    
    ApiResponse<ProductListDto> response = apiClient.get(
        "https://api.example.com/products/search",
        null,
        params,
        ProductListDto.class
    );
    
    return response.isSuccessful() ? response.getData().getProducts() : Collections.emptyList();
}
```

### POST Request

```java
public OrderDto createOrder(CreateOrderRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", apiKey);
    
    ApiResponse<OrderDto> response = apiClient.post(
        "https://api.example.com/orders",
        request,
        headers,
        OrderDto.class
    );
    
    if (response.isSuccessful()) {
        return response.getData();
    } else if (response.isClientError()) {
        throw new BadRequestException(response.getErrorMessage());
    } else {
        throw new ServiceException("Order creation failed: " + response.getErrorMessage());
    }
}
```

### PUT Request

```java
public UserDto updateUser(String userId, UpdateUserRequest request) {
    ApiResponse<UserDto> response = apiClient.put(
        "https://api.example.com/users/" + userId,
        request,
        UserDto.class
    );
    
    return response.isSuccessful() ? response.getData() : null;
}
```

### DELETE Request

```java
public boolean deleteResource(String resourceId) {
    ApiResponse<Void> response = apiClient.delete(
        "https://api.example.com/resources/" + resourceId,
        Void.class
    );
    
    return response.isSuccessful();
}
```

### Working with List Responses (Parameterized Types)

```java
public List<ProviderDto> fetchAllProviders() {
    ApiResponse<List<ProviderDto>> response = apiClient.get(
        "https://api.example.com/providers",
        null,
        new ParameterizedTypeReference<List<ProviderDto>>() {}
    );
    
    return response.isSuccessful() ? response.getData() : Collections.emptyList();
}
```

## Error Handling

The `ApiResponse` provides several convenience methods:

```java
ApiResponse<DataDto> response = apiClient.get(url, DataDto.class);

if (response.isSuccessful()) {
    // 2xx status code
    DataDto data = response.getData();
} else if (response.isClientError()) {
    // 4xx status code (bad request, unauthorized, not found, etc.)
    log.warn("Client error: {}", response.getErrorMessage());
} else if (response.isServerError()) {
    // 5xx status code (internal server error, service unavailable, etc.)
    log.error("Server error: {}", response.getErrorMessage());
} else if (response.getStatusCode() == 0) {
    // Connection failed or timeout
    log.error("Connection error: {}", response.getErrorMessage());
}
```

## Custom RestTemplate Configuration

If you need custom timeouts for specific use cases:

```java
// Create a custom RestTemplate with specific timeouts
RestTemplate customTemplate = HttpClientConfig.createRestTemplate(
    5000,  // 5 second connect timeout
    60000  // 60 second read timeout
);

// Use it directly or inject into ExternalApiClient
ExternalApiClient customClient = new ExternalApiClient(customTemplate);
```

---

# Production Deployment Strategy

## Common Module + Microservices Architecture

### Local Development

During local development:
1. The common module is built and installed to local Maven repository (`mvn install`)
2. Each microservice references the common module as a dependency
3. Changes to common module require rebuilding and reinstalling

### CI/CD Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                        Build Pipeline                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Build Common Module                                         │
│     └── mvn clean install -DskipTests                          │
│     └── Publish to Artifact Repository (Nexus/Artifactory)     │
│                                                                 │
│  2. Build Each Microservice (can run in parallel)               │
│     ├── mvn clean package -DskipTests                          │
│     └── Build Docker Image                                      │
│                                                                 │
│  3. Push Docker Images to Registry                              │
│     ├── AuthService:latest                                      │
│     ├── ProviderService:latest                                  │
│     ├── TenantService:latest                                    │
│     └── ...                                                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Production Deployment Options

#### Option 1: Embed Common in Each Service (Recommended for Microservices)

```
┌─────────────────────────────────────────────────────────────────┐
│                    Production Deployment                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Docker Image: auth-service:1.2.0                               │
│  ├── Base: eclipse-temurin:25-jre                              │
│  ├── AuthService.jar                                            │
│  │   └── Contains: AuthService classes + common module classes │
│  └── Size: ~150MB                                               │
│                                                                 │
│  Docker Image: provider-service:1.2.0                           │
│  ├── Base: eclipse-temurin:25-jre                              │
│  ├── ProviderService.jar                                        │
│  │   └── Contains: ProviderService classes + common classes    │
│  └── Size: ~150MB                                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Advantages:**
- Each service is self-contained
- Independent deployment
- No runtime dependency issues
- Easier scaling

**How it works:**
- Spring Boot Maven plugin packages all dependencies into a fat JAR
- Common module classes are included in each service's JAR
- No separate deployment of common module needed

#### Option 2: Shared Library via Maven Repository

```
┌─────────────────────────────────────────────────────────────────┐
│                     Artifact Repository                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  com.swifttrack:common:1.0.0                                   │
│  com.swifttrack:common:1.1.0                                   │
│  com.swifttrack:common:1.2.0  ← Current                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Build Time Dependency                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  AuthService (pom.xml)                                          │
│  <dependency>                                                   │
│      <groupId>com.swifttrack</groupId>                         │
│      <artifactId>common</artifactId>                           │
│      <version>1.2.0</version>                                  │
│  </dependency>                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Versioning Strategy

For production, use semantic versioning for the common module:

```xml
<!-- pom.xml -->
<version>1.2.3</version>
<!--         ^- Patch: bug fixes -->
<!--       ^--- Minor: new features, backward compatible -->
<!--    ^------ Major: breaking changes -->
```

**Best Practices:**
1. **Lock Versions**: Use specific versions, not SNAPSHOT, in production
2. **Backward Compatibility**: Avoid breaking changes in minor/patch versions
3. **Release Notes**: Document all changes to the common module
4. **Testing**: Test all services when common module changes

### Kubernetes Deployment Example

```yaml
# kubernetes/provider-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: provider-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: provider-service
  template:
    metadata:
      labels:
        app: provider-service
    spec:
      containers:
      - name: provider-service
        image: your-registry/provider-service:1.2.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

### Multi-Stage Dockerfile

```dockerfile
# Dockerfile for any microservice
FROM eclipse-temurin:25-jdk as build
WORKDIR /app

# Copy common module first (better layer caching)
COPY common/pom.xml common/pom.xml
COPY common/src common/src
RUN cd common && mvn clean install -DskipTests

# Copy and build the service
COPY services/ProviderService/pom.xml pom.xml
COPY services/ProviderService/src src
RUN mvn clean package -DskipTests

# Runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Summary

| Aspect | Local Development | Production |
|--------|------------------|------------|
| Common Module | SNAPSHOT version | Fixed version (1.2.0) |
| Build | Local `mvn install` | CI/CD pipeline |
| Artifact Storage | Local `.m2` | Nexus/Artifactory |
| Deployment | Individual services | Docker/Kubernetes |
| Updates | Rebuild + restart | Rolling deployment |
