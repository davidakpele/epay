package com.epay.common.interfaces;

/**
 * Idempotency check backed by Redis.
 * Prevents duplicate deposits/withdrawals when clients retry.
 * Implemented in common config layer using RedisTemplate.
 */
public interface IIdempotencyPort {

    /**
     * Returns true if this key was already processed.
     */
    boolean exists(String key);

    /**
     * Stores the key with a TTL in seconds.
     */
    void store(String key, long ttlSeconds);

    /**
     * Removes the key (e.g. if a transaction fails and client needs to retry with same key).
     */
    void remove(String key);
}
