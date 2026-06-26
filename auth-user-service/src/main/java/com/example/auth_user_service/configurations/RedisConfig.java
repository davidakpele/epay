package com.example.auth_user_service.configurations;


import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;  // ✅ Jackson 3, non-deprecated
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * GenericJacksonJsonRedisSerializer is the Spring Data Redis 4.0
     * replacement for the deprecated GenericJackson2JsonRedisSerializer.
     * Uses Jackson 3 (tools.jackson.*) internally.
     *
     * - enableDefaultTyping()           → embeds @class so objects deserialize
     *                                     back to their concrete type
     * - enableSpringCacheNullValueSupport() → handles Spring Cache NullValue sentinel
     */
    @Bean
    public GenericJacksonJsonRedisSerializer genericJsonSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .enableSpringCacheNullValueSupport()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJacksonJsonRedisSerializer genericJsonSerializer
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(genericJsonSerializer);
        template.setHashValueSerializer(genericJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            GenericJacksonJsonRedisSerializer genericJsonSerializer
    ) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(genericJsonSerializer)
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}