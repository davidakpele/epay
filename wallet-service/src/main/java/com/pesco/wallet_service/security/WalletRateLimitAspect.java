package com.pesco.wallet_service.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AOP advice that enforces {@link WalletRateLimited} on wallet controller methods.
 *
 * <p>Resolution order for the bucket key identifier:
 * <ol>
 *   <li>SpEL expression from {@code userIdentifier} (e.g. {@code "#authentication.name"})</li>
 *   <li>{@code X-User-ID} request header</li>
 *   <li>Client IP address (X-Forwarded-For → X-Real-IP → remoteAddr)</li>
 * </ol>
 *
 * <p>When {@code coolDownSeconds > 0} a successful call sets a Redis cool-down key.
 * Subsequent calls within the cool-down window are rejected with HTTP 429.
 */
@Aspect
@Component
public class WalletRateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(WalletRateLimitAspect.class);

    private final RedisRateLimitService rateLimitService;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    public WalletRateLimitAspect(RedisRateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Around("@annotation(walletRateLimited)")
    public Object around(ProceedingJoinPoint pjp, WalletRateLimited walletRateLimited) throws Throwable {

        String identifier = resolveIdentifier(pjp, walletRateLimited);
        String bucketKey  = "rl_wallet:" + walletRateLimited.keyPrefix() + ":" + identifier;
        Duration window   = Duration.ofMillis(
                walletRateLimited.timeUnit().toMillis(walletRateLimited.duration()));

        // ── Cool-down check ───────────────────────────────────────────────────
        if (walletRateLimited.coolDownSeconds() > 0) {
            String cdKey = "cd_wallet:" + walletRateLimited.keyPrefix() + ":" + identifier;
            if (rateLimitService.isCoolingDown(cdKey)) {
                long ttl = rateLimitService.getCoolDownTtlSeconds(cdKey);
                log.warn("[WalletRateLimit] Cool-down active for key={} ttl={}s", cdKey, ttl);
                return coolDownResponse(ttl);
            }
        }

        // ── Token-bucket check ────────────────────────────────────────────────
        boolean allowed = rateLimitService.tryConsume(bucketKey, walletRateLimited.capacity(), window);

        if (!allowed) {
            long retryAfter = window.toSeconds();
            log.warn("[WalletRateLimit] Rate limit exceeded for key={}", bucketKey);
            return rateLimitResponse(walletRateLimited, retryAfter);
        }

        // ── Proceed ───────────────────────────────────────────────────────────
        Object result = pjp.proceed();

        // ── Start cool-down after a successful call ───────────────────────────
        if (walletRateLimited.coolDownSeconds() > 0) {
            String cdKey = "cd_wallet:" + walletRateLimited.keyPrefix() + ":" + identifier;
            rateLimitService.startCoolDown(cdKey, Duration.ofSeconds(walletRateLimited.coolDownSeconds()));
        }

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String resolveIdentifier(ProceedingJoinPoint pjp, WalletRateLimited ann) {
        // 1. SpEL expression
        if (!ann.userIdentifier().isBlank()) {
            try {
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                Object[] args = pjp.getArgs();
                Parameter[] params = ((MethodSignature) pjp.getSignature())
                        .getMethod().getParameters();
                for (int i = 0; i < params.length && i < args.length; i++) {
                    ctx.setVariable(params[i].getName(), args[i]);
                }
                String val = spelParser.parseExpression(ann.userIdentifier())
                        .getValue(ctx, String.class);
                if (val != null && !val.isBlank()) return val;
            } catch (Exception e) {
                log.debug("[WalletRateLimit] SpEL eval failed: {}", e.getMessage());
            }
        }

        // 2. X-User-ID header / IP fallback
        HttpServletRequest req = currentRequest();
        if (req != null) {
            String header = req.getHeader("X-User-ID");
            if (header != null && !header.isBlank()) return header;
            return extractIp(req);
        }

        return "anonymous";
    }

    private HttpServletRequest currentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(a -> a instanceof ServletRequestAttributes)
                .map(a -> ((ServletRequestAttributes) a).getRequest())
                .orElse(null);
    }

    private String extractIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = req.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return req.getRemoteAddr();
    }

    private ResponseEntity<Map<String, Object>> rateLimitResponse(WalletRateLimited ann, long retryAfter) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Too many requests. Please try again later.");
        body.put("code", "RATE_LIMIT_EXCEEDED");
        body.put("limit", ann.capacity());
        body.put("windowSeconds", ann.timeUnit().toSeconds(ann.duration()));
        body.put("retryAfterSeconds", retryAfter);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    private ResponseEntity<Map<String, Object>> coolDownResponse(long ttl) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Action performed too recently. Please wait before trying again.");
        body.put("code", "COOL_DOWN_ACTIVE");
        body.put("retryAfterSeconds", ttl);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}
