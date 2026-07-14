package com.epay.auth.service;

import com.epay.auth.interfaces.IUserAttemptService;
import com.epay.domain.auth.entity.UserAttempt;
import com.epay.domain.auth.enums.AttemptType;
import com.epay.domain.auth.repository.UserAttemptRepository;
import com.epay.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Tracks failed login attempts and resets them on successful authentication.
 * After 5 consecutive failures the account is locked (enforced in UserRepository).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAttemptService implements IUserAttemptService {

    private static final int MAX_ATTEMPTS = 5;

    private final UserAttemptRepository userAttemptRepository;
    private final UserRepository        userRepository;

    @Override
    @Transactional
    public ResponseEntity<?> createFailAttempt(Long userId, AttemptType type) {
        UserAttempt attempt = userAttemptRepository
                .findFirstByUserIdOrderByCreatedOnDesc(userId)
                .orElseGet(() -> {
                    UserAttempt a = new UserAttempt();
                    a.setUserId(userId);
                    a.setAttemptType(type);
                    a.setCounter(0);
                    a.setSuccess(false);
                    return a;
                });

        attempt.setAttemptType(type);
        attempt.setTimestamp(Timestamp.from(Instant.now()));
        attempt.setSuccess(false);
        attempt.setCounter(attempt.getCounter() == null ? 1 : attempt.getCounter() + 1);
        userAttemptRepository.save(attempt);

        // Lock account after MAX_ATTEMPTS consecutive failures
        if (attempt.getCounter() >= MAX_ATTEMPTS) {
            userRepository.lockAccount(userId, java.time.LocalDateTime.now(),
                    "Account locked after " + MAX_ATTEMPTS + " failed login attempts");
            log.warn("[Attempt] Account locked for userId={} after {} failed attempts", userId, attempt.getCounter());
        }

        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> UpdateUserAccount(Long userId) {
        // Reset attempt counter on successful login
        userAttemptRepository.findFirstByUserIdOrderByCreatedOnDesc(userId).ifPresent(attempt -> {
            attempt.setCounter(0);
            attempt.setSuccess(true);
            attempt.setTimestamp(Timestamp.from(Instant.now()));
            userAttemptRepository.save(attempt);
        });

        // Ensure account is unlocked after successful auth
        userRepository.unlockAccount(userId);

        log.debug("[Attempt] Reset attempts for userId={}", userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteAttempt(Long userId) {
        userAttemptRepository.findByUserId(userId).forEach(userAttemptRepository::delete);
        return ResponseEntity.ok().build();
    }
}
