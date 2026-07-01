package com.example.auth_user_service.services;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional; 
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.auth_user_service.components.KeyWrapper;
import com.example.auth_user_service.components.NotificationProperties;
import com.example.auth_user_service.enums.AttemptType;
import com.example.auth_user_service.enums.ContactMethod;
import com.example.auth_user_service.enums.OTPType;
import com.example.auth_user_service.enums.Role;
import com.example.auth_user_service.enums.UserStatus;
import com.example.auth_user_service.httpClients.NotificationServiceClient;
import com.example.auth_user_service.interfaces.IAuthenticationService;
import com.example.auth_user_service.security.RedisRateLimitService;
import com.example.auth_user_service.interfaces.IAuthorizeUserVerificationService;
import com.example.auth_user_service.interfaces.IMessagingService;
import com.example.auth_user_service.interfaces.ITwoFactorAuthenticationService;
import com.example.auth_user_service.interfaces.IUserAttemptService;
import com.example.auth_user_service.interfaces.IUserTracerService;
import com.example.auth_user_service.interfaces.IWalletServiceClient;
import com.example.auth_user_service.models.AuthorizeUserVerification;
import com.example.auth_user_service.models.TwoFactorAuthentication;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.models.UserTracer;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.exceptions.Error;
import com.example.auth_user_service.models.VerificationToken;
import com.example.auth_user_service.payloads.UserSignInRequest;
import com.example.auth_user_service.payloads.UserSignUpRequest;
import com.example.auth_user_service.payloads.ForgotPasswordRequest;
import com.example.auth_user_service.payloads.ConfirmResetPasswordRequest;
import com.example.auth_user_service.payloads.ForgotUsernameRequest;
import com.example.auth_user_service.repositories.AuthorizeUserVerificationRepository;
import com.example.auth_user_service.repositories.UserRecordRepository;
import com.example.auth_user_service.repositories.UsersRepository;
import com.example.auth_user_service.repositories.VerificationTokenRepository;
import com.example.auth_user_service.responses.AuthResponse;
import com.example.auth_user_service.responses.VerificationTokenResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class AuthenticationService implements IAuthenticationService{
    
    private static final int EXPIRATION_MINUTES = 15;
    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationServiceClient notificationServiceClient;
    private final AuthenticationManager authenticationManager;
    private final KeyWrapper keysWrapper;
    private final IAuthorizeUserVerificationService authorizeUserVerificationService;
    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;
    private final ITwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation;
    private final IWalletServiceClient walletServiceClient;
    private final IUserTracerService userTracerService;
    private final IUserAttemptService userAttemptService;
    private final IMessagingService messagingService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final NotificationProperties notificationProperties;
    private final RedisRateLimitService redisRateLimitService;

    private static final DateTimeFormatter LOGIN_TIME_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");

    private String formatNow() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(LOGIN_TIME_FMT);
    }

    /** Extracts the real client IP, checking X-Forwarded-For first. */
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

    /** Extracts a short device description from the User-Agent header. */
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
            return Error.createResponse("Invalid or expired verification code.",
                    HttpStatus.BAD_REQUEST, "The verification code you entered is invalid or has expired.");
        }

        Long nextUserId = getNextUserId();
        Users user = buildUser(request, nextUserId);
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

        return Error.createResponse("success", HttpStatus.CREATED,
                "Thanks for signing up! Your account has been created successfully.");
    }

    @Override
    public ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        Map<String, Object> authResponse = new LinkedHashMap<>();
        Users user = userRepository.findByUsername(request.getUsername()).orElse(null);
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
                if (com.example.auth_user_service.enums.UserStatus.SUSPENDED
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
            // ─────────────────────────────────────────────────────────────────

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

        Users user = userRepository.findById(verificationToken.getUserId()).orElse(null);
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

        Users user = userRepository.findById(verificationToken.getUserId())
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
    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<Users> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ── Forgot Password (step 1 — send OTP) ──────────────────────────────────

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
        Users user = null;
        if ("EMAIL".equals(method)) {
            user = userRepository.findByEmail(identifier).orElse(null);
        } else if ("PHONE".equals(method)) {
            UserRecord rec = userRecordRepository.findByTelephone(identifier).orElse(null);
            if (rec != null) user = rec.getUser();
        }

        // Always respond 200 to avoid account enumeration, but only send OTP if account exists
        if (user == null || !user.isEnabled()) {
            // Start cool-down even for non-existent accounts to prevent enumeration via timing
            redisRateLimitService.startCoolDown(coolDownKey, Duration.ofMinutes(2));
            response.put("success", true);
            response.put("message", "If that account exists, a reset code has been sent.");
            return ResponseEntity.ok(response);
        }

        // ── Generate and store 4-digit OTP keyed on the normalised identifier ──
        String normalizedId = MessagingService.normalizeIdentifier(identifier);
        String otp = MessagingService.generateAndStoreOTP(normalizedId,
                OTPType.NUMERIC, 4, 10);

        // ── Start cool-down BEFORE sending so it's set even if the async call fails ──
        redisRateLimitService.startCoolDown(coolDownKey, Duration.ofMinutes(2));

        // ── Send OTP asynchronously — never block on notification failure ──
        final Users finalUser = user;
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

    // ── Reset Password (step 2 — verify OTP and update password) ─────────────

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
        Users user = userRepository.findByEmail(identifier).orElse(null);
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
        final Users finalUser = user;
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

    // ── Forgot Username ───────────────────────────────────────────────────────

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
        Users user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.isEnabled()) {
            final Users finalUser = user;
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

    private Users buildUser(UserSignUpRequest request, Long id) {
        Users user = new Users();
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

    private UserRecord buildUserRecord(UserSignUpRequest request, Users user) {
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

    private void activateUserRecord(Users user) {
        userRecordRepository.findByUserId(user.getId()).ifPresent(record -> {
            record.setStatus(UserStatus.ACTIVE);
            record.setLocked(false);
            record.setBlocked(false);
            userRecordRepository.save(record);
        });
        user.setEnabled(true);
        userRepository.save(user);
    }

    private ResponseEntity<?> handleTwoFactorAuth(Users user, Map<String, Object> authResponse) {
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
