package com.epay.common.interfaces;

public interface IIdempotencyPort {
    boolean exists(String key);

    void store(String key, long ttlSeconds);

    void remove(String key);
}
