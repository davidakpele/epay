// package com.example.admin_api_service.configs;

// import io.swagger.v3.oas.models.Components;
// import io.swagger.v3.oas.models.OpenAPI;
// import io.swagger.v3.oas.models.info.Contact;
// import io.swagger.v3.oas.models.info.Info;
// import io.swagger.v3.oas.models.info.License;
// import io.swagger.v3.oas.models.security.SecurityRequirement;
// import io.swagger.v3.oas.models.security.SecurityScheme;
// import io.swagger.v3.oas.models.servers.Server;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import java.util.List;

// @Configuration
// public class OpenApiConfig {

//     @Bean
//     public OpenAPI openAPI() {
//         return new OpenAPI()
//                 .info(new Info()
//                         .title("ePay Admin API")
//                         .description("Admin API Service for ePay Banking System")
//                         .version("1.0.0")
//                         .contact(new Contact()
//                                 .name("ePay Team")
//                                 .email("admin@payrix.com"))
//                         .license(new License()
//                                 .name("Private")
//                                 .url("https://payrix.com")))
//                 .servers(List.of(
//                         new Server()
//                                 .url("http://localhost:8287/api")
//                                 .description("Local Development"),
//                         new Server()
//                                 .url("https://api.payrix.com")
//                                 .description("Production")))
//                 .addSecurityItem(new SecurityRequirement()
//                         .addList("Bearer Authentication"))
//                 .components(new Components()
//                         .addSecuritySchemes("Bearer Authentication",
//                                 new SecurityScheme()
//                                         .type(SecurityScheme.Type.HTTP)
//                                         .scheme("bearer")
//                                         .bearerFormat("JWT")
//                                         .description("Enter your JWT token")));
//     }
// }
