package com.epay.auth.adapter;

import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletUserLookupAdapter implements UserLookupPort {

    private final UserRepository       userRepository;
    private final UserRecordRepository userRecordRepository;

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

    @Override
    public Optional<String> findEmailByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getEmail());
    }

    @Override
    public Optional<String> findFullNameByUserId(Long userId) {
        return userRecordRepository.findByUserId(userId)
                .map(r -> r.getFirstName() + " " + r.getLastName());
    }
}
