package com.pesco.wallet_service.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    @NotBlank(message = "JWT secret key is required")
    private String secretKey;
    
    @NotNull(message = "Token expiration minutes is required")
    @Positive(message = "Token expiration must be positive")
    @Min(value = 1, message = "Token expiration must be at least 1 minute")
    private Integer expirationMinutes = 30;
    
    @NotNull(message = "Refresh token expiration days is required")
    @Positive(message = "Refresh token expiration must be positive")
    @Min(value = 1, message = "Refresh token expiration must be at least 1 day")
    private Integer refreshExpirationDays = 7;
    
    private String issuer = "your-app";
    
    private String audience = "your-app-users";
    
    @NotNull(message = "Clock skew seconds is required")
    @Min(value = 0, message = "Clock skew cannot be negative")
    private Integer clockSkewSeconds = 30;
    
    private String algorithm = "HS256";
    
    private String tokenPrefix = "Bearer";
    
    private String authorizationHeader = "Authorization";
    
    private Boolean enableTokenBlacklist = true;
    
    private Long blacklistCleanupIntervalMinutes = 60L;
    
    private Long maxBlacklistSize = 10000L;
    
    private String cookieName = "jwt-token";
    
    private Boolean httpOnlyCookie = true;
    
    private Boolean secureCookie = true;
    
    private String sameSiteCookie = "Strict";
    
    private String corsAllowedOrigins = "*";
    
    private Boolean enableMultiFactorSupport = false;
    
    private Integer maxTokensPerUser = 5;

    private Boolean logTokenIssuance = false;
    
    private Boolean enableTokenRotation = true;
    
    private Integer rotationGracePeriodMinutes = 5;
    
    public boolean isCookieEnabled() {
        return cookieName != null && !cookieName.trim().isEmpty();
    }
    
    public long getExpirationMillis() {
        return expirationMinutes * 60 * 1000L;
    }
    
    public long getRefreshExpirationMillis() {
        return refreshExpirationDays * 24 * 60 * 60 * 1000L;
    }
    
    public long getClockSkewMillis() {
        return clockSkewSeconds * 1000L;
    }


    public JwtProperties() {
    }

    public JwtProperties(String secretKey, Integer expirationMinutes, Integer refreshExpirationDays, String issuer, String audience, Integer clockSkewSeconds, String algorithm, String tokenPrefix, String authorizationHeader, Boolean enableTokenBlacklist, Long blacklistCleanupIntervalMinutes, Long maxBlacklistSize, String cookieName, Boolean httpOnlyCookie, Boolean secureCookie, String sameSiteCookie, String corsAllowedOrigins, Boolean enableMultiFactorSupport, Integer maxTokensPerUser, Boolean logTokenIssuance, Boolean enableTokenRotation, Integer rotationGracePeriodMinutes) {
        this.secretKey = secretKey;
        this.expirationMinutes = expirationMinutes;
        this.refreshExpirationDays = refreshExpirationDays;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSeconds = clockSkewSeconds;
        this.algorithm = algorithm;
        this.tokenPrefix = tokenPrefix;
        this.authorizationHeader = authorizationHeader;
        this.enableTokenBlacklist = enableTokenBlacklist;
        this.blacklistCleanupIntervalMinutes = blacklistCleanupIntervalMinutes;
        this.maxBlacklistSize = maxBlacklistSize;
        this.cookieName = cookieName;
        this.httpOnlyCookie = httpOnlyCookie;
        this.secureCookie = secureCookie;
        this.sameSiteCookie = sameSiteCookie;
        this.corsAllowedOrigins = corsAllowedOrigins;
        this.enableMultiFactorSupport = enableMultiFactorSupport;
        this.maxTokensPerUser = maxTokensPerUser;
        this.logTokenIssuance = logTokenIssuance;
        this.enableTokenRotation = enableTokenRotation;
        this.rotationGracePeriodMinutes = rotationGracePeriodMinutes;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getExpirationMinutes() {
        return this.expirationMinutes;
    }

    public void setExpirationMinutes(Integer expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public Integer getRefreshExpirationDays() {
        return this.refreshExpirationDays;
    }

    public void setRefreshExpirationDays(Integer refreshExpirationDays) {
        this.refreshExpirationDays = refreshExpirationDays;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return this.audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Integer getClockSkewSeconds() {
        return this.clockSkewSeconds;
    }

    public void setClockSkewSeconds(Integer clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getTokenPrefix() {
        return this.tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getAuthorizationHeader() {
        return this.authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public Boolean isEnableTokenBlacklist() {
        return this.enableTokenBlacklist;
    }

    public Boolean getEnableTokenBlacklist() {
        return this.enableTokenBlacklist;
    }

    public void setEnableTokenBlacklist(Boolean enableTokenBlacklist) {
        this.enableTokenBlacklist = enableTokenBlacklist;
    }

    public Long getBlacklistCleanupIntervalMinutes() {
        return this.blacklistCleanupIntervalMinutes;
    }

    public void setBlacklistCleanupIntervalMinutes(Long blacklistCleanupIntervalMinutes) {
        this.blacklistCleanupIntervalMinutes = blacklistCleanupIntervalMinutes;
    }

    public Long getMaxBlacklistSize() {
        return this.maxBlacklistSize;
    }

    public void setMaxBlacklistSize(Long maxBlacklistSize) {
        this.maxBlacklistSize = maxBlacklistSize;
    }

    public String getCookieName() {
        return this.cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public Boolean isHttpOnlyCookie() {
        return this.httpOnlyCookie;
    }

    public Boolean getHttpOnlyCookie() {
        return this.httpOnlyCookie;
    }

    public void setHttpOnlyCookie(Boolean httpOnlyCookie) {
        this.httpOnlyCookie = httpOnlyCookie;
    }

    public Boolean isSecureCookie() {
        return this.secureCookie;
    }

    public Boolean getSecureCookie() {
        return this.secureCookie;
    }

    public void setSecureCookie(Boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    public String getSameSiteCookie() {
        return this.sameSiteCookie;
    }

    public void setSameSiteCookie(String sameSiteCookie) {
        this.sameSiteCookie = sameSiteCookie;
    }

    public String getCorsAllowedOrigins() {
        return this.corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public Boolean isEnableMultiFactorSupport() {
        return this.enableMultiFactorSupport;
    }

    public Boolean getEnableMultiFactorSupport() {
        return this.enableMultiFactorSupport;
    }

    public void setEnableMultiFactorSupport(Boolean enableMultiFactorSupport) {
        this.enableMultiFactorSupport = enableMultiFactorSupport;
    }

    public Integer getMaxTokensPerUser() {
        return this.maxTokensPerUser;
    }

    public void setMaxTokensPerUser(Integer maxTokensPerUser) {
        this.maxTokensPerUser = maxTokensPerUser;
    }

    public Boolean isLogTokenIssuance() {
        return this.logTokenIssuance;
    }

    public Boolean getLogTokenIssuance() {
        return this.logTokenIssuance;
    }

    public void setLogTokenIssuance(Boolean logTokenIssuance) {
        this.logTokenIssuance = logTokenIssuance;
    }

    public Boolean isEnableTokenRotation() {
        return this.enableTokenRotation;
    }

    public Boolean getEnableTokenRotation() {
        return this.enableTokenRotation;
    }

    public void setEnableTokenRotation(Boolean enableTokenRotation) {
        this.enableTokenRotation = enableTokenRotation;
    }

    public Integer getRotationGracePeriodMinutes() {
        return this.rotationGracePeriodMinutes;
    }

    public void setRotationGracePeriodMinutes(Integer rotationGracePeriodMinutes) {
        this.rotationGracePeriodMinutes = rotationGracePeriodMinutes;
    }
    
}