package com.epay.common.config.cache;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    private List<CacheConfig> caches;

    @Data
    public static class CacheConfig {
        @NotBlank
        private String name;
        @NotBlank
        private String keyType;
        @NotBlank
        private String valueType;
        @NotBlank
        private Long ttl;
    }
}
