package com.epay.common.config.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Supplemental CORS configuration.
 * The primary CorsConfigurationSource bean is defined in SecurityConfiguration
 * and takes effect for all secured endpoints.
 * This configurer covers any MVC routes outside the security filter chain.
 *
 * NOTE: @EnableWebMvc is intentionally NOT used here — it would disable
 * Spring Boot's MVC auto-configuration.
 */
@Configuration
public class CorsAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders(
                        "Authorization", "Content-Type", "X-Requested-With",
                        "Accept", "Origin", "X-Request-ID", "X-API-Version",
                        "Cache-Control", "X-Forwarded-For")
                .exposedHeaders(
                        "Authorization", "X-Request-ID",
                        "X-Rate-Limit-Limit", "X-Rate-Limit-Remaining", "X-Rate-Limit-Reset")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
