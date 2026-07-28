package com.epay.common.interfaces;

import java.time.Duration;

public interface IRateLimitService {
    void destroy();
    void init();
    boolean tryConsume(String key, int capacity, Duration duration, int tokens);
    long getRemainingTokens(String key, int capacity, Duration duration);
    void startCoolDown(String key, Duration duration);
    boolean isCoolingDown(String key);
    long getCoolDownTtlSeconds(String key);
}