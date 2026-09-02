package com.epay.config;

import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Runs once on every startup.
 *
 * Creates the default SUPER_USER account using credentials from
 * {@link SuperAdminProperties}.  If the username or email already
 * exists the seeder skips silently — no duplicate, no exception.
 *
 * The account is:
 *   - Enabled immediately (no email verification needed)
 *   - email + phone verified
 *   - KYC tier TIER_1, status NOT_SUBMITTED (admin doesn't go through KYC)
 *   - No wallet created (super admin doesn't need one)
 *
 * To change the credentials after first boot, update the database
 * directly or via the admin panel — the seeder will not overwrite
 * an existing account.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements ApplicationRunner {

    private final UserRepository        userRepository;
    private final UserRecordRepository  userRecordRepository;
    private final PasswordEncoder       passwordEncoder;
    private final SuperAdminProperties  props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        String username = props.getUsername().trim().toLowerCase();
        String email    = props.getEmail().trim().toLowerCase();

        // ── Guard: skip entirely if username OR email already exists ─────────
        if (userRepository.existsByUsername(username)) {
            log.info("[Seeder] Super-admin '{}' already exists — skipping.", username);
            return;
        }
        if (userRepository.existsByEmail(email)) {
            log.info("[Seeder] Super-admin email '{}' already registered — skipping.", email);
            return;
        }

        // ── Create User ───────────────────────────────────────────────────────
        User superAdmin = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(props.getPassword()))
                .role(Role.SUPER_USER)
                .accountType(AccountType.INDIVIDUAL)
                .kycTier(KycTier.TIER_1)
                .kycStatus(KycStatus.NOT_SUBMITTED)
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(true)
                .twoFactorEnabled(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(superAdmin);

        // ── Create profile record ─────────────────────────────────────────────
        UserRecord record = UserRecord.builder()
                .user(superAdmin)
                .firstName(props.getFirstName())
                .lastName(props.getLastName())
                .phoneNumber(props.getPhone() != null && !props.getPhone().isBlank()
                        ? props.getPhone() : null)
                .referralCode(UUID.randomUUID().toString())
                .profileComplete(true)
                .build();

        userRecordRepository.save(record);

        log.info("[Seeder] ✓ Super-admin created successfully.");
        log.info("[Seeder]   Username : {}", username);
        log.info("[Seeder]   Email    : {}", email);
        log.info("[Seeder]   Role     : {}", Role.SUPER_USER);
        log.warn("[Seeder]   ⚠ Change the default password immediately after first login!");
    }
}
