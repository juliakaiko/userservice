package com.mymicroservice.userservice;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UserServiceApplicationTests extends TestContainersConfig {

    @Test
    void testDataSource_ShouldProvideJdbcUrl_WhenContainersAreRunning() {
        assertNotNull(postgreSQLContainer.getJdbcUrl());
    }
}
