package pesco.example.withdraw_service.serviceImp;

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
import lombok.extern.slf4j.Slf4j;
import pesco.example.withdraw_service.services.IRateLimitService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Service
public class RateLimitService implements IRateLimitService{

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;
    private ProxyManager<String> proxyManager;

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
    }

    @PreDestroy
    public void destroy() {
        if (connection != null) connection.close();
        if (redisClient != null) redisClient.shutdown();
    }

    /**
     * Try to consume tokens from the bucket
     * @return true if allowed, false if rate limited
     */
    public boolean tryConsume(String key, int capacity, Duration duration, int tokens) {
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, duration));
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();

        Supplier<BucketConfiguration> configSupplier = () -> configuration;
        Bucket bucket = proxyManager.builder().build(key, configSupplier);
        
        return bucket.tryConsume(tokens);
    }

    /**
     * Get remaining tokens for a key
     */
    public long getRemainingTokens(String key, int capacity, Duration duration) {
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, duration));
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();

        Bucket bucket = proxyManager.builder().build(key, () -> configuration);
        return bucket.getAvailableTokens();
    }
}