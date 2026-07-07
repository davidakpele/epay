package com.epay.common.config.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(name = "cache.type", havingValue = "jvm")
public class JVMCacheConfiguration {
    private final JCacheCacheManager jCacheCacheManager;
    private final CacheProperties cacheProperties;

    @PostConstruct
    public void createCache() {
        for (CacheProperties.CacheConfig cacheConfig : cacheProperties.getCaches()) {
            try {
                Class<?> valueType = Class.forName(cacheConfig.getValueType());
                Class<?> keyType = Class.forName(cacheConfig.getKeyType());
                Duration ttl =
                        new Duration(MINUTES, cacheConfig.getTtl());
                jCacheCacheManager.getCacheManager()
                        .createCache(cacheConfig.getName(), createCache(keyType, valueType, ttl));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private <K, V> MutableConfiguration<K, V> createCache(Class<K> keyType, Class<V> valueType, Duration ttl) {
        return new MutableConfiguration<K, V>()
                .setTypes(keyType, valueType)
                .setStoreByValue(true)
                .setStatisticsEnabled(false)
                .setManagementEnabled(false)
                .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(ttl));
    }
}
