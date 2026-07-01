package pesco.example.withdraw_service.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pesco.example.withdraw_service.annotation.RateLimited;
import pesco.example.withdraw_service.exceptions.RateLimitExceededException;
import pesco.example.withdraw_service.serviceImp.RateLimitService;

import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AOP advice that enforces {@link RateLimited} on controller methods.
 *
 * <p>When {@code coolDownSeconds > 0} it also enforces a minimum gap between
 * successive successful calls — keyed by {@code cd:{keyPrefix}:{identifier}}.
 *
 * <p>Identifier resolution priority:
 * <ol>
 *   <li>SpEL expression from {@code userIdentifier}</li>
 *   <li>{@code X-User-ID} request header</li>
 *   <li>{@code X-API-Key} request header</li>
 *   <li>Client IP (X-Forwarded-For → X-Real-IP → remoteAddr)</li>
 *   <li>"anonymous" as last resort</li>
 * </ol>
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public RateLimitAspect(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {

        String identifier = extractUserIdentifier(joinPoint, rateLimited);
        String bucketKey  = buildKey(rateLimited.keyPrefix(), identifier);
        Duration window   = Duration.ofMillis(
                rateLimited.timeUnit().toMillis(rateLimited.duration()));

        // ── 1. Cool-down check ────────────────────────────────────────────────
        if (rateLimited.coolDownSeconds() > 0) {
            String cdKey = "cd:" + rateLimited.keyPrefix() + ":" + identifier;
            if (rateLimitService.isCoolingDown(cdKey)) {
                long ttl = rateLimitService.getCoolDownTtlSeconds(cdKey);
                log.warn("[RateLimit] Cool-down active | key={} ttl={}s", cdKey, ttl);
                return coolDownResponse(ttl);
            }
        }

        // ── 2. Token-bucket check ─────────────────────────────────────────────
        boolean allowed = rateLimitService.tryConsume(
                bucketKey, rateLimited.capacity(), window, rateLimited.cost());

        if (!allowed) {
            long remaining = rateLimitService.getRemainingTokens(
                    bucketKey, rateLimited.capacity(), window);
            log.warn("[RateLimit] Limit exceeded | key={} user={}", bucketKey, identifier);
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Try again later.",
                    rateLimited.capacity(),
                    rateLimited.duration(),
                    rateLimited.timeUnit(),
                    remaining);
        }

        // ── 3. Proceed ────────────────────────────────────────────────────────
        Object result = joinPoint.proceed();

        // ── 4. Start cool-down after successful call ──────────────────────────
        if (rateLimited.coolDownSeconds() > 0) {
            String cdKey = "cd:" + rateLimited.keyPrefix() + ":" + identifier;
            rateLimitService.startCoolDown(cdKey, Duration.ofSeconds(rateLimited.coolDownSeconds()));
            log.debug("[RateLimit] Cool-down started | key={} duration={}s",
                    cdKey, rateLimited.coolDownSeconds());
        }

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildKey(String prefix, String identifier) {
        return "rate_limit:" + prefix + ":" + identifier;
    }

    private String extractUserIdentifier(ProceedingJoinPoint joinPoint, RateLimited rateLimited) {
        // Priority 1: SpEL expression
        if (!rateLimited.userIdentifier().isEmpty()) {
            try {
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                Object[] args = joinPoint.getArgs();
                Parameter[] params = ((MethodSignature) joinPoint.getSignature())
                        .getMethod().getParameters();
                for (int i = 0; i < params.length && i < args.length; i++) {
                    ctx.setVariable(params[i].getName(), args[i]);
                }
                String val = parser.parseExpression(rateLimited.userIdentifier())
                        .getValue(ctx, String.class);
                if (val != null && !val.isEmpty()) return val;
            } catch (Exception e) {
                log.debug("[RateLimit] SpEL eval failed for '{}': {}",
                        rateLimited.userIdentifier(), e.getMessage());
            }
        }

        // Priority 2–4: headers → IP
        HttpServletRequest request = currentRequest();
        if (request != null) {
            String userId = request.getHeader("X-User-ID");
            if (userId != null && !userId.isEmpty()) return userId;

            String apiKey = request.getHeader("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) return apiKey;

            return extractIp(request);
        }

        return "anonymous";
    }

    private HttpServletRequest currentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(a -> a instanceof ServletRequestAttributes)
                .map(a -> ((ServletRequestAttributes) a).getRequest())
                .orElse(null);
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) return xri.trim();
        return request.getRemoteAddr();
    }

    private ResponseEntity<Map<String, Object>> coolDownResponse(long ttlSeconds) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Action performed too recently. Please wait before trying again.");
        body.put("code", "COOL_DOWN_ACTIVE");
        body.put("retryAfterSeconds", ttlSeconds);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}
