package com.epay.common.config.security;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String KEY_IP_GLOBAL    = "rl:ip:%s:global";
    private static final String KEY_IP_GROUP     = "rl:ip:%s:group:%s";
    private static final String KEY_USER_GROUP   = "rl:user:%s:group:%s";
    private static final String KEY_PENALTY      = "rl:penalty:%s";
    private static final String KEY_BLOCKED      = "rl:blocked:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RateLimitConfig config;

    private static final class Tightest {
        private String key;
        private int limit;
        private long remaining = Long.MAX_VALUE;
        private long windowSeconds;

        void offer(String key, int limit, long count, long windowSeconds) {
            long left = Math.max(0L, limit - count);
            if (left < remaining) {
                this.key = key;
                this.limit = limit;
                this.remaining = left;
                this.windowSeconds = windowSeconds;
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip    = extractIp(request);
        String group = resolveGroup(request.getRequestURI());
        Tightest tightest = new Tightest();

        try {
            if (isBlocked(ip)) {
                log.warn("[RATE] IP {} is blocked - path={}", ip, request.getRequestURI());
                reject(request, response, "Your IP has been temporarily blocked due to repeated violations.", 0L);
                return;
            }

            if (exceeds(String.format(KEY_IP_GLOBAL, ip),
                    config.getGlobalIpLimit(), config.getGlobalWindowSeconds(), tightest)) {
                writeRateLimitHeaders(response, tightest);
                recordViolation(ip, group, request, response);
                return;
            }

            if (group != null) {
                GroupLimit gl = groupLimit(group);
                if (exceeds(ipGroupKey(ip, group), gl.limit(), gl.windowSeconds(), tightest)) {
                    writeRateLimitHeaders(response, tightest);
                    recordViolation(ip, group, request, response);
                    return;
                }
            }

            String userId = resolveUserId();
            if (userId != null && group != null) {
                GroupLimit gl = groupLimit(group);
                String userKey = String.format(KEY_USER_GROUP, userId, group);
                long userCount = increment(userKey, gl.windowSeconds());
                tightest.offer(userKey, gl.limit(), userCount, gl.windowSeconds());

                if (userCount > gl.limit()) {
                    log.warn("[RATE] User {} exceeded {} limit on {} - count={}", userId, group, request.getRequestURI(), userCount);
                    writeRateLimitHeaders(response, tightest);
                    reject(request, response, "Request rate limit exceeded. Please wait before retrying.", gl.windowSeconds());
                    return;
                }
            }

            writeRateLimitHeaders(response, tightest);

        } catch (Exception e) {
            log.error("[RATE] Redis error for IP={} path={}: {}", ip, request.getRequestURI(), e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private long increment(String key, long windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return count != null ? count : 0L;
    }

    private boolean exceeds(String key, int limit, long windowSeconds, Tightest tightest) {
        long count = increment(key, windowSeconds);
        tightest.offer(key, limit, count, windowSeconds);
        return count > limit;
    }

    private void recordViolation(String ip, String group,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        String penaltyKey = String.format(KEY_PENALTY, ip);
        Long violations   = redisTemplate.opsForValue().increment(penaltyKey);
        if (violations != null && violations == 1L) {
            redisTemplate.expire(penaltyKey, Duration.ofHours(24));
        }

        long retryAfter = config.getGlobalWindowSeconds();
        if (violations != null && violations > 1) {
            retryAfter = Math.min(
                (long) (config.getGlobalWindowSeconds()
                        * Math.pow(config.getPenaltyMultiplier(), violations - 1)),
                config.getMaxPenaltySeconds()
            );
        }

        log.warn("[RATE] Violation #{} for IP={} group={} path={} backoff={}s",
                violations, ip, group, request.getRequestURI(), retryAfter);

        if (violations != null && violations >= config.getSuspiciousThreshold()) {
            String blockedKey = String.format(KEY_BLOCKED, ip);
            redisTemplate.opsForValue().set(blockedKey, "1",
                    Duration.ofSeconds(retryAfter));
            log.warn("[RATE] IP {} BLOCKED for {}s after {} violations", ip, retryAfter, violations);
        }

        reject(request, response,
                "Too many requests. Please wait " + retryAfter + " seconds before retrying.",
                retryAfter);
    }

    private boolean isBlocked(String ip) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(String.format(KEY_BLOCKED, ip)));
    }

    private String resolveGroup(String uri) {
        if (uri.startsWith("/auth/"))   return "auth";
        if (uri.startsWith("/wallet/")) return "wallet";
        if (uri.startsWith("/user/"))   return "user";
        return null;
    }

    private String ipGroupKey(String ip, String group) {
        return String.format(KEY_IP_GROUP, ip, group);
    }

    private record GroupLimit(int limit, long windowSeconds) {}

    private GroupLimit groupLimit(String group) {
        return switch (group) {
            case "auth"   -> new GroupLimit(config.getAuthLimit(),   config.getAuthWindowSeconds());
            case "wallet" -> new GroupLimit(config.getWalletLimit(), config.getWalletWindowSeconds());
            case "user"   -> new GroupLimit(config.getUserLimit(),   config.getUserWindowSeconds());
            default       -> new GroupLimit(config.getGlobalIpLimit(), config.getGlobalWindowSeconds());
        };
    }

    private void reject(HttpServletRequest request, HttpServletResponse response,
                         String message, long retryAfter) throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.RATE_LIMIT_EXCEEDED)
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .retryAfter(retryAfter)
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (retryAfter > 0) response.setHeader("Retry-After", String.valueOf(retryAfter));
        objectMapper.writeValue(response.getWriter(), body);
    }

    /** Same header names/semantics as express-rate-limit: Reset is an epoch timestamp in seconds. */
    private void writeRateLimitHeaders(HttpServletResponse response, Tightest tightest) {
        if (tightest.key == null) return;

        Long ttl = redisTemplate.getExpire(tightest.key, TimeUnit.SECONDS);
        long secondsToReset = (ttl != null && ttl > 0) ? ttl : tightest.windowSeconds;

        response.setHeader("X-RateLimit-Limit",     String.valueOf(tightest.limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(tightest.remaining));
        response.setHeader("X-RateLimit-Reset",     String.valueOf(Instant.now().getEpochSecond() + secondsToReset));
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return request.getRemoteAddr();
    }

    private String resolveUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/actuator/health")
                || path.startsWith("/static/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}