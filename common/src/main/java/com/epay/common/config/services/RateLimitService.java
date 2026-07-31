package com.epay.common.config.services;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.epay.common.interfaces.IRateLimitService;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Service
public class RateLimitService implements IRateLimitService {

    @Value("${spring.redis.host:redis}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private final StringRedisTemplate stringRedis;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;
    private ProxyManager<String> proxyManager;

    public RateLimitService(StringRedisTemplate stringRedis) {
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
                        .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                )
                .build();
        log.info("[RateLimitService] Connected to Redis at {}:{}", redisHost, redisPort);
    }

    @PreDestroy
    public void destroy() {
        if (connection != null) connection.close();
        if (redisClient != null) redisClient.shutdown();
    }

    @Override
    public boolean tryConsume(String key, int capacity, Duration duration, int tokens) {
        @SuppressWarnings("deprecation")
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, duration));
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
        Supplier<BucketConfiguration> configSupplier = () -> configuration;
        Bucket bucket = proxyManager.builder().build(key, configSupplier);
        return bucket.tryConsume(tokens);
    }

    @Override
    public long getRemainingTokens(String key, int capacity, Duration duration) {
        @SuppressWarnings("deprecation")
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, duration));
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
        Bucket bucket = proxyManager.builder().build(key, () -> configuration);
        return bucket.getAvailableTokens();
    }
    @Override
    public void startCoolDown(String key, Duration duration) {
        stringRedis.opsForValue().set(key, "1", duration);
    }

    @Override
    public boolean isCoolingDown(String key) {
        return Boolean.TRUE.equals(stringRedis.hasKey(key));
    }

    @Override
    public long getCoolDownTtlSeconds(String key) {
        Long ttl = stringRedis.getExpire(key);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }
}
