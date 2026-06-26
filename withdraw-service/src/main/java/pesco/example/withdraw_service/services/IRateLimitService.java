package pesco.example.withdraw_service.services;

import java.time.Duration;

public interface IRateLimitService {
    void destroy();
    void init();
    boolean tryConsume(String key, int capacity, Duration duration, int tokens);
    long getRemainingTokens(String key, int capacity, Duration duration);

}
