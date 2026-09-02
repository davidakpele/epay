package com.epay.common.config.components;

import com.epay.common.config.security.UserLookupPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Primary {@link UserDetailsService} used by {@code JwtAuthenticationFilter}.
 *
 * The {@code sub} claim in new-format JWTs is a UUID (userUuid), not a username.
 * This service tries UUID-based lookup first, then falls back to username for
 * legacy tokens and synthetic admin principals built inside the filter.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserLookupPort userLookupPort;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String subject) {
        // ── 1. Try UUID lookup (new token format: sub = userUuid) ─────────────
        try {
            UUID uuid = UUID.fromString(subject);
            return userLookupPort.findByUserUuid(uuid)
                    .orElseThrow(() -> {
                        log.debug("[Auth] User not found by UUID: {}", subject);
                        return new UsernameNotFoundException("User not found");
                    });
        } catch (IllegalArgumentException ignored) {
            // subject is not a UUID — fall through to username lookup
        }

        // ── 2. Fallback: plain username (admin synthetic principals, legacy tokens) ─
        return userLookupPort.findByUsername(subject)
                .orElseThrow(() -> {
                    log.debug("[Auth] User not found by username: {}", subject);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
