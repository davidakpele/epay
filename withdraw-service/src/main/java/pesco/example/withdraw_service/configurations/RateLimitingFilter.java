package pesco.example.withdraw_service.configurations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pesco.example.withdraw_service.serviceImp.RateLimitService;

import java.io.IOException;
import java.time.Duration;

/**
 * IP-level rate limiting filter for all withdraw endpoints, backed by Redis.
 *
 * <p>This is the first line of defence — it rejects obvious abuse by IP before
 * the request even reaches the controller or the per-user {@code @RateLimited} AOP.
 *
 * <p>Rules (per IP address):
 * <ul>
 *   <li>POST /api/withdrawals/user  — 20 req / 1 min</li>
 *   <li>POST /api/withdrawals/bank  — 20 req / 1 min</li>
 *   <li>Everything else             — 60 req / 1 min</li>
 * </ul>
 *
 * Per-user limits (stricter) are enforced separately by {@link pesco.example.withdraw_service.aspect.RateLimitAspect}.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int  WITHDRAW_IP_CAPACITY = 20;
    private static final Duration WITHDRAW_IP_WINDOW = Duration.ofMinutes(1);

    private static final int  GENERAL_IP_CAPACITY  = 60;
    private static final Duration GENERAL_IP_WINDOW = Duration.ofMinutes(1);

    private final RateLimitService rateLimitService;

    public RateLimitingFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip   = extractClientIp(request);
        String path = request.getRequestURI();

        String   bucketKey;
        int      capacity;
        Duration window;

        if (path.startsWith("/api/withdrawals/")) {
            bucketKey = "rl_wd_ip:withdraw:" + ip;
            capacity  = WITHDRAW_IP_CAPACITY;
            window    = WITHDRAW_IP_WINDOW;
        } else {
            bucketKey = "rl_wd_ip:general:" + ip;
            capacity  = GENERAL_IP_CAPACITY;
            window    = GENERAL_IP_WINDOW;
        }

        boolean allowed = rateLimitService.tryConsume(bucketKey, capacity, window, 1);

        if (allowed) {
            long remaining = rateLimitService.getRemainingTokens(bucketKey, capacity, window);
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
                "{\"error\":\"Too many requests from this IP. Please try again later.\"," +
                "\"code\":\"RATE_LIMIT_EXCEEDED\"," +
                "\"retryAfterSeconds\":" + retryAfter + "}"
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator")
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
