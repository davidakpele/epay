package pesco.example.withdraw_service.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import pesco.example.withdraw_service.exceptions.RateLimitExceededException;
import pesco.example.withdraw_service.serviceImp.RateLimitService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

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
        String userId = extractUserIdentifier(joinPoint, rateLimited);
        String key = buildKey(rateLimited.keyPrefix(), userId);
        
        Duration duration = Duration.ofMillis(
            rateLimited.timeUnit().toMillis(rateLimited.duration())
        );

        boolean allowed = rateLimitService.tryConsume(
            key, 
            rateLimited.capacity(), 
            duration, 
            rateLimited.cost()
        );

        if (!allowed) {
            long remainingTokens = rateLimitService.getRemainingTokens(
                key, rateLimited.capacity(), duration
            );
            log.warn("Rate limit exceeded for key: {}, user: {}", key, userId);
            throw new RateLimitExceededException(
                "Rate limit exceeded. Try again later.",
                rateLimited.capacity(),
                rateLimited.duration(),
                rateLimited.timeUnit(),
                remainingTokens
            );
        }

        long remaining = rateLimitService.getRemainingTokens(
            key, rateLimited.capacity(), duration
        );

        Object result = joinPoint.proceed();
        
        // Add rate limit headers if the result is a ResponseEntity
        // (handled via interceptor or controller advice for cleaner approach)
        return result;
    }

    private String extractUserIdentifier(ProceedingJoinPoint joinPoint, RateLimited rateLimited) {
        // Priority 1: SpEL expression from annotation
        if (!rateLimited.userIdentifier().isEmpty()) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            String[] paramNames = getParameterNames(joinPoint);
            
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            
            try {
                return parser.parseExpression(rateLimited.userIdentifier())
                           .getValue(context, String.class);
            } catch (Exception e) {
                log.warn("Failed to evaluate SpEL expression: {}", rateLimited.userIdentifier());
            }
        }

        // Priority 2: X-User-ID header
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String userId = request.getHeader("X-User-ID");
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }

            // Priority 3: API Key
            String apiKey = request.getHeader("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return apiKey;
            }

            // Priority 4: IP address (fallback)
            String ip = getClientIP(request);
            if (ip != null) {
                return ip;
            }
        }

        // Fallback: anonymous
        return "anonymous";
    }

    private HttpServletRequest getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attrs -> attrs instanceof ServletRequestAttributes)
                .map(attrs -> ((ServletRequestAttributes) attrs).getRequest())
                .orElse(null);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getSignature().getDeclaringType()
                .getDeclaredMethods())
                .filter(m -> m.getName().equals(joinPoint.getSignature().getName()))
                .findFirst()
                .map(m -> Arrays.stream(m.getParameters())
                        .map(java.lang.reflect.Parameter::getName)
                        .toArray(String[]::new))
                .orElse(null);
    }
}
