package com.epay.common.config.interfaces;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WalletRateLimited {
    String keyPrefix();

    int capacity();
    long duration();
    TimeUnit timeUnit() default TimeUnit.MINUTES;
    String userIdentifier() default "";


    long coolDownSeconds() default 0;
}
