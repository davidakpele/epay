package com.epay.common.config.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
