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

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserLookupPort userLookupPort;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String subject) {
        try {
            UUID uuid = UUID.fromString(subject);
            return userLookupPort.findByUserUuid(uuid)
                    .orElseThrow(() -> {
                        log.debug("[Auth] User not found by UUID: {}", subject);
                        return new UsernameNotFoundException("User not found");
                    });
        } catch (IllegalArgumentException ignored) {
        }

        return userLookupPort.findByUsername(subject)
                .orElseThrow(() -> {
                    log.debug("[Auth] User not found by username: {}", subject);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
