package com.example.auth_user_service.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Value("${app.cache.warmup-interval:600000}") 
    private long warmupInterval;

    @Value("${app.cache.page-size:500}")
    private int pageSize;

    @Value("${app.cache.max-concurrent-workers:5}")
    private int maxConcurrentWorkers;

    public long getWarmupInterval() {
        return warmupInterval;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getMaxConcurrentWorkers() {
        return maxConcurrentWorkers;
    }
}
