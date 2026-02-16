# Distributed Caching Implementation (Order Service ↔ Driver Service)

This document details the implementation of a **Distributed Cache** using Redis to optimize communication between the Driver Service and Order Service.

## Architecture Overview

The Driver Service frequently polls the Order Service for:
1.  Assigned Orders (`getMyOrders`)
2.  Order Status Updates

To reduce the load on the Order Service's database and improve response times, a **centralized Redis instance** has been introduced. Both services share this Redis instance to cache frequently accessed data.

### Key Components

-   **Redis Container**: Added to `docker-compose-kafka.yml` on port `6379`.
-   **Order Service (Provider)**: Responsible for populating the cache and handling **cache eviction** (invalidation) when order data changes.
-   **Driver Service (Consumer)**: Checks the Redis cache first. If data is missing (Cache Miss), it calls the Order Service, which then populates the cache.

---

## Configuration

### 1. Dependencies

Both `OrderService` and `DriverService` include the Spring Boot Redis starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. Redis Serialization

To ensure compatibility between the two services (so Driver Service can read Java objects written by Order Service), we use **JSON Serialization**.

A `RedisConfig` class in both services configures the `RedisTemplate` to use `GenericJackson2JsonRedisSerializer`:

```java
@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(1)) // Default TTL
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
}
```

---

## Caching Strategy

### 1. Smart Polling (Driver Service)

The `DriverService` implements a **Cache-Aside** pattern with a "smart check":

```java
// DriverService.java -> getMyOrders

// 1. Check Redis Cache
String cacheKey = "driverOrders::" + orderIds.toString();
List<GetOrdersForDriver> cachedOrders = redisTemplate.opsForValue().get(cacheKey);

if (cachedOrders != null) {
    return cachedOrders; // Cache Hit
}

// 2. Cache Miss: Fetch from Order Service
List<GetOrdersForDriver> orders = orderInterface.getOrdersForDriver(...);

// 3. Populate Cache (handled by OrderService via @Cacheable or manually here for redundancy)
if (orders != null) {
    redisTemplate.opsForValue().set(cacheKey, orders);
}
```

### 2. Cache Eviction (Order Service)

Data consistency is critical. The cache must be invalidated whenever an order's status changes. This is handled in `OrderService` using `@CacheEvict`:

-   **Kafka Consumers (`OrderEventConsumer.java`)**:
    -   `driver-assigned`: Evicts cache for the specific order.
    -   `driver-canceled`: Evicts cache.
    -   `driver-location-updates`: Evicts cache to reflect new status/location.

-   **Service Methods**:
    -   `cancelOrder`: Evicts cache when an order is cancelled.

---

## How to Run

1.  **Start Redis**:
    Ensure the Redis container is running:
    ```bash
    docker-compose -f deployment/docker-compose-kafka.yml up -d redis
    ```

2.  **Restart Services**:
    Since dependencies and configurations have changed, you must restart both services:
    -   **Order Service**: `mvn spring-boot:run`
    -   **Driver Service**: `mvn spring-boot:run`

## Verification

To verify the cache is working:
1.  Check `DriverService` logs for:
    -   `Fetching orders from Redis Cache: driverOrders::[...]` (Cache Hit)
    -   `Cache miss. Fetching from Order Service...` (Cache Miss)
2.  Monitor Redis keys using `redis-cli`:
    ```bash
    docker exec -it redis redis-cli keys *
    ```
