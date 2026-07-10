package com.epay.common.config.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.type", havingValue = "redis")
public class RedisCacheAutoConfiguration {
    private final RedisConnectionFactory redisConnectionFactory;
    private final CacheProperties cacheProperties;
    private final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer;

    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        for (CacheProperties.CacheConfig cacheConfig : cacheProperties.getCaches()) {

            RedisCacheConfiguration config = defaultConfig
                    .entryTtl(Duration.ofMinutes(cacheConfig.getTtl()));
            cacheConfigurations.put(cacheConfig.getName(), config);
        }

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}