package com.epay.common.config.security;

import com.epay.domain.auth.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("security")
public class SecurityExpressionMethods {

    private final JwtClaimsHolder               claims;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_KEY_PREFIX = "session:id:";

    public SecurityExpressionMethods(JwtClaimsHolder claims,
                                     RedisTemplate<String, Object> redisTemplate) {
        this.claims        = claims;
        this.redisTemplate = redisTemplate;
    }

    public boolean isOwner(Long resourceUserId) {
        return isOwnerOrAdmin(resourceUserId);
    }

    public boolean isOwnerOrAdmin(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                            || "ROLE_SUPER_USER".equals(a.getAuthority())
                            || "ROLE_CUSTOMER_SERVICE".equals(a.getAuthority()));
        if (isAdmin) return true;

        if (resourceUserId == null) return false;

        Long tokenUserId = claims.getUserId();
        if (tokenUserId != null) return resourceUserId.equals(tokenUserId);

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return resourceUserId.equals(user.getId());
        }

        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Object uid = jwt.getClaim("userId");
            if (uid instanceof Number n) return resourceUserId.equals(n.longValue());
        }

        return false;
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) return false;
        return claims.hasPermission(permission);
    }


    public boolean hasAllPermissions(String... permissions) {
        for (String p : permissions) {
            if (!claims.hasPermission(p)) return false;
        }
        return true;
    }

    public boolean hasAnyPermission(String... permissions) {
        for (String p : permissions) {
            if (claims.hasPermission(p)) return true;
        }
        return false;
    }


    public boolean isActiveAccount() {
        return claims.isAccountActive();
    }

    public boolean isAccessToken() {
        return claims.isAccessToken();
    }

    public boolean requiresMfa() {
        return claims.isMfaAuthenticated();
    }

    public boolean hasMfaAmr() {
        return claims.getAmr().contains("mfa");
    }

    public boolean hasValidSession() {
        String sessionId = claims.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            log.debug("[Security] hasValidSession: no sid claim present — denying");
            return false;
        }

        try {
            String key = SESSION_KEY_PREFIX + sessionId;
            boolean valid = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            if (!valid) {
                log.debug("[Security] hasValidSession: session {} not found in Redis", sessionId);
            }
            return valid;
        } catch (Exception e) {
            log.warn("[Security] Redis session check failed for sid={}: {} — failing open",
                    sessionId, e.getMessage());
            return true;
        }
    }
}
