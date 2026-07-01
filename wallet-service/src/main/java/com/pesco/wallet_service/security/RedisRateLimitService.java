package com.pesco.wallet_service.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Distributed rate limiting backed by Redis (Lettuce) for the wallet-service.
 * Uses token-bucket algorithm via Bucket4j 8.x with a Redis proxy manager.
 */
@Service
public class RedisRateLimitService {

    @Value("${spring.data.redis.host:redis}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    private final StringRedisTemplate stringRedis;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;
    private ProxyManager<String> proxyManager;

    public RedisRateLimitService(StringRedisTemplate stringRedis) {
        this.stringRedis = stringRedis;
    }

    @PostConstruct
    public void init() {
        redisClient = RedisClient.create("redis://" + redisHost + ":" + redisPort);
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        connection = redisClient.connect(codec);
        proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                    io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(15))
                )
                .build();
    }

    @PreDestroy
    public void destroy() {
        if (connection != null) connection.close();
        if (redisClient != null)  redisClient.shutdown();
    }

    // ── Token-bucket rate limiting ────────────────────────────────────────────

    public boolean tryConsume(String key, int capacity, Duration window) {
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, window));
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
        Supplier<BucketConfiguration> supplier = () -> config;
        Bucket bucket = proxyManager.builder().build(key, supplier);
        return bucket.tryConsume(1);
    }

    public long getAvailableTokens(String key, int capacity, Duration window) {
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, window));
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
        Bucket bucket = proxyManager.builder().build(key, () -> config);
        return bucket.getAvailableTokens();
    }

    // ── Cool-down support (minimum interval between successive actions) ────────

    public void startCoolDown(String key, Duration duration) {
        stringRedis.opsForValue().set(key, "1", duration);
    }

    public boolean isCoolingDown(String key) {
        return Boolean.TRUE.equals(stringRedis.hasKey(key));
    }

    public long getCoolDownTtlSeconds(String key) {
        Long ttl = stringRedis.getExpire(key);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }
}
