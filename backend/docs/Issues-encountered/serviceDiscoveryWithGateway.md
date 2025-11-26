# Service Discovery with Gateway - Issue & Solution

## Problem Statement

When trying to access the AuthService through the API Gateway at `http://localhost:8080/authservice/test`, the request was not being routed properly even though:
- Both services were registered with Eureka Server
- Direct access to AuthService at `http://localhost:8001/test` worked fine
- Gateway was running and registered with Eureka

## Root Causes Identified

### 1. **Wrong Gateway Dependency** (Primary Issue)
The Gateway was using `spring-cloud-starter-gateway-server-webmvc` which is servlet-based (blocking I/O).

**Problem**: 
- WebMvc-based gateway doesn't support reactive routing patterns required by Spring Cloud Gateway
- Servlet-based approach is incompatible with non-blocking service discovery routing
- The gateway couldn't properly resolve and route to discovered services

**Solution**:
Changed to `spring-cloud-starter-gateway` which is reactive (non-blocking).

```xml
<!-- BEFORE (Wrong) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webmvc</artifactId>
</dependency>

<!-- AFTER (Correct) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

### 2. **Hostname Resolution Issues** (Secondary Issue)
Services were registering with their full network hostname (`Ajay.mshome.net`) instead of localhost/127.0.0.1.

**Problem**: 
- Gateway couldn't resolve hostnames when trying to route requests
- Service discovery returned one hostname but gateway tried to connect using another

**Solution**:
Added explicit IP address configuration in all services:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka/
  instance:
    hostname: 127.0.0.1
    prefer-ip-address: true
    ip-address: 127.0.0.1
    instance-id: ${spring.application.name}:${server.port}
```

### 3. **Version Incompatibilities** (Tertiary Issue)
Services had mismatched Spring Boot and Spring Cloud versions.

**Problem**:
- AuthService: Spring Boot 4.0.0 + Spring Cloud 2025.1.0-RC1
- EurekaServer: Spring Boot 4.0.0 + Spring Cloud 2025.1.0-RC1  
- Gateway: Spring Boot 3.5.8 + Spring Cloud 2025.0.0

**Solution**:
Standardized all services to use:
- Spring Boot: **3.5.8** (stable version)
- Spring Cloud: **2025.0.0** (stable version, not RC)

### 4. **Invalid Test Dependencies**
Both pom.xml files had non-existent test artifacts:
- `spring-boot-starter-actuator-test` (doesn't exist)
- `spring-boot-starter-data-jpa-test` (doesn't exist)
- `spring-boot-starter-webmvc-test` (doesn't exist)

**Solution**:
Replaced with the correct `spring-boot-starter-test` which includes everything needed.

## Configuration Changes Made

### Gateway Configuration (application.yaml)

```yaml
spring:
  application:
    name: GateWay
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: authservice
          uri: lb://authservice          # lowercase service name
          predicates:
            - Path=/authservice/**
          filters:
            - StripPrefix=1              # removes /authservice prefix before forwarding
server:
  port: 8080
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka/
  instance:
    hostname: 127.0.0.1
    prefer-ip-address: true
    ip-address: 127.0.0.1
    instance-id: ${spring.application.name}:${server.port}
```

### Key Routing Logic
- **Request**: `http://localhost:8080/authservice/test`
- **Gateway matches**: Path `/authservice/**`
- **StripPrefix=1**: Removes `/authservice`, forwards to `/test`
- **Load Balancer**: Uses `lb://authservice` to discover and route to AuthService via Eureka
- **Final URL**: `http://127.0.0.1:8001/test` (AuthService)

## How Service Discovery Works

1. **Services register with Eureka**:
   - AuthService registers as `AuthService:8001` at `127.0.0.1:8001`
   - Gateway registers as `GateWay:8080` at `127.0.0.1:8080`

2. **Gateway discovers services**:
   - Fetches registry from Eureka Server at startup
   - Maintains local cache of available services
   - Uses load balancer to resolve service names

3. **Request routing**:
   - Gateway receives request for `/authservice/test`
   - Matches against configured route predicate
   - Resolves `lb://authservice` to actual service location using Eureka cache
   - Applies filters (StripPrefix)
   - Forwards request to discovered service

## Testing the Solution

### Verify Registration
Access Eureka dashboard: `http://localhost:8761`

Expected output:
```
Instances currently registered with Eureka
AUTHSERVICE    UP (1) - AuthService:8001
GATEWAY        UP (1) - GateWay:8080
```

### Test Direct Access
```
GET http://localhost:8001/test
Response: "Test"
```

### Test Gateway Routing
```
GET http://localhost:8080/authservice/test
Response: "Test"
```

## Key Learnings

1. **Gateway Type Matters**: Use reactive gateway for service discovery routing, not servlet-based
2. **Hostname Resolution**: Use IP addresses (127.0.0.1) instead of hostnames for local development
3. **Version Consistency**: Keep Spring Boot and Spring Cloud versions consistent across all microservices
4. **Service Name Case**: Use lowercase in load balancer URIs when `lower-case-service-id: true` is set
5. **Route Order**: Explicit routes with StripPrefix are clearer than relying solely on discovery locator

## Files Modified

1. `services/GateWay/pom.xml` - Changed gateway dependency
2. `services/GateWay/src/main/resources/application.yaml` - Added routes + Eureka config
3. `services/AuthService/pom.xml` - Fixed versions and test dependencies
4. `services/AuthService/src/main/resources/application.yaml` - Added Eureka config
5. `services/EurekaServer/pom.xml` - Fixed versions and test dependencies
6. `services/EurekaServer/src/main/resources/application.yaml` - Added IP configuration
7. `services/GateWay/src/main/java/com/mircoservice/GateWay/GateWayApplication.java` - Added @EnableDiscoveryClient
8. `services/AuthService/src/main/java/com/swifttrack/AuthService/AuthServiceApplication.java` - Already had @EnableDiscoveryClient