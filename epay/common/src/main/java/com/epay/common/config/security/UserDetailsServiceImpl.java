package com.epay.common.config.security;

import com.epay.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
        try {
            UUID uuid = UUID.fromString(subject);
            return userRepository.findByUserUuid(uuid)
                    .orElseThrow(() -> {
                        log.debug("User not found by UUID during authentication: {}", subject);
                        return new UsernameNotFoundException("User not found");
                    });
        } catch (IllegalArgumentException ignored) {

        }
        return userRepository.findByUsername(subject)
                .orElseThrow(() -> {
                    log.debug("User not found by username during authentication: {}", subject);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
