package com.epay.auth.config;

import com.epay.auth.repository.UserRepository;
import com.epay.common.config.security.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implements the {@link UserLookupPort} defined in common.
 * Bridges the security layer to the auth module's UserRepository.
 *
 * Dependency direction:
 *   common defines UserLookupPort  ←  auth implements it here
 *   common never imports auth.
 */
@Component
@RequiredArgsConstructor
public class UserLookupAdapter implements UserLookupPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> (UserDetails) user);
    }
}
