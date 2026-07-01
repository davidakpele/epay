package pesco.example.withdraw_service.services;

import java.time.Duration;

public interface IRateLimitService {
    void destroy();
    void init();
    boolean tryConsume(String key, int capacity, Duration duration, int tokens);
    long getRemainingTokens(String key, int capacity, Duration duration);

    // ── Cool-down support ─────────────────────────────────────────────────────
    /** Starts a cool-down for the given key with the specified duration. */
    void startCoolDown(String key, Duration duration);
    /** Returns true if a cool-down is currently active for this key. */
    boolean isCoolingDown(String key);
    /** Returns remaining TTL in seconds, or 0 if not cooling down. */
    long getCoolDownTtlSeconds(String key);
}
