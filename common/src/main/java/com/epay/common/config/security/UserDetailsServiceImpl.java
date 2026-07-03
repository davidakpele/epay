package com.epay.common.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a user by username for Spring Security's authentication mechanism.
 *
 * The actual database lookup is delegated to a {@link UserLookupPort} — an interface
 * defined here in common but implemented in the auth module.
 * This keeps common free of any JPA/entity dependency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserLookupPort userLookupPort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userLookupPort.findByUsername(username)
                .orElseThrow(() -> {
                    log.debug("User not found during authentication: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
