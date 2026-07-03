package com.epay.auth.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional; 
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.oss.protocol.internal.request.AuthResponse;
import com.epay.auth.domain.entity.AuthorizeUserVerification;
import com.epay.auth.domain.entity.TwoFactorAuthentication;
import com.epay.auth.domain.entity.User;
import com.epay.auth.domain.entity.UserRecord;
import com.epay.auth.domain.entity.UserTracer;
import com.epay.auth.domain.entity.VerificationToken;
import com.epay.auth.interfaces.IAuthenticationService;
import com.epay.auth.interfaces.IAuthorizeUserVerificationService;
import com.epay.auth.interfaces.IMessagingService;
import com.epay.auth.interfaces.ITwoFactorAuthenticationService;
import com.epay.auth.interfaces.IUserAttemptService;
import com.epay.auth.interfaces.IUserTracerService;
import com.epay.auth.repository.AuthorizeUserVerificationRepository;
import com.epay.auth.repository.UserRecordRepository;
import com.epay.auth.repository.UserRepository;
import com.epay.auth.repository.VerificationTokenRepository;
import com.epay.common.config.components.KeyWrapper;
import com.epay.common.config.components.NotificationProperties;
import com.epay.common.config.services.JwtService;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.enums.AttemptType;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.Role;
import com.epay.domain.auth.enums.UserStatus;
import com.epay.domain.auth.input.ConfirmResetPasswordRequest;
import com.epay.domain.auth.input.ForgotPasswordRequest;
import com.epay.domain.auth.input.ForgotUsernameRequest;
import com.epay.domain.auth.input.OtpVerificationRequest;
import com.epay.domain.auth.input.ResetPasswordRequest;
import com.epay.domain.auth.input.UserSignInRequest;
import com.epay.domain.auth.input.UserSignUpRequest;
import com.epay.domain.auth.response.VerificationTokenResult;
import com.epay.notification.service.AuthenticationNotificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class AuthenticationService implements IAuthenticationService{
    
    private static final int EXPIRATION_MINUTES = 15;
    private final UserRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationNotificationService notificationServiceClient;
    private final AuthenticationManager authenticationManager;
    private final KeyWrapper keysWrapper; 
    private final IAuthorizeUserVerificationService authorizeUserVerificationService;
    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;
    private final ITwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation;
    private static final DateTimeFormatter LOGIN_TIME_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");
    private final IUserTracerService userTracerService;
    private final IUserAttemptService userAttemptService;
    private final IMessagingService messagingService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final NotificationProperties notificationProperties;

    private String formatNow() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(LOGIN_TIME_FMT);
    }

    private String extractClientIp(HttpServletRequest req) {
        if (req == null) return "";
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = req.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return req.getRemoteAddr();
    }

    private String extractDevice(HttpServletRequest req) {
        if (req == null) return "Unknown device";
        String ua = req.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) return "Unknown device";
        ua = ua.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobile Browser";
        if (ua.contains("tablet") || ua.contains("ipad")) return "Tablet Browser";
        return "Desktop Browser";
    }

    @Override
    @Transactional
    public ResponseEntity<?> createAccount(UserSignUpRequest request) {
        String identifier = "email".equals(request.getRegMode())
                ? request.getEmail()
                : request.getPhone();

        if (!messagingService.verifyOTP(identifier, request.getVerificationCode())) {
           throw new com.epay.common.exception.AuthenticationException(
                    "The verification code you entered is invalid or has expired.",
                    ErrorCode.INVALID_OTP);
        }

        Long nextUserId = getNextUserId();
        User user = buildUser(request, nextUserId);
        userRepository.save(user);
        UserRecord userRecord = buildUserRecord(request, user);
        userRecordRepository.save(userRecord);

        boolean isEmail = "email".equals(request.getRegMode());

        createWallet(user.getId());

        if (isEmail) {
            authorizeUserVerificationService.save(nextUserId, KeyWrapper.generateUniqueAuthorizeUserId());
            activateUserRecord(user);
        } else {
            activateUserRecord(user);
            messagingService.invalidateOTP(identifier);
            ContactMethod method = "WHATSAPP".equals(request.getVerificationMethod())
                    ? ContactMethod.WHATSAPP
                    : ContactMethod.SMS;
            CompletableFuture.runAsync(() ->
                    messagingService.sendWelcomeMessage(request.getPhone(), request.getUsername(), method));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(com.epay.common.exception.ApiResponse.success(
                "Thanks for signing up! Your account has been created successfully.", null));

    }

    @Override
    public ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        Map<String, Object> authResponse = new LinkedHashMap<>();
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return buildAuthError(authResponse, "Invalid user credentials.", HttpStatus.BAD_REQUEST);
        }

        if (!user.isEnabled()) {
            return buildAuthError(authResponse, "This account has not been verified.", HttpStatus.UNAUTHORIZED);
        }

        if (userTracerService.hasActiveSession(user.getId())) {
            return buildAuthError(authResponse, "This account is already logged in on another device.", HttpStatus.UNAUTHORIZED);
        }

        userAttemptService.createFailAttempt(user.getId(), AttemptType.LOGIN);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserRecord record = userRecordRepository.findByUserId(user.getId()).orElse(null);
            if (record != null) {
                if (record.isLocked()) {
                    return buildAuthError(authResponse,
                            "Sorry, this account is currently locked. Please contact customer service.",
                            HttpStatus.UNAUTHORIZED);
                }
                if (record.isBlocked()) {
                    return buildAuthError(authResponse,
                            "Sorry, this account is currently blocked. Please contact customer service.",
                            HttpStatus.UNAUTHORIZED);
                }
                if (UserStatus.SUSPENDED
                        .equals(record.getStatus())) {
                    return buildAuthError(authResponse,
                            "Sorry, this account is currently suspended. Please contact customer service to reactivate.",
                            HttpStatus.UNAUTHORIZED);
                }
            }

            if (user.isTwoFactorAuth()) {
                return handleTwoFactorAuth(user, authResponse);
            }

            String jwtToken = jwtService.generateToken(user, user.getId());
            UserTracer session = userTracerService.createSession(user);
            userAttemptService.UpdateUserAccount(user.getId());

            UserRecord rec = userRecordRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("User record not found"));

            // ── Login alert notification ──────────────────────────────────────
            final String loginTime = formatNow();
            final String ipAddr    = extractClientIp(httpRequest);
            final String device    = extractDevice(httpRequest);
            final String fullName  = rec.getFirstName() + " " + rec.getLastName();
            CompletableFuture.runAsync(() ->
                notificationServiceClient.sendLoginAlertNotification(
                    user.getEmail(), fullName, user.getUsername(),
                    loginTime, ipAddr, device,
                    notificationProperties.getPhone(),
                    notificationProperties.getEmail()
                )
            ).exceptionally(ex -> { System.err.println("[LoginAlert] " + ex.getMessage()); return null; });
         
            authResponse.put("jwt", jwtToken);
            authResponse.put("email", user.getEmail());
            authResponse.put("userId", user.getId());
            authResponse.put("status", HttpStatus.OK.value());
            authResponse.put("success", true);
            authResponse.put("session", true);
            authResponse.put("sessionId", session.getSessionId());
            authResponse.put("username", user.getUsername());
            authResponse.put("is_verify", user.isEnabled());
            authResponse.put("is_profile_complete", rec.isProfileComplete());
            authResponse.put("referral_username", rec.getReferralUsername());
            authResponse.put("referral_link", rec.getReferralLink());
            authResponse.put("date_of_birth", rec.getDateofBirth());
            authResponse.put("country", rec.getCountry());
            authResponse.put("state", rec.getState());
            authResponse.put("city", rec.getCity());
            authResponse.put("gender", rec.getGender());
            authResponse.put("telephone", rec.getTelephone());
            authResponse.put("fullname", rec.getFirstName() + " " + rec.getLastName());
            authResponse.put("twoFactorAuthEnabled", false);

            return ResponseEntity.ok()
                    .header("X-Session-ID", session.getSessionId())
                    .header("X-Session-Expires", session.getExpiresAt().toString())
                    .body(authResponse);

        } catch (BadCredentialsException e) {
            return buildAuthError(authResponse, "Invalid user credentials.", HttpStatus.BAD_REQUEST);
        } catch (AuthenticationException e) {
            return buildAuthError(authResponse, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> verifyUser(String token, Long id) {
        AuthResponse verifyResponse = new AuthResponse();

        if (authorizeUserVerificationRepository.findUserById(id)) {
            verifyResponse.setMessage("This account has already been verified.");
            verifyResponse.setStatus(true);
            return ResponseEntity.ok(verifyResponse);
        }
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            verifyResponse.setMessage("Invalid verification token.");
            verifyResponse.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(verifyResponse);
        }

        User user = userRepository.findById(verificationToken.getUserId()).orElse(null);
        if (user == null) {
            verifyResponse.setMessage("User not found.");
            verifyResponse.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(verifyResponse);
        }
        if (user.isEnabled()) {
            verifyResponse.setMessage("Hi " + user.getUsername() + ", your account has already been verified.");
            verifyResponse.setStatus(true);
            return ResponseEntity.ok(verifyResponse);
        }

        if (verificationToken.getExpirationTime().getTime() - System.currentTimeMillis() < 0) {
            verifyResponse.setMessage("Verification token has expired. Click resend to get a new token.");
            verifyResponse.setStatus(false);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(verifyResponse);
        }

        try {
            walletServiceClient.createUserWallet(user.getId());
            activateUserRecord(user);
            verificationTokenRepository.delete(verificationToken);

            verifyResponse.setMessage("User registration verified successfully.");
            verifyResponse.setStatus(true);
            return ResponseEntity.ok(verifyResponse);

        } catch (Exception e) {
            verifyResponse.setMessage("Failed to verify user: " + e.getMessage());
            verifyResponse.setStatus(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(verifyResponse);
        }
    }

    @Override
    public VerificationTokenResult generateVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        if (verificationToken == null) {
            return new VerificationTokenResult(false, "Token not found");
        }

        String newToken = UUID.randomUUID().toString();
        verificationToken.setToken(newToken);
        verificationToken.setExpirationTime(calculateExpirationDate(EXPIRATION_MINUTES));
        verificationTokenRepository.save(verificationToken);

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthorizeUserVerification authUser = authorizeUserVerificationRepository
                .findUserByIdOptional(user.getId())
                .orElseThrow(() -> new RuntimeException("Auth verification not found"));

        String verificationLink = keysWrapper.getUrl() + "/auth/verifyRegistration?token=" + newToken + "&id=" + authUser.getId();
        String content = "Dear " + user.getUsername() + ",\n\nThank you for registering. Please verify your email to activate your account.";
        CompletableFuture.runAsync(() ->
                notificationServiceClient.sendVerificationEmail(user.getEmail(), content, verificationLink, user.getUsername()));

        return new VerificationTokenResult(true, verificationToken);
    }

    @Override
    public ResponseEntity<?> createWallet(Long id) {
        try {
            Map<String, Object> result = walletServiceClient.createUserWallet(id).join(); 
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            throw new RuntimeException("Wallet creation failed for userId: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        String identifier = request.getIdentifier() != null ? request.getIdentifier().trim() : "";
        String method     = request.getMethod()     != null ? request.getMethod().trim().toUpperCase() : "";

        if (identifier.isEmpty()) {
            response.put("success", false);
            response.put("message", "Identifier (email or phone) is required.");
            return ResponseEntity.badRequest().body(response);
        }

        // ── Cool-down check (per identifier, 2-minute minimum between requests) ──
        String coolDownKey = "cd:forgot_pw:" + MessagingService.normalizeIdentifier(identifier);
        if (redisRateLimitService.isCoolingDown(coolDownKey)) {
            long ttl = redisRateLimitService.getCoolDownTtlSeconds(coolDownKey);
            response.put("success", false);
            response.put("message", "A reset code was recently sent. Please wait " + ttl + " seconds before requesting again.");
            response.put("retryAfterSeconds", ttl);
            return ResponseEntity.status(429).body(response);
        }

        // ── Look up the user — only proceed if the account exists and is enabled ──
        User user = null;
        if ("EMAIL".equals(method)) {
            user = userRepository.findByEmail(identifier).orElse(null);
        } else if ("PHONE".equals(method)) {
            UserRecord rec = userRecordRepository.findByTelephone(identifier).orElse(null);
            if (rec != null) user = rec.getUser();
        }

        if (user == null || !user.isEnabled()) {
            redisRateLimitService.startCoolDown(coolDownKey, Duration.ofMinutes(2));
            response.put("success", true);
            response.put("message", "If that account exists, a reset code has been sent.");
            return ResponseEntity.ok(response);
        }

        String normalizedId = MessagingService.normalizeIdentifier(identifier);
        String otp = MessagingService.generateAndStoreOTP(normalizedId,
                OTPType.NUMERIC, 4, 10);

        redisRateLimitService.startCoolDown(coolDownKey, Duration.ofMinutes(2));

        final User finalUser = user;
        CompletableFuture.runAsync(() ->
            notificationServiceClient.sendForgotPasswordOtp(
                finalUser.getEmail(),
                finalUser.getUsername(),
                otp
            )
        ).exceptionally(ex -> {
            System.err.println("[ForgotPassword] Failed to send OTP email: " + ex.getMessage());
            return null;
        });

        response.put("success", true);
        response.put("message", "If that account exists, a reset code has been sent.");
        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional
    public ResponseEntity<?> confirmResetPassword(ConfirmResetPasswordRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new LinkedHashMap<>();

        String identifier   = request.getIdentifier()  != null ? request.getIdentifier().trim()  : "";
        String otp          = request.getOtp()          != null ? request.getOtp().trim()          : "";
        String newPassword  = request.getNewPassword()  != null ? request.getNewPassword().trim()  : "";

        if (identifier.isEmpty() || otp.isEmpty() || newPassword.isEmpty()) {
            response.put("success", false);
            response.put("message", "Identifier, OTP and new password are all required.");
            return ResponseEntity.badRequest().body(response);
        }

        // Verify OTP from in-memory store
        String normalizedId = MessagingService.normalizeIdentifier(identifier);
        if (!messagingService.verifyOTP(normalizedId, otp)) {
            response.put("success", false);
            response.put("message", "Invalid or expired OTP. Please request a new code.");
            return ResponseEntity.badRequest().body(response);
        }

        // Validate new password strength
        if (newPassword.length() < 8
                || !newPassword.matches(".*[a-z].*")
                || !newPassword.matches(".*[A-Z].*")
                || !newPassword.matches(".*[0-9].*")
                || !newPassword.matches(".*[^A-Za-z0-9].*")) {
            response.put("success", false);
            response.put("message", "Password must be at least 8 characters and include uppercase, "
                    + "lowercase, number and special character.");
            return ResponseEntity.badRequest().body(response);
        }

        // Resolve user — try email first, then phone
        User user = userRepository.findByEmail(identifier).orElse(null);
        if (user == null) {
            UserRecord rec = userRecordRepository.findByTelephone(identifier).orElse(null);
            if (rec != null) user = rec.getUser();
        }

        if (user == null) {
            // OTP was valid but account vanished — still clear the OTP
            messagingService.invalidateOTP(normalizedId);
            response.put("success", false);
            response.put("message", "Account not found.");
            return ResponseEntity.badRequest().body(response);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate OTP so it cannot be reused
        messagingService.invalidateOTP(normalizedId);

        // Fire account-security alert (PASSWORD_RESET event) asynchronously
        final User finalUser = user;
        final String eventTime = formatNow();
        final String ipAddr    = extractClientIp(httpRequest);
        final String device    = extractDevice(httpRequest);
        userRecordRepository.findByUserId(user.getId()).ifPresent(rec -> {
            final String fullName = rec.getFirstName() + " " + rec.getLastName();
            CompletableFuture.runAsync(() ->
                notificationServiceClient.sendAccountSecurityAlert(
                    finalUser.getEmail(), fullName, finalUser.getUsername(),
                    "PASSWORD_RESET", eventTime, ipAddr, device,
                    notificationProperties.getPhone(), notificationProperties.getEmail()
                )
            ).exceptionally(ex -> { System.err.println("[ResetPassword] " + ex.getMessage()); return null; });
        });

        response.put("success", true);
        response.put("message", "Password has been reset successfully. You can now log in with your new password.");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> forgotUsername(ForgotUsernameRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        String email = request.getEmail() != null ? request.getEmail().trim() : "";

        if (email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email address is required.");
            return ResponseEntity.badRequest().body(response);
        }

        // ── Cool-down check (per email, 2-minute minimum between requests) ──
        String coolDownKey = "cd:forgot_un:" + email.toLowerCase();
        if (redisRateLimitService.isCoolingDown(coolDownKey)) {
            long ttl = redisRateLimitService.getCoolDownTtlSeconds(coolDownKey);
            response.put("success", false);
            response.put("message", "A username reminder was recently sent. Please wait " + ttl + " seconds before requesting again.");
            response.put("retryAfterSeconds", ttl);
            return ResponseEntity.status(429).body(response);
        }

        // ── Start cool-down before processing to prevent rapid-fire requests ──
        redisRateLimitService.startCoolDown(coolDownKey, Duration.ofMinutes(2));

        // ── Look up and send only if the account exists and is active ──
        // Always return 200 — don't leak whether an account exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.isEnabled()) {
            final User finalUser = user;
            userRecordRepository.findByUserId(user.getId()).ifPresent(rec -> {
                final String fullName = rec.getFirstName() + " " + rec.getLastName();
                CompletableFuture.runAsync(() ->
                    notificationServiceClient.sendForgotUsernameEmail(
                        finalUser.getEmail(),
                        finalUser.getUsername(),
                        fullName
                    )
                ).exceptionally(ex -> {
                    System.err.println("[ForgotUsername] Failed to send email: " + ex.getMessage());
                    return null;
                });
            });
        }

        response.put("success", true);
        response.put("message", "If that email address is registered, your username has been sent to it.");
        return ResponseEntity.ok(response);
    }

    private User buildUser(UserSignUpRequest request, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTwoFactorAuth(false);
        user.setRole(Role.USER);

        if ("email".equals(request.getRegMode())) {
            user.setEmail(request.getEmail());
            user.setEnabled(false);
        } else {
            user.setEmail(null);
            user.setEnabled(true);
        }
        return user;
    }

    private UserRecord buildUserRecord(UserSignUpRequest request, User user) {
        String referralCode = UUID.randomUUID().toString();
        UserRecord record = new UserRecord();
        record.setUser(user);
        record.setFirstName(request.getFirstname());
        record.setLastName(request.getLastname());
        record.setTransferPinSet(false);
        record.setLocked(false);
        record.setLockedAt(null);
        record.setBlocked(false);
        record.setProfileComplete(false);
        record.setTotalReferers(null);
        record.setReferralCode(referralCode);
        record.setReferralUsername("n13_" + request.getUsername());
        record.setReferralLink(keysWrapper.getUrl() + "/auth/register?referral_code=" + referralCode);

        if ("phone".equals(request.getRegMode())) {
            record.setTelephone(request.getPhone());
            record.setStatus(UserStatus.ACTIVE);
        } else {
            record.setStatus(UserStatus.PENDING_VERIFICATION);
        }
        return record;
    }

    private void activateUserRecord(User user) {
        userRecordRepository.findByUserId(user.getId()).ifPresent(record -> {
            record.setStatus(UserStatus.ACTIVE);
            record.setLocked(false);
            record.setBlocked(false);
            userRecordRepository.save(record);
        });
        user.setEnabled(true);
        userRepository.save(user);
    }

    private ResponseEntity<?> handleTwoFactorAuth(User user, Map<String, Object> authResponse) {
        String otp = keysWrapper.generateOTP();
        String jwt = keysWrapper.generateUniqueKey();

        // Resolve URL on the main thread BEFORE the async lambda
        String baseUrl = keysWrapper.getUrl();

        TwoFactorAuthentication existing = twoFactorAuthenticationServiceImplementation.findByUser(user.getId());
        if (existing != null) {
            twoFactorAuthenticationServiceImplementation.deleteTwoFactorOtp(existing);
        }

        TwoFactorAuthentication newOtp = twoFactorAuthenticationServiceImplementation
                .createTwoFactorOtp(user, otp, jwt);

        CompletableFuture.runAsync(() ->
                notificationServiceClient.sendOptEmail(
                        user.getEmail(), otp,
                        baseUrl + "/auth/security/password",
                        baseUrl + "/auth/security/configuring-two-factor-authentication",
                        baseUrl + "/auth/security/configuring-two-factor-authentication-recovery-methods"))
            .exceptionally(ex -> {
                System.err.println("[2FA] Failed to send OTP email to " + user.getEmail() + ": " + ex.getMessage());
                return null;
            });

        authResponse.put("message", "Two-factor authentication is enabled.");
        authResponse.put("twoFactorAuthEnabled", true);
        authResponse.put("status", HttpStatus.OK.value());
        authResponse.put("otpId", newOtp.getId().toString());
        authResponse.put("jwt", newOtp.getToken());
        return ResponseEntity.ok(authResponse);
    }

    private ResponseEntity<?> buildAuthError(Map<String, Object> response, String message, HttpStatus status) {
        response.put("status", status.value());
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private Long getNextUserId() {
        return userRepository.findMaxId().orElse(1000L) + 1;
    }

    private Date calculateExpirationDate(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return calendar.getTime();
    }
}
