package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminTwoFactorAuthService;
import com.example.admin_api_service.enums.TwoFactorMethod;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminTwoFactorAuth;
import com.example.admin_api_service.repository.AdminTwoFactorAuthRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
@Transactional
public class AdminTwoFactorAuthServiceImpl implements IAdminTwoFactorAuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;
    private static final int BACKUP_CODE_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;

    private final AdminTwoFactorAuthRepository twoFactorAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public AdminTwoFactorAuthServiceImpl(AdminTwoFactorAuthRepository twoFactorAuthRepository,
                                         PasswordEncoder passwordEncoder,
                                         ObjectMapper objectMapper) {
        this.twoFactorAuthRepository = twoFactorAuthRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminTwoFactorAuth initiate2FA(String adminUserId, TwoFactorMethod method) {
        twoFactorAuthRepository.findByAdminUserIdAndMethodAndIsEnabledTrue(adminUserId, method)
                .ifPresent(existing -> {
                    throw new ConflictException("2FA method " + method + " is already active for this admin");
                });

        AdminTwoFactorAuth twoFA = new AdminTwoFactorAuth();
        twoFA.setAdminUserId(adminUserId);
        twoFA.setMethod(method);
        twoFA.setEnabled(false);
        twoFA.setVerified(false);

        if (method == TwoFactorMethod.TOTP || method == TwoFactorMethod.AUTHENTICATOR_APP) {
            String secret = new DefaultSecretGenerator().generate();
            twoFA.setSecret(secret); // Store encrypted in production via @Convert
        }

        return twoFactorAuthRepository.save(twoFA);
    }

    @Override
    public AdminTwoFactorAuth verify2FA(String adminUserId, TwoFactorMethod method, String code) {
        AdminTwoFactorAuth twoFA = twoFactorAuthRepository
                .findByAdminUserIdAndMethod(adminUserId, method)
                .orElseThrow(() -> new ResourceNotFoundException("AdminTwoFactorAuth", "adminUserId+method", adminUserId));

        checkLockStatus(twoFA);

        boolean valid = verifyTotpCode(twoFA.getSecret(), code);
        if (!valid) {
            incrementFailedAttempts(adminUserId);
            throw new BadRequestException("Invalid 2FA code");
        }

        twoFA.setVerified(true);
        twoFA.setFailedAttempts(0);
        twoFA.setLockedUntil(null);
        return twoFactorAuthRepository.save(twoFA);
    }

    @Override
    public void enable2FA(String adminUserId, TwoFactorMethod method) {
        AdminTwoFactorAuth twoFA = twoFactorAuthRepository
                .findByAdminUserIdAndMethod(adminUserId, method)
                .orElseThrow(() -> new ResourceNotFoundException("AdminTwoFactorAuth", "adminUserId+method", adminUserId));

        if (!twoFA.isVerified()) {
            throw new BadRequestException("2FA must be verified before it can be enabled");
        }

        List<String> plainBackupCodes = generateBackupCodes();
        List<String> hashedCodes = plainBackupCodes.stream()
                .map(passwordEncoder::encode)
                .collect(Collectors.toList());

        try {
            twoFA.setBackupCodes(objectMapper.writeValueAsString(hashedCodes));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize backup codes", e);
        }

        twoFA.setEnabled(true);
        twoFA.setBackupCodesRemainingCount(BACKUP_CODE_COUNT);
        twoFA.setEnabledAt(LocalDateTime.now());
        twoFactorAuthRepository.save(twoFA);
    }

    @Override
    public void disable2FA(String adminUserId, TwoFactorMethod method, String disabledBy) {
        AdminTwoFactorAuth twoFA = twoFactorAuthRepository
                .findByAdminUserIdAndMethod(adminUserId, method)
                .orElseThrow(() -> new ResourceNotFoundException("AdminTwoFactorAuth", "adminUserId+method", adminUserId));

        twoFA.setEnabled(false);
        twoFA.setVerified(false);
        twoFA.setSecret(null);
        twoFA.setBackupCodes(null);
        twoFA.setBackupCodesRemainingCount(0);
        twoFactorAuthRepository.save(twoFA);
    }

    @Override
    public boolean validate2FACode(String adminUserId, String code) {
        return twoFactorAuthRepository.findByAdminUserIdAndIsEnabledTrue(adminUserId)
                .stream()
                .anyMatch(twoFA -> {
                    checkLockStatus(twoFA);
                    return verifyTotpCode(twoFA.getSecret(), code);
                });
    }

    @Override
    public boolean validateBackupCode(String adminUserId, String backupCode) {
        List<AdminTwoFactorAuth> methods = twoFactorAuthRepository.findByAdminUserIdAndIsEnabledTrue(adminUserId);

        for (AdminTwoFactorAuth twoFA : methods) {
            if (twoFA.getBackupCodes() == null || twoFA.getBackupCodesRemainingCount() == 0) continue;

            try {
                List<String> hashedCodes = objectMapper.readValue(
                        twoFA.getBackupCodes(), new TypeReference<>() {});

                for (int i = 0; i < hashedCodes.size(); i++) {
                    if (hashedCodes.get(i) != null && passwordEncoder.matches(backupCode, hashedCodes.get(i))) {
                        // Invalidate the used backup code
                        hashedCodes.set(i, null);
                        twoFA.setBackupCodes(objectMapper.writeValueAsString(hashedCodes));
                        twoFA.setBackupCodesRemainingCount(twoFA.getBackupCodesRemainingCount() - 1);
                        twoFactorAuthRepository.save(twoFA);
                        return true;
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to validate backup code", e);
            }
        }
        return false;
    }

    @Override
    public List<String> regenerateBackupCodes(String adminUserId) {
        AdminTwoFactorAuth twoFA = twoFactorAuthRepository.findByAdminUserIdAndIsEnabledTrue(adminUserId)
                .stream().findFirst()
                .orElseThrow(() -> new BadRequestException("No active 2FA method found for this admin"));

        List<String> plainCodes = generateBackupCodes();
        List<String> hashedCodes = plainCodes.stream()
                .map(passwordEncoder::encode)
                .collect(Collectors.toList());

        try {
            twoFA.setBackupCodes(objectMapper.writeValueAsString(hashedCodes));
            twoFA.setBackupCodesRemainingCount(BACKUP_CODE_COUNT);
            twoFactorAuthRepository.save(twoFA);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to regenerate backup codes", e);
        }

        return plainCodes; // Return plain codes once — never again
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminTwoFactorAuth> getActive2FA(String adminUserId) {
        return twoFactorAuthRepository.findByAdminUserIdAndIsEnabledTrue(adminUserId)
                .stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTwoFactorAuth> getAll2FAMethods(String adminUserId) {
        return twoFactorAuthRepository.findAllByAdminUserId(adminUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean is2FAEnabled(String adminUserId) {
        return !twoFactorAuthRepository.findByAdminUserIdAndIsEnabledTrue(adminUserId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean is2FALocked(String adminUserId) {
        return twoFactorAuthRepository.findAllByAdminUserId(adminUserId).stream()
                .anyMatch(twoFA -> twoFA.getLockedUntil() != null
                        && twoFA.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Override
    public void resetFailedAttempts(String adminUserId) {
        twoFactorAuthRepository.findAllByAdminUserId(adminUserId).forEach(twoFA -> {
            twoFA.setFailedAttempts(0);
            twoFA.setLockedUntil(null);
        });
    }

    @Override
    public void incrementFailedAttempts(String adminUserId) {
        twoFactorAuthRepository.findAllByAdminUserId(adminUserId).forEach(twoFA -> {
            int attempts = twoFA.getFailedAttempts() + 1;
            twoFA.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                twoFA.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            }
            twoFactorAuthRepository.save(twoFA);
        });
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void checkLockStatus(AdminTwoFactorAuth twoFA) {
        if (twoFA.getLockedUntil() != null && twoFA.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("2FA is locked until " + twoFA.getLockedUntil()
                    + ". Too many failed attempts.");
        }
    }

    private boolean verifyTotpCode(String secret, String code) {
        try {
            CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
            DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
            verifier.setAllowedTimePeriodDiscrepancy(1);
            return verifier.isValidCode(secret, code);
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> generateBackupCodes() {
        SecureRandom random = new SecureRandom();
        List<String> codes = new ArrayList<>();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
            codes.add(code.toString());
        }
        return codes;
    }
}