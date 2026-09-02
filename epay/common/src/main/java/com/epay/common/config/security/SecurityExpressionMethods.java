package com.epay.common.config.security;

import com.epay.domain.auth.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security SpEL expression helper bean — referenced in {@code @PreAuthorize}
 * annotations as {@code @security}.
 *
 * <pre>
 * // Ownership check (existing)
 * @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
 *
 * // Permission check  ← NEW
 * @PreAuthorize("@security.hasPermission('payment:create')")
 *
 * // MFA required  ← NEW
 * @PreAuthorize("@security.requiresMfa()")
 *
 * // Active account required  ← NEW (belt-and-suspenders over filter)
 * @PreAuthorize("@security.isActiveAccount()")
 *
 * // ACCESS token required  ← NEW
 * @PreAuthorize("@security.isAccessToken()")
 *
 * // Valid Redis session required  ← NEW
 * @PreAuthorize("@security.hasValidSession()")
 *
 * // Combine freely
 * @PreAuthorize("hasRole('USER') and @security.hasPermission('wallet:read') and @security.hasValidSession()")
 * </pre>
 */
@Slf4j
@Component("security")
public class SecurityExpressionMethods {

    private final JwtClaimsHolder               claims;
    private final RedisTemplate<String, Object> redisTemplate;

    // Session key pattern mirrors UserTracerService: "session:id:{sessionId}"
    private static final String SESSION_KEY_PREFIX = "session:id:";

    public SecurityExpressionMethods(JwtClaimsHolder claims,
                                     RedisTemplate<String, Object> redisTemplate) {
        this.claims        = claims;
        this.redisTemplate = redisTemplate;
    }

    // ── Ownership ─────────────────────────────────────────────────────────────

    /**
     * True if the authenticated user owns the resource ({@code resourceUserId})
     * or holds an admin role.
     */
    public boolean isOwner(Long resourceUserId) {
        return isOwnerOrAdmin(resourceUserId);
    }

    public boolean isOwnerOrAdmin(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        // Admins bypass ownership checks
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                            || "ROLE_SUPER_USER".equals(a.getAuthority())
                            || "ROLE_CUSTOMER_SERVICE".equals(a.getAuthority()));
        if (isAdmin) return true;

        if (resourceUserId == null) return false;

        // Try JwtClaimsHolder first (covers both admin synthetic and user DB paths)
        Long tokenUserId = claims.getUserId();
        if (tokenUserId != null) return resourceUserId.equals(tokenUserId);

        // Fallback: domain User entity on SecurityContext (regular user DB-loaded path)
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return resourceUserId.equals(user.getId());
        }

        // Fallback: OAuth2 Jwt principal (oauth2ResourceServer path)
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Object uid = jwt.getClaim("userId");
            if (uid instanceof Number n) return resourceUserId.equals(n.longValue());
        }

        return false;
    }

    // ── Permission checks ─────────────────────────────────────────────────────

    /**
     * True if the JWT {@code permissions} claim contains the given permission.
     *
     * <p>Example: {@code @PreAuthorize("@security.hasPermission('payment:create')")}
     *
     * <p>Available permissions per role:
     * <ul>
     *   <li>USER        — payment:create, payment:read, wallet:read, crypto:deposit</li>
     *   <li>ADMIN       — + payment:update, wallet:update/freeze, user:read/update, admin:access, crypto:deposit</li>
     *   <li>SUPER_USER  — + payment:delete, user:delete, wallet:freeze, crypto:withdraw</li>
     *   <li>CUSTOMER_SERVICE — payment:read, wallet:read/freeze, user:read, admin:access</li>
     *   <li>EDITOR      — payment:read, wallet:read, user:read, admin:access</li>
     * </ul>
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) return false;
        return claims.hasPermission(permission);
    }

    /**
     * True if the caller holds ALL of the given permissions.
     *
     * <p>Example: {@code @PreAuthorize("@security.hasAllPermissions('wallet:read','wallet:update')")}
     */
    public boolean hasAllPermissions(String... permissions) {
        for (String p : permissions) {
            if (!claims.hasPermission(p)) return false;
        }
        return true;
    }

    /**
     * True if the caller holds at least one of the given permissions.
     *
     * <p>Example: {@code @PreAuthorize("@security.hasAnyPermission('payment:create','payment:read')")}
     */
    public boolean hasAnyPermission(String... permissions) {
        for (String p : permissions) {
            if (claims.hasPermission(p)) return true;
        }
        return false;
    }

    // ── Account status ────────────────────────────────────────────────────────

    /**
     * True if the JWT {@code accountStatus} claim is {@code "ACTIVE"}.
     *
     * <p>The filter already blocks LOCKED/INACTIVE accounts; this method provides
     * belt-and-suspenders enforcement directly in controller {@code @PreAuthorize}.
     *
     * <p>Example: {@code @PreAuthorize("@security.isActiveAccount()")}
     */
    public boolean isActiveAccount() {
        return claims.isAccountActive();
    }

    // ── Token type ────────────────────────────────────────────────────────────

    /**
     * True if the JWT {@code token.type} is {@code "ACCESS"} (or absent for legacy tokens).
     *
     * <p>The filter already blocks REFRESH tokens on all non-refresh paths; this
     * annotation can be added as extra documentation/enforcement on individual endpoints.
     *
     * <p>Example: {@code @PreAuthorize("@security.isAccessToken()")}
     */
    public boolean isAccessToken() {
        return claims.isAccessToken();
    }

    // ── MFA / ACR ─────────────────────────────────────────────────────────────

    /**
     * True if the JWT {@code acr} claim is {@code "urn:epay:auth:mfa"}, meaning
     * the user authenticated with a second factor (TOTP, OTP email, etc.).
     *
     * <p>Use this on high-value endpoints that must require MFA regardless of
     * whether the user has MFA enabled globally.
     *
     * <p>Example: {@code @PreAuthorize("@security.requiresMfa()")}
     */
    public boolean requiresMfa() {
        return claims.isMfaAuthenticated();
    }

    /**
     * True if {@code amr} contains {@code "mfa"} — same semantics as
     * {@link #requiresMfa()} but checks the AMR list instead of ACR.
     * Either check is sufficient; ACR is preferred.
     */
    public boolean hasMfaAmr() {
        return claims.getAmr().contains("mfa");
    }

    // ── Session validation ────────────────────────────────────────────────────

    /**
     * True if the session referenced by the JWT {@code sid} claim still exists
     * in Redis (i.e. the session has not been invalidated by logout or admin action).
     *
     * <p>This provides true server-side session revocation on top of stateless JWTs.
     * Redis key pattern: {@code session:id:{sessionId}} — set by {@code UserTracerService}.
     *
     * <p>Fails open (returns {@code true}) when Redis is unavailable so that a
     * Redis outage does not lock out all users.
     *
     * <p>Example: {@code @PreAuthorize("@security.hasValidSession()")}
     */
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
            // Fail open — Redis unavailable should not lock out all users
            log.warn("[Security] Redis session check failed for sid={}: {} — failing open",
                    sessionId, e.getMessage());
            return true;
        }
    }
}
