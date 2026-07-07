package com.epay.common.config.security;

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

    private final UserLookupPort userLookupPort;

    public Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
        return Optional.of(auth);
    }

    public Optional<UserDetails> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserDetails)
                .map(p -> (UserDetails) p);
    }

    public Optional<Long> getUserId() {
        return getPrincipal()
                .map(UserDetails::getUsername)
                .flatMap(userLookupPort::findUserIdByUsername);
    }

    public Long getUserIdOrThrow() {
        return getUserId().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("No authenticated user found"));
    }

    public String getUsername() {
        return getPrincipal().map(UserDetails::getUsername).orElse("unknown");
    }

    public String getRole() {
        return getPrincipal()
                .map(u -> u.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("UNKNOWN"))
                .orElse("UNKNOWN");
    }

    public boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }

    /** Safe logging helper — never throws, never returns sensitive data. */
    public String getUserForLog() {
        return getUserId().map(String::valueOf).orElse("anonymous");
    }
}
