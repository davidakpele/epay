package com.epay.common.config.cache;

import com.epay.common.interfaces.IIdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IIdempotencyPort {

    private static final String PREFIX = "idem:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + key));
    }

    @Override
    public void store(String key, long ttlSeconds) {
        redisTemplate.opsForValue().set(PREFIX + key, "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(PREFIX + key);
    }
}
