package com.mymicroservice.userservice;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import com.mymicroservice.userservice.configuration.JwtDecoderTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
//@Import(TestContainersConfig.class)
@Import({TestContainersConfig.class, JwtDecoderTestConfig.class})
@Testcontainers(disabledWithoutDocker = true)
class UserServiceApplicationTests extends TestContainersConfig{

	@Test
	void contextLoads() {
	}

	@Test
	void testDataSource() {
		System.out.println("PostgreSQL JDBC URL: " + TestContainersConfig.postgreSQLContainer.getJdbcUrl());
	}

}
