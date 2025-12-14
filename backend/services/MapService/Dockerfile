# SwiftTrack Map Service Dockerfile
# Multi-stage build for optimized image size

# Stage 1: Build with common module
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy common module first
COPY common ./common

# Build common module with Java 21 (override the parent's Java 25 setting)
WORKDIR /build/common
RUN mvn clean install -DskipTests -B -Djava.version=21 -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 -Dmaven.compiler.release=21

# Copy MapService
WORKDIR /build/mapservice
COPY services/MapService/pom.xml .
COPY services/MapService/src ./src

# Build MapService (already configured for Java 21)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S swifttrack && adduser -S swifttrack -G swifttrack
USER swifttrack

# Copy JAR from builder
COPY --from=builder /build/mapservice/target/*.jar app.jar

# Environment variables with defaults
ENV SERVER_PORT=8006 \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    NOMINATIM_URL=https://nominatim.openstreetmap.org \
    OSRM_URL=https://router.project-osrm.org \
    EUREKA_URL=http://eureka:8761/eureka/ \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Expose port
EXPOSE 8006

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8006/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
