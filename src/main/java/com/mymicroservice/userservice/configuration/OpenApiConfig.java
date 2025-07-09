package com.mymicroservice.userservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    //https://www.baeldung.com/spring-boot-swagger-jwt
    //http://localhost:8080/swagger-ui/index.html

    @Bean
    public OpenAPI api (){
        return new OpenAPI().servers(
                List.of(new Server().url("http://localhost:8080"))
        ).info(
                new Info().title("User Servise API")
        );
    }

  /*  @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080")))
                .addSecurityItem(new SecurityRequirement().
                        addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info().title("Task Management System API")
                        .description("Simple Task Management System"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }*/
}
