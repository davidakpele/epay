package com.epay.common.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate limit configuration — override in application.yaml per environment.
 *
 * epay:
 *   rate-limit:
 *     global-ip-limit: 200
 *     global-window-seconds: 60
 *     auth-limit: 10
 *     auth-window-seconds: 60
 *     wallet-limit: 30
 *     wallet-window-seconds: 60
 *     user-limit: 50
 *     user-window-seconds: 60
 *     penalty-multiplier: 3
 *     max-penalty-seconds: 3600
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "epay.rate-limit")
public class RateLimitConfig {

    // Global IP bucket — coarse DDoS guard
    private int  globalIpLimit       = 200;
    private long globalWindowSeconds = 60L;

    // Auth endpoints — /auth/** (login, register, forgot-password, OTP)
    // Heavy surveillance: brute-force and credential stuffing protection
    private int  authLimit          = 10;
    private long authWindowSeconds  = 60L;

    // Wallet endpoints — /wallet/**
    // Financial operations: transfer, balance update, PIN
    private int  walletLimit        = 30;
    private long walletWindowSeconds = 60L;

    // User endpoints — /user/**
    private int  userLimit          = 50;
    private long userWindowSeconds  = 60L;

    // After exceeding limit N times, multiply the window by this factor
    private int  penaltyMultiplier  = 3;

    // Maximum penalty duration in seconds (1 hour)
    private long maxPenaltySeconds  = 3600L;

    // How many violations before an IP is flagged as suspicious
    private int  suspiciousThreshold = 5;
}
