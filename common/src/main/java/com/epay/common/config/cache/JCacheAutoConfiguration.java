package com.epay.common.config.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;


@Configuration
@ConditionalOnProperty(name = "cache.type", havingValue = "jvm")
public class JCacheAutoConfiguration {
    @Bean
    public JCacheCacheManager jCacheCacheManager() {
        return new JCacheCacheManager(cacheManager());
    }

    @Bean
    public CacheManager cacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        return cacheManager;
    }

}