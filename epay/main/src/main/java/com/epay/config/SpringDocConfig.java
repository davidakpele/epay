package com.epay.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringDocConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ePay API")
                        .description("ePay — Unified Digital Payment Platform. " +
                                "Use the **Authorize** button to set your Bearer token.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ePay Support")
                                .email("support@epay.com"))
                        .license(new License()
                                .name("Private")
                                .url("https://epay.com")))
                .servers(List.of(
                        new Server().url("/api/v1").description("API Gateway (nginx)")
                ))
                // Apply bearer auth globally — every endpoint shows the lock icon
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here. " +
                                                "Obtain it from POST /auth/login")));
    }
}
