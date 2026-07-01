package com.example.auth_user_service.security;

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
 * Per-IP rate limiting filter backed by Redis (distributed, survives restarts).
 *
 * <p>Rules:
 * <ul>
 *   <li>POST /auth/login          — 5 attempts / 1 min (burst 10)</li>
 *   <li>POST /auth/register       — 3 attempts / 1 hour</li>
 *   <li>POST /auth/forgot-password — 3 attempts / 15 min</li>
 *   <li>POST /auth/forgot-username — 3 attempts / 15 min</li>
 *   <li>everything else           — 100 requests / 1 min</li>
 * </ul>
 *
 * <p>Note: The cool-down enforcement for forgot-password / forgot-username is done
 * inside {@link com.example.auth_user_service.services.AuthenticationService}
 * so it can also be keyed on the submitted identifier (email/phone), not just IP.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // ── bucket keys: "rl:{type}:{ip}" ────────────────────────────────────────
    private static final int  LOGIN_CAPACITY        = 10;
    private static final int  LOGIN_REFILL          = 5;
    private static final Duration LOGIN_WINDOW      = Duration.ofMinutes(1);

    private static final int  REGISTER_CAPACITY     = 3;
    private static final Duration REGISTER_WINDOW   = Duration.ofHours(1);

    private static final int  FORGOT_CAPACITY       = 3;
    private static final Duration FORGOT_WINDOW     = Duration.ofMinutes(15);

    private static final int  GENERAL_CAPACITY      = 100;
    private static final Duration GENERAL_WINDOW    = Duration.ofMinutes(1);

    private final RedisRateLimitService rateLimitService;

    public RateLimitingFilter(RedisRateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip   = extractClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        String bucketKey;
        int    capacity;
        Duration window;
        String endpointLabel;

        if ("POST".equals(method) && path.contains("/auth/login")) {
            bucketKey    = "rl:login:" + ip;
            capacity     = LOGIN_CAPACITY;
            window       = LOGIN_WINDOW;
            endpointLabel = "login";

        } else if ("POST".equals(method) && path.contains("/auth/register")) {
            bucketKey    = "rl:register:" + ip;
            capacity     = REGISTER_CAPACITY;
            window       = REGISTER_WINDOW;
            endpointLabel = "registration";

        } else if ("POST".equals(method) && path.contains("/auth/forgot-password")) {
            bucketKey    = "rl:forgot_pw:" + ip;
            capacity     = FORGOT_CAPACITY;
            window       = FORGOT_WINDOW;
            endpointLabel = "forgot-password";

        } else if ("POST".equals(method) && path.contains("/auth/forgot-username")) {
            bucketKey    = "rl:forgot_un:" + ip;
            capacity     = FORGOT_CAPACITY;
            window       = FORGOT_WINDOW;
            endpointLabel = "forgot-username";

        } else {
            bucketKey    = "rl:general:" + ip;
            capacity     = GENERAL_CAPACITY;
            window       = GENERAL_WINDOW;
            endpointLabel = "requests";
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
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.getWriter().write(
                "{\"error\":\"Too many " + endpointLabel + " attempts. Please try again later.\"," +
                "\"code\":\"RATE_LIMIT_EXCEEDED\"," +
                "\"retryAfterSeconds\":" + retryAfter + "}"
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.contains("/public")
            || path.contains("/health")
            || path.contains("/actuator")
            || path.contains("/swagger")
            || path.contains("/v3/api-docs");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return request.getRemoteAddr();
    }
}
