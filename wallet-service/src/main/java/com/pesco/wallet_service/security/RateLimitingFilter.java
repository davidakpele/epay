package com.pesco.wallet_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Per-IP rate limiting filter for all wallet endpoints, backed by Redis.
 *
 * <p>Rules (per IP address):
 * <ul>
 *   <li>POST  /wallet/create/pin   — 5 attempts / 10 min (pin setup is sensitive)</li>
 *   <li>POST  /wallet/verify/pin   — 10 attempts / 5 min (brute-force guard)</li>
 *   <li>POST  /wallet/internal/**  — 30 req / 1 min (internal service-to-service)</li>
 *   <li>Everything else /wallet/** — 60 req / 1 min</li>
 * </ul>
 *
 * <p>Fine-grained per-user limits on sensitive actions are enforced separately
 * via the {@link WalletRateLimited} annotation + {@link WalletRateLimitAspect}.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Capacities
    private static final int PIN_SETUP_CAPACITY    = 5;
    private static final Duration PIN_SETUP_WINDOW = Duration.ofMinutes(10);

    private static final int PIN_VERIFY_CAPACITY    = 10;
    private static final Duration PIN_VERIFY_WINDOW = Duration.ofMinutes(5);

    private static final int INTERNAL_CAPACITY    = 30;
    private static final Duration INTERNAL_WINDOW = Duration.ofMinutes(1);

    private static final int GENERAL_CAPACITY    = 60;
    private static final Duration GENERAL_WINDOW = Duration.ofMinutes(1);

    private final RedisRateLimitService rateLimitService;

    public RateLimitingFilter(RedisRateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip     = extractClientIp(request);
        String path   = request.getRequestURI();
        String method = request.getMethod();

        String   bucketKey;
        int      capacity;
        Duration window;

        if ("POST".equals(method) && path.contains("/wallet/create/pin")) {
            bucketKey = "rl_w:pin_setup:" + ip;
            capacity  = PIN_SETUP_CAPACITY;
            window    = PIN_SETUP_WINDOW;

        } else if ("POST".equals(method) && path.contains("/wallet/verify/pin")) {
            bucketKey = "rl_w:pin_verify:" + ip;
            capacity  = PIN_VERIFY_CAPACITY;
            window    = PIN_VERIFY_WINDOW;

        } else if (path.contains("/wallet/internal/")) {
            bucketKey = "rl_w:internal:" + ip;
            capacity  = INTERNAL_CAPACITY;
            window    = INTERNAL_WINDOW;

        } else {
            bucketKey = "rl_w:general:" + ip;
            capacity  = GENERAL_CAPACITY;
            window    = GENERAL_WINDOW;
        }

        boolean allowed = rateLimitService.tryConsume(bucketKey, capacity, window);

        if (allowed) {
            long remaining = rateLimitService.getAvailableTokens(bucketKey, capacity, window);
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
            response.setHeader("X-Rate-Limit-Limit",     String.valueOf(capacity));
            filterChain.doFilter(request, response);
        } else {
            long retryAfter = window.toSeconds();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After",            String.valueOf(retryAfter));
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.getWriter().write(
                "{\"error\":\"Too many wallet requests. Please try again later.\"," +
                "\"code\":\"RATE_LIMIT_EXCEEDED\"," +
                "\"retryAfterSeconds\":" + retryAfter + "}"
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/wallet")
            || path.startsWith("/actuator")
            || path.startsWith("/swagger")
            || path.startsWith("/v3/api-docs")
            || path.contains("/health")
            || path.contains("/ping");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return request.getRemoteAddr();
    }
}
