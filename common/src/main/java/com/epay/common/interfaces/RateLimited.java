package com.epay.common.interfaces;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    
    String keyPrefix();
    
    int capacity();
    
    long duration();
    
    TimeUnit timeUnit() default TimeUnit.MINUTES;
    
    String userIdentifier() default "";
    int cost() default 1;

    long coolDownSeconds() default 0;
}