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

@Component
@RequiredArgsConstructor
public class WebSecurityContext {

    private final JwtClaimsHolder claims;

    public Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
        return Optional.of(auth);
    }

    public boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }


    @SuppressWarnings("null")
    public Optional<UserDetails> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserDetails)
                .map(p -> (UserDetails) p);
    }

    public Optional<Long> getUserId() {
        Long fromToken = claims.getUserId();
        if (fromToken != null) return Optional.of(fromToken);
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

    @SuppressWarnings("null")
    public String getUsername() {
        String sub = claims.getSub();
        if (sub != null) return sub;
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

    public String getSessionId() { return claims.getSessionId(); }

    public String getTokenId()   { return claims.getTokenId(); }

    public String getAccountStatus() { return claims.getAccountStatus(); }
}
