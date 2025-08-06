package com.mymicroservice.userservice.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import org.slf4j.Logger;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
         logger.info("Redis Config - Host: {}, Port: {}", redisHost, redisPort);
        return createLettuceConnectionFactory();
    }

    private LettuceConnectionFactory createLettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Creates and configures a {@link RedisCacheManager} bean for declarative caching using annotations.
     * This cache manager uses Redis as the underlying cache store with JSON serialization.
     *
     * <p>Key features:
     * <ul>
     *     <li>Uses Jackson-based JSON serialization for cache values.</li>
     *     <li>Sets a default Time-To-Live (TTL) of 15 minutes for all cache entries.</li>
     *     <li>Prevents caching of {@code null} values to avoid cache pollution.</li>
     *     <li>Configures a customized {@link ObjectMapper} for consistent JSON handling.</li>
     * </ul>
     *
     * @param connectionFactory the {@link RedisConnectionFactory} used to establish Redis connections
     * @return a fully configured {@link RedisCacheManager} instance
     * @see org.springframework.cache.annotation.EnableCaching
     * @see org.springframework.data.redis.cache.RedisCacheConfiguration
     *
     * @implNote The cache manager uses {@link GenericJackson2JsonRedisSerializer} for value serialization,
     *           which stores type information in the JSON payload for proper deserialization.
     *           The default TTL (15 minutes) can be overridden per-cache via {@code @Cacheable} annotations.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) { //for declarative caching (via annotations)
        ObjectMapper objectMapper = createConfiguredObjectMapper();

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) { //for programmatic work with Redis
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = createConfiguredObjectMapper();

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        return template;
    }

    /**
     * Creates a preconfigured {@link ObjectMapper} for Redis JSON serialization.
     * <p>Features:
     * <ul>
     *     <li>Supports Java 8 Date/Time via {@link JavaTimeModule}</li>
     *     <li>Ignores unknown JSON properties</li>
     *     <li>Preserves type info for polymorphic DTOs</li>
     * </ul>
     * <p>
     *  Enables a mechanism that adds class metadata to JSON (as an {@code @class} field),
     *  allowing proper deserialization of objects back to their original type.
     *  <p>
     *  Solves the {@code ClassCastException} issue where Jackson by default
     *  deserializes complex objects into {@code LinkedHashMap} instead of the target DTO class.
     *  <p>
     *  Example of the problem this solves:
     *  {@code java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast
     *  to com.mymicroservice.userservice.dto.UserDto}
     *
     * @return configured ObjectMapper instance
     */
    private ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // serialization in JSON LocalDate.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.activateDefaultTyping( // helps deserialization to Dto
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL //no adding data about !!!type of non-final classes (String, Integer, int)
        );
        return objectMapper;
    }
}
