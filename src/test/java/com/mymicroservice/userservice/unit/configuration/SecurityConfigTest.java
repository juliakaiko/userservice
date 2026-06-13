package com.mymicroservice.userservice.unit.configuration;

import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.filter.GatewayAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static com.mymicroservice.userservice.util.data.TestConstants.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class))
            .withUserConfiguration(SecurityConfig.class, GatewayAuthFilter.class)
            .withPropertyValues("security.public.endpoints=/actuator/**");

    @Test
    void passwordEncoder_ShouldEncodeWithBCrypt_WhenContextLoads() {
        contextRunner.run(context -> {
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
            assertNotNull(passwordEncoder);
            String encoded = passwordEncoder.encode(USER_PASSWORD);
            assertTrue(passwordEncoder.matches(USER_PASSWORD, encoded));
        });
    }

    @Test
    void securityFilterChain_ShouldBeConfigured_WhenContextLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SecurityFilterChain.class);
            assertNotNull(context.getBean(SecurityFilterChain.class));
        });
    }
}
