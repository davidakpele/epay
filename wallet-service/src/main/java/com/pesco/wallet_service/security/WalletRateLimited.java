package com.pesco.wallet_service.security;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Marks a controller method for per-user/per-IP rate limiting.
 *
 * <pre>
 * &#64;WalletRateLimited(keyPrefix = "pin_verify", capacity = 5, duration = 1, timeUnit = TimeUnit.MINUTES)
 * </pre>
 *
 * The key used in Redis is: {@code rl_wallet:{keyPrefix}:{identifier}}
 * where {@code identifier} is resolved from the SpEL {@code userIdentifier} expression,
 * falling back to the client IP address.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WalletRateLimited {

    /** Unique prefix for the bucket key. */
    String keyPrefix();

    /** Maximum tokens (= max requests) in the window. */
    int capacity();

    /** Window length. */
    long duration();

    /** Unit of {@link #duration()}. */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * Optional SpEL expression evaluated against method parameters.
     * Example: {@code "#request.userId"} or {@code "#authentication.name"}.
     * Falls back to client IP when empty or evaluation fails.
     */
    String userIdentifier() default "";

    /**
     * Optional cool-down duration in seconds applied after a successful call.
     * 0 = no cool-down. When > 0 a Redis key blocks re-entry until it expires.
     */
    long coolDownSeconds() default 0;
}
