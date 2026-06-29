package com.example.auth_user_service.services;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional; 
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
import com.example.auth_user_service.enums.Role;
import com.example.auth_user_service.enums.UserStatus;
import com.example.auth_user_service.httpClients.NotificationServiceClient;
import com.example.auth_user_service.interfaces.IAuthenticationService;
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
                notificationServiceClient.sendAccountSecurityAlert(
                    user.getEmail(), fullName, user.getUsername(),
                    "LOGIN_ALERT", loginTime,
                    ipAddr, device,
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
                        baseUrl + "/auth/security/password",           // use pre-resolved URL
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
