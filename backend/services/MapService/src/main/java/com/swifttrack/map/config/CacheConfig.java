package com.swifttrack.map.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration for Map Service
 */
@Configuration
public class CacheConfig implements CachingConfigurer {
    
    // Cache names
    public static final String GEOCODE_CACHE = "geocode";
    public static final String REVERSE_GEOCODE_CACHE = "reverse_geocode";
    public static final String ROUTE_CACHE = "route";
    public static final String MATRIX_CACHE = "matrix";
    public static final String ETA_CACHE = "eta";
    public static final String SNAP_CACHE = "snap";
    
    private final MapServiceProperties properties;
    
    public CacheConfig(MapServiceProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Custom ObjectMapper for Redis serialization that ignores unknown properties
     */
    private ObjectMapper cacheObjectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        // Configure to ignore unknown properties during deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Enable default typing for proper polymorphic deserialization
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        
        return mapper;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper()));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper()));
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper());
        
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();
        
        // Cache-specific configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put(GEOCODE_CACHE, defaultConfig.entryTtl(
            Duration.ofSeconds(properties.getCache().getGeocodeTtl())));
        
        cacheConfigurations.put(REVERSE_GEOCODE_CACHE, defaultConfig.entryTtl(
            Duration.ofSeconds(properties.getCache().getReverseGeocodeTtl())));
        
        cacheConfigurations.put(ROUTE_CACHE, defaultConfig.entryTtl(
            Duration.ofSeconds(properties.getCache().getRouteTtl())));
        
        cacheConfigurations.put(MATRIX_CACHE, defaultConfig.entryTtl(
            Duration.ofSeconds(properties.getCache().getMatrixTtl())));
        
        cacheConfigurations.put(ETA_CACHE, defaultConfig.entryTtl(
            Duration.ofSeconds(properties.getCache().getEtaTtl())));
        
        cacheConfigurations.put(SNAP_CACHE, defaultConfig.entryTtl(
            Duration.ofSeconds(properties.getCache().getRouteTtl())));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
}
