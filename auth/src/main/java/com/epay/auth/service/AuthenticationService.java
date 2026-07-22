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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epay.auth.interfaces.IAuthenticationService;
import com.epay.auth.interfaces.IAuthorizeUserVerificationService;
import com.epay.auth.interfaces.IMessagingService;
import com.epay.auth.interfaces.ITwoFactorAuthenticationService;
import com.epay.auth.interfaces.IUserAttemptService;
import com.epay.auth.interfaces.IUserTracerService;
import com.epay.common.config.components.KeyWrapper;
import com.epay.common.config.components.NotificationProperties;
import com.epay.common.config.services.JwtService;
import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.AuthenticationException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.interfaces.IAuthNotificationPublisher;
import com.epay.common.interfaces.IWalletPort;
import com.epay.domain.auth.entity.AuthorizeUserVerification;
import com.epay.domain.auth.entity.TwoFactorAuthentication;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.entity.UserTracer;
import com.epay.domain.auth.entity.VerificationToken;
import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.AttemptType;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import com.epay.domain.auth.input.ConfirmResetPasswordRequest;
import com.epay.domain.auth.input.ForgotPasswordRequest;
import com.epay.domain.auth.input.ForgotUsernameRequest;
import com.epay.domain.auth.input.UserSignInRequest;
import com.epay.domain.auth.input.UserSignUpRequest;
import com.epay.domain.auth.repository.AuthorizeUserVerificationRepository;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.auth.repository.VerificationTokenRepository;
import com.epay.domain.auth.response.AuthResponse;
import com.epay.domain.auth.response.VerificationTokenResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor 
public class AuthenticationService implements IAuthenticationService{
    
    private static final int EXPIRATION_MINUTES = 15;
    private final UserRepository userRepository;
    private final IWalletPort walletPort;
    private final UserRecordRepository userRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IAuthNotificationPublisher notificationService;
    private final AuthenticationManager authenticationManager;
    private final KeyWrapper keysWrapper; 
    private final IAuthorizeUserVerificationService authorizeUserVerificationService;
    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;
    private final ITwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
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
        if (request.getFirstname() == null || request.getFirstname().trim().isEmpty()) {
            throw new AuthenticationException("FirstName is required.", ErrorCode.INVALID_INPUT);
        }

        if (request.getLastname() == null || request.getLastname().trim().isEmpty()) {
            throw new AuthenticationException("LastName is required.*",ErrorCode.INVALID_INPUT);
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AuthenticationException("Username is required.", ErrorCode.INVALID_INPUT);
        }
        
        if (existUsername(request.getUsername())) {
            throw new AuthenticationException("Sorry..! Username already been chosen by another user.", ErrorCode.CONFLICT_ON_REQUEST);
        }

        if (request.getRegMode() == null ||
            (!request.getRegMode().equals("email") 
            && !request.getRegMode().equals("phone"))) {
            throw new AuthenticationException("Invalid registration mode.", ErrorCode.INVALID_INPUT);
        }

        if (request.getRegMode().equals("email")) {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new AuthenticationException("Email is required.", ErrorCode.INVALID_INPUT);
            }

            if (!request.getEmail().matches(EMAIL_REGEX)) {
                throw new AuthenticationException("Invalid email address", ErrorCode.INVALID_INPUT);
            }

            if (emailExists(request.getEmail())) {
                throw new AuthenticationException("Sorry..! Email already been used by another user.", ErrorCode.CONFLICT_ON_REQUEST);
            }

        } else {
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new AuthenticationException("Phone number is required", ErrorCode.INVALID_INPUT);
            }

            if (phoneExists(request.getPhone())) {
                throw new AuthenticationException("Sorry..! Phone number already been used by another user.", ErrorCode.INVALID_INPUT);
            }

            if (!"SMS".equals(request.getVerificationMethod()) && 
                !"WHATSAPP".equals(request.getVerificationMethod())) {
                throw new AuthenticationException("Verification method must be SMS or WHATSAPP", ErrorCode.INVALID_INPUT);
            }
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new AuthenticationException("Password is required", ErrorCode.INVALID_INPUT);
        }

        if (request.getPassword().length() < 8) {
            throw new AuthenticationException("Password is too short", ErrorCode.INVALID_INPUT);
        }

        if (!request.getPassword().matches(".*[a-z].*")) {
            throw new AuthenticationException("Password must contain lowercase letter.", ErrorCode.INVALID_INPUT);
        }

        if (!request.getPassword().matches(".*[A-Z].*")) {
            throw new AuthenticationException("Password must contain uppercase letter.", ErrorCode.INVALID_INPUT);
        }

        if (!request.getPassword().matches(".*[0-9].*")) {
            throw new AuthenticationException("Password must contain a number.", ErrorCode.INVALID_INPUT);
        }

        if (!request.getPassword().matches(".*[^A-Za-z0-9].*")) {
            throw new AuthenticationException("Password must contain special character.", ErrorCode.INVALID_INPUT);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Passwords do not match.", ErrorCode.INVALID_INPUT);
        }

        if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
            throw new AuthenticationException("Verification code is required.", ErrorCode.INVALID_INPUT);
        }
   
        String identifier = "email".equals(request.getRegMode())
        ? request.getEmail()
        : request.getPhone();

        if (!messagingService.verifyOTP(identifier, request.getVerificationCode())) {
            throw new AuthenticationException("The verification code you entered is invalid or has expired.", ErrorCode.INVALID_OTP);
        }

        User user = buildUser(request);
        userRepository.save(user);
        UserRecord userRecord = buildUserRecord(request, user);
        userRecordRepository.save(userRecord);

        boolean isEmail = "email".equals(request.getRegMode());

        if (isEmail) {
            authorizeUserVerificationService.save(user.getId(), null);
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

        // Create wallet async after activation — user must be enabled first
        final Long newUserId = user.getId();
        CompletableFuture.runAsync(() -> {
            try {
                walletPort.createWalletForUser(newUserId, "NGN");
                log.info("[Register] Wallet created for userId={}", newUserId);
            } catch (Exception ex) {
                log.warn("[Register] Wallet creation failed for userId={}: {}", newUserId, ex.getMessage());
            }
        });

        // Send welcome/verification email async
        final String username = user.getUsername();
        final String email    = user.getEmail();
        if (isEmail && email != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    notificationService.publishVerificationEmail(
                            email,
                            "Welcome to ePay! Your account has been created successfully.",
                            null, username);
                } catch (Exception ex) {
                    log.warn("[Register] Welcome email failed for userId={}: {}", newUserId, ex.getMessage());
                }
            });
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
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

            if (user.isAccountLocked()) {
                return buildAuthError(authResponse,
                        "Sorry, this account is currently locked. Please contact customer service.",
                        HttpStatus.UNAUTHORIZED);
            }

            if (!user.isEnabled()) {
                return buildAuthError(authResponse,
                        "Sorry, this account is currently suspended. Please contact customer service to reactivate.",
                        HttpStatus.UNAUTHORIZED);
            }

            if (user.isTwoFactorEnabled()) {
                return handleTwoFactorAuth(user, authResponse);
            }

            String jwtToken = jwtService.generateToken(user, user.getId());
            UserTracer session = userTracerService.createSession(user);
            userAttemptService.UpdateUserAccount(user.getId());

            UserRecord rec = userRecordRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("User record not found"));

            final String loginTime = formatNow();
            final String ipAddr    = extractClientIp(httpRequest);
            final String device    = extractDevice(httpRequest);
            final String fullName  = rec.getFirstName() + " " + rec.getLastName();

            CompletableFuture.runAsync(() ->
                notificationService.publishLoginAlert(
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
            authResponse.put("referral_code", rec.getReferralCode());
            authResponse.put("date_of_birth", rec.getDateOfBirth());
            authResponse.put("country", rec.getCountry());
            authResponse.put("state", rec.getState());
            authResponse.put("city", rec.getCity());
            authResponse.put("gender", rec.getGender());
            authResponse.put("phone_number", rec.getPhoneNumber());
            authResponse.put("fullname", rec.getFirstName() + " " + rec.getLastName());
            authResponse.put("twoFactorAuthEnabled", user.isTwoFactorEnabled());

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
            walletPort.createWalletForUser(user.getId(), "NGN");
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
                notificationService.publishVerificationEmail(user.getEmail(), content, verificationLink, user.getUsername()));

        return new VerificationTokenResult(true, verificationToken);
    }

    @Override
    @Transactional
    public ResponseEntity<?> createWallet(Long id) {
        try {
            walletPort.createWalletForUser(id, "NGN");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Wallet creation failed",
                            "error", e.getMessage(),
                            "userId", id
                    ));
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

        if (identifier.isEmpty()) {
            response.put("success", false);
            response.put("message", "Identifier (email or phone) is required.");
            return ResponseEntity.badRequest().body(response);
        }


        String channel = request.getChannel() != null ? request.getChannel().name() : "EMAIL";
        User user = null;
        if ("EMAIL".equals(channel)) {
            user = userRepository.findByEmail(identifier).orElse(null);
        } else {
            // PHONE / SMS / WHATSAPP
            UserRecord rec = userRecordRepository.findByPhoneNumber(identifier).orElse(null);
            if (rec != null) user = rec.getUser();
        }


        if (user == null || !user.isEnabled()) {
            response.put("success", true);
            response.put("message", "If that account exists, a reset code has been sent.");
            return ResponseEntity.ok(response);
        }
        final String otp = keysWrapper.generateOTP();
        messagingService.sendSmSMessage(otp); 

        final User finalUser = user;
        CompletableFuture.runAsync(() ->
            notificationService.publishForgotPasswordOtp(
                finalUser.getEmail(),
                finalUser.getUsername(),
                otp
            )
        ).exceptionally(ex -> { System.err.println("[ForgotPassword] " + ex.getMessage()); return null; });

        response.put("success", true);
        response.put("message", "If that account exists, a reset code has been sent.");
        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional
    public ResponseEntity<?> confirmResetPassword(ConfirmResetPasswordRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new LinkedHashMap<>();

        String identifier  = request.getIdentifier()  != null ? request.getIdentifier().trim()  : "";
        String otp         = request.getOtp()          != null ? request.getOtp().trim()          : "";
        String newPassword = request.getNewPassword()  != null ? request.getNewPassword().trim()  : "";

        if (identifier.isEmpty() || otp.isEmpty() || newPassword.isEmpty()) {
            response.put("success", false);
            response.put("message", "Identifier, OTP and new password are all required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (newPassword.length() < 8
                || !newPassword.matches(".*[a-z].*")
                || !newPassword.matches(".*[A-Z].*")
                || !newPassword.matches(".*[0-9].*")
                || !newPassword.matches(".*[^A-Za-z0-9].*")) {
            response.put("success", false);
            response.put("message", "Password must be at least 8 characters and include uppercase, lowercase, a number and a special character.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!messagingService.verifyOTP(identifier, otp)) {
            response.put("success", false);
            response.put("message", "Invalid or expired OTP. Please request a new code.");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userRepository.findByEmail(identifier).orElse(null);
        if (user == null) {
            UserRecord rec = userRecordRepository.findByPhoneNumber(identifier).orElse(null);
            if (rec != null) user = rec.getUser();
        }

        if (user == null) {
            messagingService.invalidateOTP(identifier);
            response.put("success", false);
            response.put("message", "Account not found.");
            return ResponseEntity.badRequest().body(response);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        messagingService.invalidateOTP(identifier);

        final User finalUser = user;
        final String eventTime = formatNow();
        final String ipAddr    = extractClientIp(httpRequest);
        final String device    = extractDevice(httpRequest);
        userRecordRepository.findByUserId(user.getId()).ifPresent(rec -> {
            final String fullName = rec.getFirstName() + " " + rec.getLastName();
            CompletableFuture.runAsync(() ->
                notificationService.publishAccountSecurityAlert(
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
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.isEnabled()) {
            final User finalUser = user;
            userRecordRepository.findByUserId(user.getId()).ifPresent(rec -> {
                final String fullName = rec.getFirstName() + " " + rec.getLastName();
                CompletableFuture.runAsync(() ->
                    notificationService.publishForgotUsername(
                        finalUser.getEmail(),
                        finalUser.getUsername(),
                        fullName
                    )
                ).exceptionally(ex -> { System.err.println("[ForgotUsername] " + ex.getMessage()); return null; });
            });
        }

        response.put("success", true);
        response.put("message", "If that email address is registered, your username has been sent to it.");
        return ResponseEntity.ok(response);
    }

    private User buildUser(UserSignUpRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTwoFactorEnabled(false);
        user.setAccountLocked(false);
        user.setRole(Role.USER);
        user.setAccountType(AccountType.INDIVIDUAL);
        user.setKycTier(KycTier.TIER_1);
        user.setKycStatus(KycStatus.NOT_SUBMITTED);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setFailedLoginAttempts(0);

        if ("email".equals(request.getRegMode())) {
            user.setEmail(request.getEmail());
            user.setEnabled(false);
        } else {
            user.setEmail(null);
            user.setEnabled(true);
            user.setPhoneVerified(true);
        }
        return user;
    }

    private UserRecord buildUserRecord(UserSignUpRequest request, User user) {
        String referralCode = UUID.randomUUID().toString();
        UserRecord record = new UserRecord();
        record.setUser(user);
        record.setFirstName(request.getFirstname());
        record.setLastName(request.getLastname());
        record.setPhoneNumber(request.getPhone());
        record.setProfileComplete(false);
        record.setTotalReferrals(0);
        record.setReferralCode(referralCode);
        return record;
    }

    private void activateUserRecord(User user) {
        user.setEnabled(true);
        user.setAccountLocked(false);
        userRepository.save(user);
    }

    private ResponseEntity<?> handleTwoFactorAuth(User user, Map<String, Object> authResponse) {
        String otp = keysWrapper.generateOTP();
        String jwt = keysWrapper.generateUniqueKey();
        String baseUrl = keysWrapper.getUrl();

        TwoFactorAuthentication existing = twoFactorAuthenticationServiceImplementation.findByUser(user.getId());
        if (existing != null) {
            twoFactorAuthenticationServiceImplementation.deleteTwoFactorOtp(existing);
        }

        TwoFactorAuthentication newOtp = twoFactorAuthenticationServiceImplementation
                .createTwoFactorOtp(user, otp, jwt);

        CompletableFuture.runAsync(() ->
                notificationService.publishTwoFactorOtp(
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

    private Date calculateExpirationDate(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return calendar.getTime();
    }

    @Override
    public ResponseEntity<?> sendVerificationCode(String identifier, ContactMethod method) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (identifier.isEmpty()) {
            response.put("success", false);
            response.put("message", "Identifier (email or phone) is required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (method == ContactMethod.EMAIL) {
            if (!identifier.matches(EMAIL_REGEX)) {
                return buildAuthError(response, "Invalid email format.", HttpStatus.BAD_REQUEST);
            }

            User user = userRepository.findByEmail(identifier).orElse(null);
            if (user != null) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(response);
            }
        }

        else if (method == ContactMethod.SMS || method == ContactMethod.WHATSAPP) {
            if (phoneExists(identifier)) {
                response.put("success", false);
                response.put("message", "Phone number already exists");
                return ResponseEntity.badRequest().body(response);
            }
        }

        switch (method) {
            case SMS -> messagingService.sendSmSMessage(identifier);
            case WHATSAPP -> messagingService.sendWhatsAppMessage(identifier);
            case EMAIL -> messagingService.sendEmailMessage(identifier);
        }

        response.put("success", true);
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Verification code sent successfully.");
        response.put("contactMethod", method);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private boolean phoneExists(String phone) {
        return userRecordRepository.findByPhoneNumber(phone).isPresent();
    }

    private boolean existUsername(String username) {
        Optional<User> existingUsers = userRepository.findByUsername(username);
        return existingUsers.isPresent();
    }

    private boolean emailExists(String email) {
        Optional<User> existingUsers = userRepository.findByEmail(email);
        return existingUsers.isPresent();
    }

    @Override
    @Transactional
    public ResponseEntity<?> logoutUser(Long userId) {
        Map<String, Object> authResponse = new LinkedHashMap<>();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user == null) {
            return buildAuthError(authResponse, "Invalid user ID.", HttpStatus.BAD_REQUEST);
        }
        userTracerService.deleteByUserId(userId);
        authResponse.put("message", "User successfully logout from the system");
        authResponse.put("status", "success");
        return ResponseEntity.ok().body(authResponse);
    }
}
