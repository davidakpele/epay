package com.epay.common.service;

import com.epay.common.interfaces.IIdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisIdempotencyService implements IIdempotencyPort {

    private static final String PREFIX = "idem:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + key));
        } catch (Exception e) {
            log.warn("[Idempotency] Redis check failed for key={}: {}", key, e.getMessage());
            return false; 
        }
    }

    @Override
    public void store(String key, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(PREFIX + key, "1", Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("[Idempotency] Redis store failed for key={}: {}", key, e.getMessage());
        }
    }

    @Override
    public void remove(String key) {
        try {
            redisTemplate.delete(PREFIX + key);
        } catch (Exception e) {
            log.warn("[Idempotency] Redis remove failed for key={}: {}", key, e.getMessage());
        }
    }
}
