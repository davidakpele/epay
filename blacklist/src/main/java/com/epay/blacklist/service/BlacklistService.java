package com.epay.blacklist.service;

import com.epay.blacklist.domain.entity.BlacklistEntry;
import com.epay.blacklist.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Blacklist service — Redis-cached for sub-millisecond lookups.
 * Falls back to DB if Redis is unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private static final String KEY_PREFIX  = "bl:";
    private static final long   CACHE_TTL_S = 300L; // 5 minutes

    private final BlacklistRepository  repository;
    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isAccountBlacklisted(Long userId) {
        return check("ACCOUNT", String.valueOf(userId));
    }

    public boolean isIpBlacklisted(String ipAddress) {
        return ipAddress != null && check("IP", ipAddress);
    }

    public boolean isAccountNumberBlacklisted(String accountNumber) {
        return accountNumber != null && check("ACCOUNT_NUMBER", accountNumber);
    }

    public void addToBlacklist(String type, String value, Long userId,
                                String reason, String addedBy, LocalDateTime expiresAt) {
        BlacklistEntry entry = BlacklistEntry.builder()
                .type(type.toUpperCase())
                .value(value)
                .userId(userId)
                .active(true)
                .reason(reason)
                .addedBy(addedBy)
                .expiresAt(expiresAt)
                .build();
        repository.save(entry);
        // Warm cache immediately
        redisTemplate.opsForValue().set(cacheKey(type, value), "1", Duration.ofSeconds(CACHE_TTL_S));
        log.info("[Blacklist] Added: type={} value={} by={}", type, value, addedBy);
    }

    public void removeFromBlacklist(Long id) {
        repository.findById(id).ifPresent(entry -> {
            entry.setActive(false);
            repository.save(entry);
            redisTemplate.delete(cacheKey(entry.getType(), entry.getValue()));
            log.info("[Blacklist] Removed: id={}", id);
        });
    }

    // =========================================================================
    // Private
    // =========================================================================

    private boolean check(String type, String value) {
        String key = cacheKey(type, value);
        try {
            // Check Redis first
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) return true;
        } catch (Exception e) {
            log.warn("[Blacklist] Redis check failed: {}", e.getMessage());
        }

        // Fall back to DB
        boolean blacklisted = repository.existsActiveByTypeAndValue(
                type, value, LocalDateTime.now());

        if (blacklisted) {
            // Warm cache
            try {
                redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(CACHE_TTL_S));
            } catch (Exception ignored) {}
        }

        return blacklisted;
    }

    private String cacheKey(String type, String value) {
        return KEY_PREFIX + type.toLowerCase() + ":" + value;
    }
}
