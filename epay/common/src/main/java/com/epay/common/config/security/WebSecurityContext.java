package com.epay.common.config.security;

import com.epay.domain.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Convenience accessor for the currently authenticated user.
 *
 * <p>Uses {@link JwtClaimsHolder} as the primary source for {@code userId} so that
 * the correct numeric DB id is always returned regardless of whether the principal
 * is a domain {@link User} entity, a synthetic admin {@code UserDetails}, or an
 * OAuth2 {@code Jwt} object.
 *
 * <p>The old implementation used {@code UserLookupPort.findUserIdByUsername(sub)}
 * where {@code sub} is now a UUID — causing a DB miss for every call.  The JWT
 * already contains the {@code userId} claim, so we read it directly from there.
 */
@Component
@RequiredArgsConstructor
public class WebSecurityContext {

    private final JwtClaimsHolder claims;

    // ── Authentication ────────────────────────────────────────────────────────

    public Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
        return Optional.of(auth);
    }

    public boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }

    // ── Principal ─────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    public Optional<UserDetails> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserDetails)
                .map(p -> (UserDetails) p);
    }

    // ── User identity — JWT-first, multi-path fallback ────────────────────────

    /**
     * Returns the authenticated user's numeric DB id.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>JWT {@code userId} claim via {@link JwtClaimsHolder} (fastest, no DB hit)</li>
     *   <li>Domain {@link User} entity on the SecurityContext principal (regular user path)</li>
     *   <li>OAuth2 {@code Jwt} principal {@code userId} claim (oauth2ResourceServer path)</li>
     * </ol>
     */
    public Optional<Long> getUserId() {
        // 1 — JWT claim (works for both admin synthetic and user DB-loaded paths)
        Long fromToken = claims.getUserId();
        if (fromToken != null) return Optional.of(fromToken);

        // 2 — domain User entity on the principal
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .flatMap(principal -> {
                    if (principal instanceof User user) {
                        return Optional.ofNullable(user.getId());
                    }
                    if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                        Object uid = jwt.getClaim("userId");
                        if (uid instanceof Number n) return Optional.of(n.longValue());
                    }
                    return Optional.empty();
                });
    }

    public Long getUserIdOrThrow() {
        return getUserId().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("No authenticated user found"));
    }

    /**
     * Returns the raw JWT subject ({@code sub} claim) — a UUID string for new tokens.
     * Use {@link #getUserId()} when you need the numeric DB id.
     */
    @SuppressWarnings("null")
    public String getUsername() {
        // Try JWT sub first
        String sub = claims.getSub();
        if (sub != null) return sub;
        // Fallback to SecurityContext principal name
        return getPrincipal().map(UserDetails::getUsername).orElse("unknown");
    }

    @SuppressWarnings("null")
    public String getRole() {
        return getPrincipal()
                .map(u -> u.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("UNKNOWN"))
                .orElse("UNKNOWN");
    }

    public String getUserForLog() {
        return getUserId().map(String::valueOf).orElse("anonymous");
    }

    // ── Convenience JWT claim accessors ───────────────────────────────────────

    /** Returns the session ID ({@code sid} claim). */
    public String getSessionId() { return claims.getSessionId(); }

    /** Returns the token unique ID ({@code jti} claim). */
    public String getTokenId()   { return claims.getTokenId(); }

    /** Returns the account status ({@code accountStatus} claim). */
    public String getAccountStatus() { return claims.getAccountStatus(); }
}
