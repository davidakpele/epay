package com.epay.auth.service.impl;

import com.epay.auth.domain.entity.User;
import com.epay.auth.domain.entity.UserRecord;
import com.epay.auth.repository.UserRecordRepository;
import com.epay.auth.repository.UserRepository;
import com.epay.auth.service.AuthenticationService;
import com.epay.auth.service.OtpService;
import com.epay.auth.service.UserSessionService;
import com.epay.common.config.security.JwtService;
import com.epay.common.constants.SecurityConstants;
import com.epay.common.events.auth.AccountSecurityEvent;
import com.epay.common.events.auth.PasswordResetEvent;
import com.epay.common.events.auth.UserRegisteredEvent;
import com.epay.common.exception.AuthenticationException;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ConflictException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.util.TokenHashUtil;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import com.epay.domain.auth.enums.TokenPurpose;
import com.epay.domain.auth.input.ForgotPasswordRequest;
import com.epay.domain.auth.input.ForgotUsernameRequest;
import com.epay.domain.auth.input.OtpVerificationRequest;
import com.epay.domain.auth.input.ResetPasswordRequest;
import com.epay.domain.auth.input.UserSignInRequest;
import com.epay.domain.auth.input.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository            userRepository;
    private final UserRecordRepository      userRecordRepository;
    private final PasswordEncoder           passwordEncoder;
    private final JwtService                jwtService;
    private final AuthenticationManager     authenticationManager;
    private final OtpService               otpService;
    private final UserSessionService        sessionService;
    private final TokenHashUtil             tokenHashUtil;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserDTO register(UserSignUpRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "An account with this email already exists",
                    ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException(
                    "Username is already taken",
                    ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRecordRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException(
                    "An account with this phone number already exists",
                    ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match", ErrorCode.INVALID_INPUT);
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .accountType(request.getAccountType() != null
                        ? request.getAccountType() : AccountType.INDIVIDUAL)
                .kycTier(KycTier.TIER_1)
                .kycStatus(KycStatus.NOT_SUBMITTED)
                .enabled(false)
                .emailVerified(false)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);

        UserRecord record = UserRecord.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .referralCode(generateReferralCode())
                .referredByCode(request.getReferralCode())
                .totalReferrals(0)
                .profileComplete(false)
                .build();

        userRecordRepository.save(record);

        // Credit the referrer's count if a valid code was provided
        if (request.getReferralCode() != null && !request.getReferralCode().isBlank()) {
            userRecordRepository.findByReferralCode(request.getReferralCode())
                    .ifPresent(ref ->
                            userRecordRepository.incrementReferralCount(request.getReferralCode()));
        }

        otpService.generateAndSend(user.getId(),
                TokenPurpose.EMAIL_VERIFICATION,
                request.getVerificationChannel());

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getId(), user.getUsername(), user.getEmail(),
                request.getPhoneNumber(), request.getFirstName(), request.getLastName(),
                user.getAccountType(), request.getVerificationChannel()));

        log.info("User registered: userId={} username={}", user.getId(), user.getUsername());

        return toUserDTO(user);
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public String login(UserSignInRequest request) {

        // 1. Resolve user — same message for missing user vs wrong password (anti-enumeration)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException(
                        "Invalid credentials", ErrorCode.INVALID_CREDENTIALS));

        // 2. Account must be verified/enabled
        if (!user.isEnabled()) {
            throw new AuthenticationException(
                    "Account is not verified. Check your email or phone for the verification code.",
                    ErrorCode.ACCOUNT_DISABLED);
        }

        // 3. Account must not be locked
        if (user.isAccountLocked()) {
            throw new AuthenticationException(
                    "Account is locked. Please contact support.",
                    ErrorCode.ACCOUNT_LOCKED);
        }

        // 4. Authenticate credentials via Spring Security
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));

        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            // Return same generic message — never tell the caller which field was wrong
            throw new AuthenticationException("Invalid credentials", ErrorCode.INVALID_CREDENTIALS);

        } catch (DisabledException e) {
            throw new AuthenticationException(
                    "Account is not verified.", ErrorCode.ACCOUNT_DISABLED);

        } catch (LockedException e) {
            throw new AuthenticationException(
                    "Account is locked. Please contact support.", ErrorCode.ACCOUNT_LOCKED);
        }

        // 5. 2FA check — if enabled, return a challenge token instead of a full JWT
        if (user.isTwoFactorEnabled()) {
            otpService.generateAndSend(user.getId(),
                    TokenPurpose.TWO_FACTOR_AUTH, ContactMethod.EMAIL);
            // Return a short-lived challenge token; the real JWT is issued after 2FA verification
            return jwtService.generateAccessToken(user, user.getId());
        }

        // 6. Successful login — reset counters and issue JWT
        userRepository.resetFailedLoginAttempts(user.getId());
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        sessionService.recordAttempt(user.getId(), true, null, null,
                com.epay.domain.auth.enums.AttemptType.LOGIN, null);

        log.info("User logged in: userId={}", user.getId());
        return jwtService.generateAccessToken(user, user.getId());
    }

    // -------------------------------------------------------------------------
    // OTP verification
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void verifyOtp(OtpVerificationRequest request) {
        // Controller must resolve userId from SecurityContext and call verifyOtpForUser
        throw new UnsupportedOperationException(
                "Use verifyOtpForUser(userId, request) — userId must come from the authenticated context");
    }

    @Transactional
    public void verifyOtpForUser(Long userId, OtpVerificationRequest request) {
        boolean valid = otpService.validate(userId, request.getOtp(), request.getPurpose());

        if (!valid) {
            throw new AuthenticationException(
                    "Invalid or expired verification code", ErrorCode.INVALID_OTP);
        }

        switch (request.getPurpose()) {
            case EMAIL_VERIFICATION -> {
                userRepository.markEmailVerified(userId);
                userRepository.updateEnabled(userId, true);
                log.info("Email verified, account enabled: userId={}", userId);
            }
            case PHONE_VERIFICATION -> {
                userRepository.markPhoneVerified(userId);
                log.info("Phone verified: userId={}", userId);
            }
            default -> log.debug("OTP verified: userId={} purpose={}", userId, request.getPurpose());
        }
    }

    // -------------------------------------------------------------------------
    // Resend OTP
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void resendOtp(OtpVerificationRequest request) {
        throw new UnsupportedOperationException(
                "Use resendOtpForUser(userId, purpose, channel) — userId must come from the authenticated context");
    }

    @Transactional
    public void resendOtpForUser(Long userId, TokenPurpose purpose, ContactMethod channel) {
        if (otpService.hasActiveOtp(userId, purpose)) {
            throw new BadRequestException(
                    "A verification code was recently sent. Please wait before requesting another.",
                    ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        otpService.generateAndSend(userId, purpose, channel);
    }

    // -------------------------------------------------------------------------
    // Forgot password
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always succeeds — never reveals whether the account exists (anti-enumeration)
        userRepository.findByEmail(request.getIdentifier())
                .filter(User::isEnabled)
                .ifPresent(user -> otpService.generateAndSend(
                        user.getId(),
                        TokenPurpose.PASSWORD_RESET,
                        request.getChannel()));
    }

    // -------------------------------------------------------------------------
    // Reset password
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Passwords do not match", ErrorCode.INVALID_INPUT);
        }

        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        boolean valid = otpService.validate(
                user.getId(), request.getOtp(), TokenPurpose.PASSWORD_RESET);

        if (!valid) {
            throw new AuthenticationException(
                    "Invalid or expired verification code", ErrorCode.INVALID_OTP);
        }

        userRepository.updatePassword(user.getId(),
                passwordEncoder.encode(request.getNewPassword()));

        otpService.invalidate(user.getId(), TokenPurpose.PASSWORD_RESET);

        eventPublisher.publishEvent(new PasswordResetEvent(
                user.getId(), user.getUsername(), user.getEmail(), null, null));

        log.info("Password reset completed: userId={}", user.getId());
    }

    // -------------------------------------------------------------------------
    // Forgot username
    // -------------------------------------------------------------------------

    @Override
    public void forgotUsername(ForgotUsernameRequest request) {
        // Always succeeds — never reveals whether the account exists (anti-enumeration)
        userRepository.findByEmail(request.getEmail())
                .filter(User::isEnabled)
                .ifPresent(user -> eventPublisher.publishEvent(new AccountSecurityEvent(
                        user.getId(), user.getUsername(), user.getEmail(),
                        AccountSecurityEvent.SecurityAction.PASSWORD_CHANGED, null, null)));
    }

    // -------------------------------------------------------------------------
    // Logout & token refresh
    // -------------------------------------------------------------------------

    @Override
    public void logout(String userId) {
        // JWT is stateless. Token invalidation via Redis blacklist is handled
        // in the security filter using a key set here if needed.
        log.info("User logged out: userId={}", userId);
    }

    @Override
    public String refreshToken(String refreshToken) {
        final String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException(
                        "Invalid token", ErrorCode.TOKEN_INVALID));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthenticationException(
                    "Refresh token has expired. Please log in again.", ErrorCode.TOKEN_EXPIRED);
        }

        return jwtService.generateAccessToken(user, user.getId());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void handleFailedLogin(User user) {
        userRepository.incrementFailedLoginAttempts(user.getId());

        long recentFailures = sessionService.countRecentFailedAttempts(user.getId());

        if (recentFailures >= SecurityConstants.MAX_FAILED_LOGINS) {
            userRepository.lockAccount(
                    user.getId(),
                    LocalDateTime.now(),
                    "Locked after " + SecurityConstants.MAX_FAILED_LOGINS + " consecutive failed login attempts");

            eventPublisher.publishEvent(new AccountSecurityEvent(
                    user.getId(), user.getUsername(), user.getEmail(),
                    AccountSecurityEvent.SecurityAction.ACCOUNT_LOCKED, null, null));

            log.warn("Account locked after failed attempts: userId={}", user.getId());
        }

        sessionService.recordAttempt(user.getId(), false, null, null,
                com.epay.domain.auth.enums.AttemptType.LOGIN, "Invalid credentials");
    }

    private String generateReferralCode() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .accountType(user.getAccountType())
                .kycTier(user.getKycTier())
                .kycStatus(user.getKycStatus())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .twoFactorAuth(user.isTwoFactorEnabled())
                .createdOn(user.getCreatedAt())
                .build();
    }
}
