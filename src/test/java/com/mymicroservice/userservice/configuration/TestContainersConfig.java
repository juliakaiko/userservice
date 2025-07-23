package com.mymicroservice.userservice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public class TestContainersConfig {

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine") //postgres:15
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("password")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1)
                .withStartupTimeout(Duration.ofSeconds(120))
            );

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static {
        postgreSQLContainer.start();
        redisContainer.start();
        log.info("PostgreSQL JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
        log.info("Redis Host: {}, Port: {}", redisContainer.getHost(), redisContainer.getMappedPort(6379));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        log.info("PostgreSQL URL: {}", jdbcUrl);
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redisContainer.getMappedPort(6379)));

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }
}
