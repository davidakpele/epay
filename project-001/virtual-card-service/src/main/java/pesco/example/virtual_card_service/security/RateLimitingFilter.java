package pesco.example.virtual_card_service.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Wallet rate limit
    private static final int WALLET_RATE_LIMIT = 60; 
    private static final int WALLET_BURST_LIMIT = 120;

    private Bucket createWalletBucket() {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    WALLET_BURST_LIMIT,
                    Refill.intervally(WALLET_RATE_LIMIT, Duration.ofMinutes(1))
                )
            )
            .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // SAFETY: This should never happen because of shouldNotFilter,
        // but we keep it defensive.
        if (!path.startsWith("/wallet")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String bucketKey = clientIp + "_wallet";

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createWalletBucket());

        if (bucket.tryConsume(1)) {
            response.setHeader(
                "X-Rate-Limit-Remaining",
                String.valueOf(bucket.getAvailableTokens())
            );
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                "{\"error\":\"Too many wallet requests. Please try again later.\"," +
                "\"code\":\"RATE_LIMIT_EXCEEDED\"}"
            );
        }
    }

    /**
     * IMPORTANT:
     * Only apply filter to /wallet/**
     * Explicitly exclude auth, health, swagger, etc.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return
            !path.startsWith("/wallet") ||
            path.startsWith("/api/auth") ||
            path.startsWith("/actuator") ||
            path.startsWith("/swagger") ||
            path.startsWith("/v3/api-docs") ||
            path.contains("/health") ||
            path.contains("/ping");
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
