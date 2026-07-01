package pesco.example.withdraw_service.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    
    /** Unique key prefix for this endpoint */
    String keyPrefix();
    
    /** Maximum requests allowed in the time window */
    int capacity();
    
    /** Time window duration */
    long duration();
    
    /** Time unit for the duration */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
    
    /** SpEL expression to extract user identifier from method args */
    String userIdentifier() default "";
    
    /** Cost of this request in tokens */
    int cost() default 1;

    /**
     * Minimum cool-down period in seconds between successive successful calls.
     * 0 = no cool-down enforced.
     * When > 0, after a successful call a Redis key blocks re-entry until it expires.
     */
    long coolDownSeconds() default 0;
}