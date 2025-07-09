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
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import org.slf4j.Logger;

@Configuration
@EnableCaching // Включить кэширование. Ищет @Cacheable, @CachePut, @CacheEvict и будет кэшировать их результаты
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

   /* @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.username}")
    private String redisUsername;
*/

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        /* logger.info("Redis Config - Host: {}, Port: {}, Username: {}, Password: {}",
                redisHost, redisPort, redisUsername, password);*/
        return createLettuceConnectionFactory();
    }

    private LettuceConnectionFactory createLettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        //config.setUsername(redisUsername);
       // config.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(config);
    }


    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = createConfiguredObjectMapper();

        // Включаем информацию о типе для корректной десериализации
        objectMapper.activateDefaultTyping( // !!! Чтобы Jackson сохранял информацию о Dto при сериализации.
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL//EVERYTHING,
                //JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        // Используем тот же ObjectMapper, что и в cacheManager
        ObjectMapper objectMapper = createConfiguredObjectMapper();
        objectMapper.activateDefaultTyping( // !!! Чтобы Jackson сохранял информацию о Dto при сериализации.
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL//EVERYTHING,
                //JsonTypeInfo.As.PROPERTY
        );
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        return template;
    }

    //  Для серелизации в JSON LocalDate создаём ObjectMapper и регистрируем модуль JavaTimeModule
    // Jackson по умолчанию не поддерживает сериализацию типов Java 8 Date/Time
    private ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Для поддержки LocalDate
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }


/*
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        logger.info("Redis Config - Host: {}, Port: {}, Username: {}, Password: {}",
                redisHost, redisPort, redisUsername, password);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        //config.setUsername(null); // Явно отключаем имя пользователя (не подключался Redis)
        config.setUsername(redisUsername); // Имя пользователя
        config.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        //  Для серелизации в JSON LocalDate создаём ObjectMapper и регистрируем модуль JavaTimeModule
        //Jackson по умолчанию не поддерживает сериализацию типов Java 8 Date/Time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Настраиваем сериализатор с поддержкой Java 8 Date/Time
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // Время жизни кэша - 60 минут
                .disableCachingNullValues() // Не кэшировать null значения
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }*/

}
