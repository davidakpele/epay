package com.epay.auth.adapter;

import com.epay.auth.repository.UserRepository;
import com.epay.wallet.port.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implements the wallet module's UserLookupPort.
 * Bridges wallet → user validation without wallet importing auth entities.
 */
@Component
@RequiredArgsConstructor
public class WalletUserLookupAdapter implements UserLookupPort {

    private final UserRepository userRepository;

    @Override
    public Optional<Long> findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(u -> u.isEnabled() && !u.isAccountLocked())
                .map(u -> u.getId());
    }

    @Override
    public boolean existsActiveUser(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.isEnabled() && !u.isAccountLocked())
                .orElse(false);
    }
}
