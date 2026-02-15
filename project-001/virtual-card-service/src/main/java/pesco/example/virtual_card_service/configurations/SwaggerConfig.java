package pesco.example.virtual_card_service.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.name:Virtual Card Service}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:REST API for managing virtual cards}")
    private String appDescription;

    @Value("${app.contact.name:Virtual Card Team}")
    private String contactName;

    @Value("${app.contact.email:support@virtualcard.com}")
    private String contactEmail;

    @Value("${app.contact.url:https://virtualcard.com}")
    private String contactUrl;

    @Value("${app.license.name:Apache 2.0}")
    private String licenseName;

    @Value("${app.license.url:https://www.apache.org/licenses/LICENSE-2.0.html}")
    private String licenseUrl;

    @Value("${server.port:8037}")
    private String serverPort;

    @Bean
    public OpenAPI virtualCardOpenAPI() {
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .description("Enter JWT Bearer token")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .description(appDescription)
                        .version(appVersion)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local"),
                        new Server().url("https://api.virtualcard.com").description("Production")
                ))
                .components(new Components().addSecuritySchemes("Bearer Authentication", jwtSecurityScheme))
                .addSecurityItem(securityRequirement)
                .tags(List.of(
                        new Tag().name("Virtual Card Management").description("CRUD operations"),
                        new Tag().name("Card Operations").description("Freeze, unfreeze, cancel"),
                        new Tag().name("Balance Management").description("Add/deduct funds"),
                        new Tag().name("Health Check").description("Service health")
                ));
    }
}