package com.epay.auth.config;

import com.epay.common.config.security.UserLookupPort;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserLookupAdapter implements UserLookupPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> (UserDetails) user);
    }

    @Override
    public Optional<Long> findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId);
    }

    @Override
    public Optional<UserDetails> findByUserUuid(UUID userUuid) {
        return userRepository.findByUserUuid(userUuid)
                .map(user -> (UserDetails) user);
    }
}
