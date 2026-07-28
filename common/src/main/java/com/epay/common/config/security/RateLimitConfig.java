package com.epay.common.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "epay.rate-limit")
public class RateLimitConfig {

    private int  globalIpLimit       = 200;
    private long globalWindowSeconds = 60L;

    private int  authLimit          = 10;
    private long authWindowSeconds  = 60L;

    private int  walletLimit        = 30;
    private long walletWindowSeconds = 60L;
    private int  userLimit          = 50;
    private long userWindowSeconds  = 60L;
    private int  penaltyMultiplier  = 3;

    private long maxPenaltySeconds  = 3600L;
    private int  suspiciousThreshold = 5;
}
