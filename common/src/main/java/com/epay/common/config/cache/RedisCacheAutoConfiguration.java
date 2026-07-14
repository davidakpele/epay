package com.epay.common.config.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisCacheAutoConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;
    private final CacheProperties        cacheProperties;
    private final RedisSerializer<Object> redisJsonSerializer;

    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(redisJsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        if (cacheProperties.getCaches() != null) {
            for (CacheProperties.CacheConfig cacheConfig : cacheProperties.getCaches()) {
                RedisCacheConfiguration config = defaultConfig
                        .entryTtl(Duration.ofMinutes(cacheConfig.getTtl()));
                cacheConfigurations.put(cacheConfig.getName(), config);
            }
        }

        log.info("RedisCacheManager configured with {} named caches", cacheConfigurations.size());
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
