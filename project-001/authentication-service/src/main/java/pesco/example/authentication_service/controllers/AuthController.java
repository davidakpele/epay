package pesco.example.authentication_service.controllers;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletResponse;
import pesco.example.authentication_service.services.AuthenticationService;
import pesco.example.authentication_service.services.PasswordResetTokenService;
import pesco.example.authentication_service.services.TwoFactorAuthenticationService;
import pesco.example.authentication_service.services.UserRecordService;
import pesco.example.authentication_service.services.UserService;
import pesco.example.authentication_service.services.UserTracerService;
import pesco.example.authentication_service.servicesImplementation.MessagingService;
import pesco.example.authentication_service.enums.ContactMethod;
import pesco.example.authentication_service.exceptions.Error;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.payloads.ChangePasswordRequest;
import pesco.example.authentication_service.payloads.OTPRequest;
import pesco.example.authentication_service.payloads.UserSignInRequest;
import pesco.example.authentication_service.payloads.UserSignUpRequest;
import pesco.example.authentication_service.responses.AuthResponse;
import pesco.example.authentication_service.responses.VerificationTokenResult;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    private final UserService userServiceImplementation;
    private final PasswordResetTokenService passwordResetTokenServiceImplementation;
    private final TwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation;
    private final UserTracerService userTracerService;
    private final MessagingService messagingService;
    private final UserRecordService userRecordService;
  
    public AuthController(AuthenticationService authenticationService, UserService userServiceImplementation, PasswordResetTokenService passwordResetTokenServiceImplementation, TwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation, UserTracerService userTracerService, MessagingService messagingService, UserRecordService userRecordService) {
        this.authenticationService = authenticationService;
        this.userServiceImplementation = userServiceImplementation;
        this.passwordResetTokenServiceImplementation = passwordResetTokenServiceImplementation;
        this.twoFactorAuthenticationServiceImplementation = twoFactorAuthenticationServiceImplementation;
        this.userTracerService = userTracerService;
        this.messagingService = messagingService;
        this.userRecordService = userRecordService;
    }

    @PostMapping("/create/wallet/{id}")
    public ResponseEntity<?> createWallet(@PathVariable Long id) {
         return authenticationService.createWallet(id);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserSignUpRequest request) {
        
        if (request.getFirstname() == null || request.getFirstname().trim().isEmpty()) {
            return Error.createResponse("Firstname is required.*", HttpStatus.BAD_REQUEST,
                    "Firstname cannot be empty");
        }

        if (request.getLastname() == null || request.getLastname().trim().isEmpty()) {
            return Error.createResponse("Lastname is required.*", HttpStatus.BAD_REQUEST,
                    "Lastname cannot be empty");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Error.createResponse("Username is required.*", HttpStatus.BAD_REQUEST,
                    "Username cannot be empty");
        }

        if (existUsername(request.getUsername())) {
            return Error.createResponse("Sorry..! Username already been chosen by another user.*",
                    HttpStatus.BAD_REQUEST, "This Username has been used.");
        }

        // Validate registration mode
        if (request.getRegMode() == null || 
            (!request.getRegMode().equals("email") && !request.getRegMode().equals("phone"))) {
            return Error.createResponse("Invalid registration mode.*", HttpStatus.BAD_REQUEST,
                    "Registration mode must be either 'email' or 'phone'");
        }

        // Validate based on registration mode
        if (request.getRegMode().equals("email")) {
            // Email validation
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Error.createResponse("Email is required.*", HttpStatus.BAD_REQUEST,
                        "Email cannot be empty");
            }

            if (!request.getEmail().matches(EMAIL_REGEX)) {
                return Error.createResponse("Invalid email format*", HttpStatus.BAD_REQUEST, 
                        "Invalid email address");
            }

            if (emailExists(request.getEmail())) {
                return Error.createResponse("Sorry..! Email already been used by another user.*", 
                        HttpStatus.BAD_REQUEST, "This email has been used.");
            }

            if (!"EMAIL".equals(request.getVerificationMethod())) {
                return Error.createResponse("Invalid verification method for email registration.*", 
                        HttpStatus.BAD_REQUEST, "Verification method must be EMAIL");
            }
        } else {
            // Phone validation
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                return Error.createResponse("Phone number is required.*", HttpStatus.BAD_REQUEST,
                        "Phone number cannot be empty");
            }

            if (phoneExists(request.getPhone())) {
                return Error.createResponse("Sorry..! Phone number already been used by another user.*", 
                        HttpStatus.BAD_REQUEST, "This phone number has been used.");
            }

            if (!"SMS".equals(request.getVerificationMethod()) && 
                !"WHATSAPP".equals(request.getVerificationMethod())) {
                return Error.createResponse("Invalid verification method for phone registration.*", 
                        HttpStatus.BAD_REQUEST, "Verification method must be SMS or WHATSAPP");
            }
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Error.createResponse("Password is required.*", HttpStatus.BAD_REQUEST, 
                    "Password cannot be empty");
        }

        if (request.getPassword().length() < 8) {
            return Error.createResponse("Password is too short.*", HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters");
        }

        if (!request.getPassword().matches(".*[a-z].*")) {
            return Error.createResponse("Password must contain lowercase letter.*", HttpStatus.BAD_REQUEST,
                    "Password must contain a lowercase letter");
        }

        if (!request.getPassword().matches(".*[A-Z].*")) {
            return Error.createResponse("Password must contain uppercase letter.*", HttpStatus.BAD_REQUEST,
                    "Password must contain an uppercase letter");
        }

        if (!request.getPassword().matches(".*[0-9].*")) {
            return Error.createResponse("Password must contain a number.*", HttpStatus.BAD_REQUEST,
                    "Password must contain a number");
        }

        if (!request.getPassword().matches(".*[^A-Za-z0-9].*")) {
            return Error.createResponse("Password must contain special character.*", HttpStatus.BAD_REQUEST,
                    "Password must contain a special character");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Error.createResponse("Passwords do not match.*", HttpStatus.BAD_REQUEST,
                    "Passwords do not match");
        }

        if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
            return Error.createResponse("Verification code is required.*", HttpStatus.BAD_REQUEST,
                    "Verification code cannot be empty");
        }

        return authenticationService.createAccount(request);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserSignInRequest request,
            HttpServletResponse response) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return Error.createResponse("Username is require.*", HttpStatus.BAD_REQUEST, "Username can not be empty");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Error.createResponse("Password is require*", HttpStatus.BAD_REQUEST, "Password can not be empty");
        }
        return authenticationService.login(request, response);
    }

    @GetMapping("/verifyRegistration")
    public ModelAndView verifyRegistration(@RequestParam("token") String token, @RequestParam("id") Long id) {
        if (token == null || token.isEmpty() || id == null) {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("message", "Missing 'token' or 'id' parameter.");
            modelAndView.setStatus(HttpStatus.BAD_REQUEST);
            return modelAndView;
        }

        ResponseEntity<?> response = authenticationService.verifyUser(token, id);
        AuthResponse authResponse = (AuthResponse) response.getBody();

        ModelAndView modelAndView;
        if (response.getStatusCode() == HttpStatus.OK) {
            modelAndView = new ModelAndView("success");
            modelAndView.addObject("message", "This account has been verified.");
        } else {
            if (response.getStatusCode() == HttpStatus.CONFLICT) {
                modelAndView = new ModelAndView("error");
                modelAndView.addObject("message", "Verification token has expired.");
                modelAndView.addObject("showResendButton", true);
                modelAndView.addObject("token", token);
                modelAndView.setStatus(HttpStatus.BAD_REQUEST);
                return modelAndView;
            }

            modelAndView = new ModelAndView("error");
            modelAndView.addObject("message",
                    authResponse != null ? authResponse.getMessage() : "An unexpected error occurred.");
            modelAndView.setStatus(response.getStatusCode());
        }

        return modelAndView;
    }

    @SuppressWarnings("unlikely-arg-type")
    @GetMapping("/resendVerifyToken")
    public ResponseEntity<?> resendVerificationToken(@RequestParam("token") String oldToken) {
        VerificationTokenResult response = authenticationService.generateVerificationToken(oldToken);

        if (response.equals("failure")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody UserSignUpRequest request) {
        if (request.getEmail().isEmpty()) {
            return Error.createResponse("Email address is require*", HttpStatus.BAD_REQUEST,
                    "Provide your email address.");
        } else if (request.getEmail() == null) {
            return Error.createResponse("Your need to provide email address", HttpStatus.BAD_REQUEST,
                    "Invalid request sent.");
        }
        return userServiceImplementation.forgetPassword(request.getEmail());
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> showResetPasswordPage(@RequestParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return Error.createResponse("Valid token parameter is require.", HttpStatus.BAD_REQUEST,
                    "Provide token parameter to validate this endpoint.");
        } else {
            return passwordResetTokenServiceImplementation.findByToken(token);
        }
    }

    @PostMapping("/create-new-password")
    public ResponseEntity<?> createNewPassword(@RequestBody ChangePasswordRequest request) {
        if (request.getPassword().isEmpty()) {
            return Error.createResponse("Password is require*", HttpStatus.BAD_REQUEST,
                    "Password can not be empty");
        }
        if (request.getConfirmPassword().isEmpty()) {
            return Error.createResponse("Confirm Password is require*", HttpStatus.BAD_REQUEST,
                    "Confirm Password can not be empty");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Error.createResponse("Passwords do not match*", HttpStatus.BAD_REQUEST,
                    "Password and Confirm Password must be the same");
        }

        boolean isUpdated = passwordResetTokenServiceImplementation.updatePassword(request);

        if (isUpdated) {
            return new ResponseEntity<>("Password successfully updated.", HttpStatus.CREATED);
        } else {
            return Error.createResponse("Invalid or expired token", HttpStatus.BAD_REQUEST,
                    "The token is invalid or has expired");
        }
    }

    @GetMapping("/verify-otp-token")
    public ResponseEntity<?> verifyOtpToken(@RequestParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return Error.createResponse("Valid token parameter is require.", HttpStatus.BAD_REQUEST,
                    "Provide token parameter to validate this endpoint.");
        } else {
            return twoFactorAuthenticationServiceImplementation.findByToken(token);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyUserOtp(@RequestBody OTPRequest reqOtpPayload) {
        if (reqOtpPayload.getOtp() == null || reqOtpPayload.getOtp().isEmpty()) {
            return Error.createResponse("OTP require*.", HttpStatus.BAD_REQUEST,
                    "Provide the OTP code sent to your email address");
        } else {
            return twoFactorAuthenticationServiceImplementation.verifyUserTwoFactorOtp(reqOtpPayload);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestParam(name = "sessionId", required = false) String sessionId) {

        Map<String, Object> response = new HashMap<>();

        if (sessionId == null || sessionId.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Session ID is required for logout");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        userTracerService.deleteSession(sessionId);

        response.put("status", "success");
        response.put("message", "Logged out successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cracked-logout")
    public ResponseEntity<String> logoutUserId(
            @RequestParam(name = "userId", required = true) Long userId,
            HttpServletResponse response) {
            userTracerService.deleteByUserId(userId);
        return ResponseEntity.ok("Logged out successfully");
    }

    public static String FormatBigDecimal(BigDecimal amount) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(amount);
    }

    private boolean phoneExists(String phone) {
        return userRecordService.findByTelephone(phone).isPresent();
    }

    @PostMapping("/send-verify-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestParam String identifier, @RequestParam ContactMethod method) {
        if (method == ContactMethod.EMAIL) {
            if (!identifier.matches(EMAIL_REGEX)) {
                return buildError("Invalid email format", HttpStatus.BAD_REQUEST);
            }
            if (emailExists(identifier)) {
                return buildError("Email already exists", HttpStatus.CONFLICT);
            }
        }
        else if (method == ContactMethod.SMS || method == ContactMethod.WHATSAPP) {
            if (phoneExists(identifier)) {
                return buildError("Phone number already exists", HttpStatus.CONFLICT);
            }
        }
        switch (method) {
            case SMS -> messagingService.sendSmSMessage(identifier);
            case WHATSAPP -> messagingService.sendWhatsAppMessage(identifier);
            case EMAIL -> messagingService.sendEmailMessage(identifier);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Verification code sent successfully.");
        response.put("contactMethod", method);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/unlock-account/{id}")
    public ResponseEntity<?> unlockUserAccountById(@PathVariable Long id) {
        return userRecordService.unlockedAccount(id);
    }
     
    private boolean emailExists(String email) {
        Optional<Users> existingUsers = authenticationService.findByEmail(email);
        return existingUsers.isPresent();
    }

    private boolean existUsername(String username) {
        Optional<Users> existingUsers = authenticationService.findByUsername(username);
        return existingUsers.isPresent();
    }

    private ResponseEntity<Map<String,Object>> buildError(String message, HttpStatus status) {
        Map<String,Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", status.value());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

}