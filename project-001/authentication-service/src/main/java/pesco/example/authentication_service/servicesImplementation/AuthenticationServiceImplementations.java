package pesco.example.authentication_service.servicesImplementation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; 
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.HttpServletResponse;
import pesco.example.authentication_service.clients.NotificationServiceClient;
import pesco.example.authentication_service.clients.WalletServiceClient;
import pesco.example.authentication_service.enums.AttemptType;
import pesco.example.authentication_service.enums.ContactMethod;
import pesco.example.authentication_service.enums.Role;
import pesco.example.authentication_service.enums.UserStatus;
import pesco.example.authentication_service.exceptions.Error;
import pesco.example.authentication_service.exceptions.UserNotFoundException;
import pesco.example.authentication_service.models.AuthorizeUserVerification;
import pesco.example.authentication_service.models.TwoFactorAuthentication;
import pesco.example.authentication_service.models.UserRecord;
import pesco.example.authentication_service.models.UserTracer;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.models.VerificationToken;
import pesco.example.authentication_service.payloads.UserSignInRequest;
import pesco.example.authentication_service.payloads.UserSignUpRequest;
import pesco.example.authentication_service.repositories.AuthorizeUserVerificationRepository;
import pesco.example.authentication_service.repositories.UserRecordRepository;
import pesco.example.authentication_service.repositories.UsersRepository;
import pesco.example.authentication_service.repositories.VerificationTokenRepository;
import pesco.example.authentication_service.responses.AuthResponse;
import pesco.example.authentication_service.responses.VerificationTokenResult;
import pesco.example.authentication_service.services.AuthenticationService;
import pesco.example.authentication_service.services.AuthorizeUserVerificationService;
import pesco.example.authentication_service.services.JwtService;
import pesco.example.authentication_service.services.UserAttemptService;
import pesco.example.authentication_service.services.UserTracerService;
import pesco.example.authentication_service.utils.KeyWrapper;

@Service
@Transactional
public class AuthenticationServiceImplementations implements AuthenticationService {

    private static final int EXPIRATION_MINUTES = 10;
    private Date expirationTime;
    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationServiceClient notificationServiceClient;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final KeyWrapper keysWrapper;
    private final AuthorizeUserVerificationService authorizeUserVerificationService;
    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;
    private final TwoFactorAuthenticationServiceImplementations twoFactorAuthenticationServiceImplementation;
    private final WalletServiceClient walletServiceClient;
    private final UserTracerService userTracerService;
    private final UserAttemptService userAttemptService;
    private final MessagingService messagingService;

    public AuthenticationServiceImplementations(UsersRepository userRepository,
            UserRecordRepository userRecordRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            NotificationServiceClient notificationServiceClient,
            VerificationTokenRepository verificationTokenRepository,
            AuthenticationManager authenticationManager,
            KeyWrapper keysWrapper,
            AuthorizeUserVerificationService authorizeUserVerificationService,
            AuthorizeUserVerificationRepository authorizeUserVerificationRepository,
            TwoFactorAuthenticationServiceImplementations twoFactorAuthenticationServiceImplementation,
            WalletServiceClient walletServiceClient,
            UserTracerService userTracerService,
            UserAttemptService userAttemptService,
            MessagingService messagingService) {
        this.userRepository = userRepository;
        this.userRecordRepository = userRecordRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationServiceClient = notificationServiceClient;
        this.verificationTokenRepository = verificationTokenRepository;
        this.authenticationManager = authenticationManager;
        this.keysWrapper = keysWrapper;
        this.authorizeUserVerificationService = authorizeUserVerificationService;
        this.authorizeUserVerificationRepository = authorizeUserVerificationRepository;
        this.twoFactorAuthenticationServiceImplementation = twoFactorAuthenticationServiceImplementation;
        this.walletServiceClient = walletServiceClient;
        this.userTracerService = userTracerService;
        this.userAttemptService = userAttemptService;
        this.messagingService = messagingService;
    }

    @Transactional
    public ResponseEntity<?> createAccount(UserSignUpRequest request) {
        String identifier = "email".equals(request.getRegMode()) 
            ? request.getEmail() 
            : request.getPhone();

        System.out.println("=== OTP Verification Debug ===");
        System.out.println("Identifier: " + identifier);
        System.out.println("Verification Code from request: " + request.getVerificationCode());
        System.out.println("OTP Store Size: " + MessagingService.getOTPStoreSize());
        System.out.println("Is OTP Valid: " + MessagingService.isOTPValid(identifier));
        System.out.println("Remaining Time: " + MessagingService.getRemainingTime(identifier) + " minutes");
        
        if (!MessagingService.verifyOTP(identifier, request.getVerificationCode())) {
            System.out.println("OTP Verification FAILED");
            return Error.createResponse("Invalid or expired verification code.*", 
                HttpStatus.BAD_REQUEST, "The verification code you entered is invalid or has expired.");
        }
        
        System.out.println("OTP Verification SUCCESS");
        Long nextUserId = getNextUserId();
        Users user = new Users();
        user.setId(nextUserId);
        user.setUsername(request.getUsername());
        user.setTwoFactorAuth(false);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        if ("email".equals(request.getRegMode())) {
            user.setEmail(request.getEmail());
            user.setEnabled(false); 
        } else {
            user.setEmail(null);
            user.setEnabled(true); 
        }

        userRepository.save(user);

        Users savedUser = userRepository.findById(nextUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        String referralCode = UUID.randomUUID().toString();

        UserRecord userRecord = new UserRecord();
        userRecord.setUser(savedUser);
        
        if ("phone".equals(request.getRegMode())) {
            userRecord.setTelephone(request.getPhone());
        }
        
        userRecord.setFirstName(request.getFirstname());
        userRecord.setLastName(request.getLastname());
        userRecord.setTransferPinSet(false);
        userRecord.setLocked(false);
        userRecord.setLockedAt(null);
        userRecord.setReferralCode(referralCode);
        userRecord.setBlocked(false);
        userRecord.setProfileComplete(false);
        userRecord.setTotalReferers(null);
        userRecord.setReferralUsername("n13_" + request.getUsername());
        userRecord.setReferralLink(keysWrapper.getUrl() + "/auth/register?referral_code=" + referralCode);
        
        if ("email".equals(request.getRegMode())) {
            userRecord.setStatus(UserStatus.PENDING_VERIFICATION);
        } else {
            userRecord.setStatus(UserStatus.ACTIVE);
        }

        userRecordRepository.save(userRecord);
        
        // UUID verificationToken = UUID.randomUUID();
        // expirationTime = calculateExpirationDate(EXPIRATION_MINUTES);
        // VerificationToken tokenEntity = new VerificationToken();
        // tokenEntity.setUserId(nextUserId);
        // tokenEntity.setToken(String.valueOf(verificationToken));
        // tokenEntity.setExpirationTime(expirationTime);

        // verificationTokenRepository.save(tokenEntity);
        String message = "Thanks for sigining up for ePay! Your account has been created succcessfully.";
        if ("email".equals(request.getRegMode())) {
            Long unverifiedUserId = KeyWrapper.generateUniqueAuthorizeUserId();
            authorizeUserVerificationService.save(nextUserId, unverifiedUserId);
            CompletableFuture<Void> walletCreationFuture = CompletableFuture
                .runAsync(() -> {
                    walletServiceClient.createUserWallet(user.getId());
                });
    
            walletCreationFuture.join();
            Optional<UserRecord> optionalRecord = userRecordRepository.findByUserId(user.getId());
            optionalRecord.ifPresent(record -> {
                record.setStatus(UserStatus.ACTIVE);
                record.setLocked(false);
                record.setBlocked(false);
                userRecordRepository.save(record);

                user.setEnabled(true);
                userRepository.save(user);
            });
            // String verificationLink = keysWrapper.getUrl() + "/auth/verifyRegistration?token=" 
            //     + verificationToken + "&id=" + unverifiedUserId;
            // String content = "Dear " + request.getUsername() + ",\n\n"
            //         + "Thank you for signing up for pesco! We're excited to have you on board.\n\n"
            //         + "Please verify your email address to complete your registration and activate your account.";

            // CompletableFuture<Void> sendVerificationMessage = CompletableFuture.runAsync(() -> 
            //     notificationServiceClient.sendVerificationEmail(request.getEmail(), content, 
            //         verificationLink, request.getUsername()));
            // sendVerificationMessage.join();
            return Error.createResponse("success", HttpStatus.CREATED, message);
        } else {
            // Phone registration - account is already verified via OTP
            // Create wallet asynchronously
            CompletableFuture<Void> walletCreationFuture = CompletableFuture
                .runAsync(() -> {
                    walletServiceClient.createUserWallet(user.getId());
                });
    
            walletCreationFuture.join();
            
            // Update user record status - use different variable name in lambda
            Optional<UserRecord> optionalRecord = userRecordRepository.findByUserId(user.getId());
            optionalRecord.ifPresent(record -> {
                record.setStatus(UserStatus.ACTIVE);
                record.setLocked(false);
                record.setBlocked(false);
                userRecordRepository.save(record);
            });

            user.setEnabled(true);
            userRepository.save(user);
            // Invalidate the OTP after successful registration
            MessagingService.invalidateOTP(identifier);
            
            // Send welcome message via the chosen method
            ContactMethod method = "WHATSAPP".equals(request.getVerificationMethod()) 
                ? ContactMethod.WHATSAPP 
                : ContactMethod.SMS;
            
            CompletableFuture.runAsync(() -> 
                messagingService.sendWelcomeMessage(request.getPhone(), request.getUsername(), method));
            return Error.createResponse("success", HttpStatus.CREATED, message);
        }
    }

    @Override
    public ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response) {
        Map<String, Object> Authresponse = new HashMap<>();

        Optional<Users> userInfo = userRepository.findByUsername(request.getUsername());
        if (userInfo.isEmpty()) {
            Authresponse.put("status", HttpStatus.BAD_REQUEST.value());
            Authresponse.put("success", false);
            Authresponse.put("message", "Invalid user credentials.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Authresponse);
        }

        Users user = userInfo.get();

        if (!user.isEnabled()) {
            Authresponse.put("status", HttpStatus.UNAUTHORIZED.value());
            Authresponse.put("success", false);
            Authresponse.put("message", "This account has not been verified.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
        }

        if (userTracerService.hasActiveSession(user.getId())) {
            Authresponse.put("status", HttpStatus.UNAUTHORIZED.value());
            Authresponse.put("success", false);
            Authresponse.put("message", "This account is already logged in on another device.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
        }

        userAttemptService.createFailAttempt(user.getId(), AttemptType.LOGIN);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            Optional<UserRecord> checkAccountStatus = userRecordRepository.findByUserId(user.getId());
            if (checkAccountStatus.isPresent()) {
                UserRecord recordStatus = checkAccountStatus.get();
                if (recordStatus.isLocked()) {
                    Authresponse.put("status", HttpStatus.UNAUTHORIZED.value());
                    Authresponse.put("success", false);
                    Authresponse.put("message", "Sorry, this account is currently locked. Please contact customer service.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
                } else if (recordStatus.isBlocked()) {
                    Authresponse.put("status", HttpStatus.UNAUTHORIZED.value());
                    Authresponse.put("success", false);
                    Authresponse.put("message", "Sorry, this account is currently blocked. Please contact customer service.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
                }
            }

            String jwtToken = jwtService.generateToken(user, user.getId());

            UserTracer session = userTracerService.createSession(user);

            boolean isTwoFactorAuthEnabled = user.isTwoFactorAuth();
            if (isTwoFactorAuthEnabled) {
                Authresponse.put("message", "Two-factor authentication is enabled.");
                Authresponse.put("twoFactorAuthEnabled", true);
                Authresponse.put("status", HttpStatus.OK.value());

                String otp = keysWrapper.generateOTP();
                String jwt = keysWrapper.generateUniqueKey();

                TwoFactorAuthentication existingOtp = twoFactorAuthenticationServiceImplementation
                        .findByUser(user.getId());
                if (existingOtp != null) {
                    twoFactorAuthenticationServiceImplementation.deleteTwoFactorOtp(existingOtp);
                }

                TwoFactorAuthentication newOtp = twoFactorAuthenticationServiceImplementation
                        .createTwoFactorOtp(user, otp, jwt);
                Authresponse.put("otpId", newOtp.getId().toString());
                Authresponse.put("jwt", newOtp.getToken());

                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                CompletableFuture.runAsync(() -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        notificationServiceClient.sendOptEmail(
                                user.getEmail(), otp,
                                keysWrapper.getUrl() + "/auth/security/password",
                                keysWrapper.getUrl() + "/auth/security/configuring-two-factor-authentication",
                                keysWrapper.getUrl() + "/auth/security/configuring-two-factor-authentication-recovery-methods");
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });

                return ResponseEntity.ok(Authresponse);
            }

            Optional<UserRecord> record = userRecordRepository.findByUserId(user.getId());

            userAttemptService.UpdateUserAccount(user.getId());
            
            Authresponse.put("jwt", jwtToken);
            Authresponse.put("email", user.getEmail());
            Authresponse.put("userId", user.getId());
            Authresponse.put("status", HttpStatus.OK.value());
            Authresponse.put("is_verify", user.isEnabled());
            Authresponse.put("is_profile_complete", user.getRecords().get(0).isProfileComplete());
            Authresponse.put("referral_username", record.get().getReferralUsername());
            Authresponse.put("date_of_birth", record.get().getDateofBirth());
            Authresponse.put("referral_link", record.get().getReferralLink());
            Authresponse.put("country", user.getRecords().get(0).getCountry());
            Authresponse.put("state", user.getRecords().get(0).getState());
            Authresponse.put("city", user.getRecords().get(0).getCity());
            Authresponse.put("gender", user.getRecords().get(0).getGender());
            Authresponse.put("telephone", user.getRecords().get(0).getTelephone());
            Authresponse.put("success", true);
            Authresponse.put("session", true);
            Authresponse.put("sessionId", session.getSessionId());
            Authresponse.put("twoFactorAuthEnabled", user.isTwoFactorAuth());
            Authresponse.put("username", user.getUsername());
            Authresponse.put("fullname", user.getRecords().get(0).getFirstName() + " " + user.getRecords().get(0).getLastName());

             return ResponseEntity.ok()
                .header("X-Session-ID", session.getSessionId())
                .header("X-Session-Expires", session.getExpiresAt().toString())
                .body(Authresponse);

        } catch (BadCredentialsException e) {
            Authresponse.put("status", HttpStatus.BAD_REQUEST.value());
            Authresponse.put("success", false);
            Authresponse.put("message", "Invalid user credentials.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Authresponse);
        } catch (AuthenticationException e) {
            Authresponse.put("status", HttpStatus.BAD_REQUEST.value());
            Authresponse.put("success", false);
            Authresponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Authresponse);
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

    @Override
    public VerificationTokenResult generateVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        if (verificationToken == null) {
            return new VerificationTokenResult(false, "Token not found");
        }
        Date newExpirationTime = calculateExpirationDate(EXPIRATION_MINUTES);

        Optional<Users> user = userRepository.findById(verificationToken.getUserId());
        verificationToken.setExpirationTime(newExpirationTime);

        String newToken = UUID.randomUUID().toString();
        verificationToken.setToken(newToken);
        verificationTokenRepository.save(verificationToken);

        Optional<AuthorizeUserVerification> optionAuthUser = authorizeUserVerificationRepository
                .findUserByIdOptional(user.get().getId());

        String verificationLink = keysWrapper.getUrl() + "/auth/verifyRegistration?token=" + newToken + "&id="
                + optionAuthUser.get().getId();

        String content = "Dear " + user.get().getUsername() + ",\n\n"
                + "Thank you for registering with Pesco! We're thrilled to have you join us.\n\n"
                + "To complete your registration and activate your account";

        CompletableFuture<Void>sendVerificationLinkMessage = CompletableFuture.runAsync(() -> notificationServiceClient.sendVerificationEmail(user.get().getEmail(), content, verificationLink, user.get().getUsername()));
        sendVerificationLinkMessage.join();
        
        return new VerificationTokenResult(true, verificationToken);
    }

    private Date calculateExpirationDate(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return new Date(calendar.getTime().getTime());
    }

    @Override
    @Transactional
    public ResponseEntity<?> verifyUser(String token, Long id) {
        AuthResponse verifyResponse = new AuthResponse();
        boolean checkVerifyUser = authorizeUserVerificationRepository.findUserById(id);
        if (checkVerifyUser) {
            verifyResponse.setMessage("This account has already been verified.");
            verifyResponse.setStatus(true);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        }

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            verifyResponse.setMessage("Invalid verification token");
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.BAD_REQUEST);
        }

        Optional<Users> optionalUser = userRepository.findById(verificationToken.getUserId());
        if (optionalUser.isEmpty()) {
            verifyResponse.setMessage("User not found.");
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.BAD_REQUEST);
        }

        Users user = optionalUser.get();
        if (user.isEnabled()) {
            verifyResponse.setMessage("Hi " + user.getUsername() + ", Your account has already been verified.");
            verifyResponse.setStatus(true);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        }

        Calendar cal = Calendar.getInstance();
        if (verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() < 0) {
            verifyResponse.setMessage("Verification token has expired. Click the resend button to get a new token.");
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.CONFLICT);
        }

        CompletableFuture<Void> walletCreationFuture = CompletableFuture
                .runAsync(() -> {
                    walletServiceClient.createUserWallet(user.getId());
                });
        try {
            walletCreationFuture.join();
            Optional<UserRecord> optionalRecord = userRecordRepository.findByUserId(user.getId());
            optionalRecord.ifPresent(userRecord -> {
                userRecord.setStatus(UserStatus.ACTIVE);
                userRecord.setLocked(false);
                userRecord.setBlocked(false);
                userRecordRepository.save(userRecord);
            });

            user.setEnabled(true);
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);

            verifyResponse.setMessage("User registration verified successfully.");
            verifyResponse.setStatus(true);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);

        } catch (Exception e) {
            verifyResponse.setMessage(
                    "Failed to create wallet for user. Skipping user verification. Error: " + e.getMessage());
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   private Long getNextUserId() {
        List<Users> existingUsers = userRepository.findAll();
        Users newUser = new Users();
        if (existingUsers.isEmpty()) {
            newUser.setId(1001L);
        } else {
            Long maxId = existingUsers.stream()
                    .map(Users::getId)
                    .max(Long::compare)
                    .orElse(0L);
            newUser.setId(maxId + 1);
        }
        return newUser.getId();
    }

    @Override
    public ResponseEntity<?> createWallet(Long id) {

       CompletableFuture<Void> walletCreationFuture = CompletableFuture
                .runAsync(() -> {
                    walletServiceClient.createUserWallet(id);
                });
        walletCreationFuture.join();
            
        return ResponseEntity.status(HttpStatus.CREATED).body("Wallet created."); 
    }

}