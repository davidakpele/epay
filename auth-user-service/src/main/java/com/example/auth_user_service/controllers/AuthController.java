package com.example.auth_user_service.controllers;

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
import com.example.auth_user_service.exceptions.Error;
import com.example.auth_user_service.enums.ContactMethod;
import com.example.auth_user_service.interfaces.IAuthenticationService;
import com.example.auth_user_service.interfaces.IMessagingService;
import com.example.auth_user_service.interfaces.ITwoFactorAuthenticationService;
import com.example.auth_user_service.interfaces.IUserRecordService;
import com.example.auth_user_service.interfaces.IUserTracerService;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.ConfirmResetPasswordRequest;
import com.example.auth_user_service.payloads.ForgotPasswordRequest;
import com.example.auth_user_service.payloads.ForgotUsernameRequest;
import com.example.auth_user_service.payloads.OTPRequest;
import com.example.auth_user_service.payloads.UserSignInRequest;
import com.example.auth_user_service.payloads.UserSignUpRequest;
import com.example.auth_user_service.responses.AuthResponse;
import com.example.auth_user_service.responses.VerificationTokenResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    private final IAuthenticationService authenticationService; 
    private final ITwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation;
    private final IUserTracerService userTracerService;
    private final IMessagingService messagingService;
    private final IUserRecordService userRecordService;

    public AuthController(IAuthenticationService authenticationService, ITwoFactorAuthenticationService twoFactorAuthenticationServiceImplementation, IUserTracerService userTracerService, IMessagingService messagingService, IUserRecordService userRecordService) {
        this.authenticationService = authenticationService;
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

        if (request.getRegMode() == null || 
            (!request.getRegMode().equals("email") && !request.getRegMode().equals("phone"))) {
            return Error.createResponse("Invalid registration mode.*", HttpStatus.BAD_REQUEST,
                    "Registration mode must be either 'email' or 'phone'");
        }

        if (request.getRegMode().equals("email")) {
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
            HttpServletResponse response,
            HttpServletRequest httpRequest) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return Error.createResponse("Username is require.*", HttpStatus.BAD_REQUEST, "Username can not be empty");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Error.createResponse("Password is require*", HttpStatus.BAD_REQUEST, "Password can not be empty");
        }
        return authenticationService.login(request, response, httpRequest);
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

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestParam(name = "userId", required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();

        if (userId == null || userId <= 0) {
            response.put("status", "error");
            response.put("message", "User ID is required and must be a valid positive number");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        userTracerService.deleteByUserId(userId);

        response.put("status", "success");
        response.put("message", "Logged out successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);    
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        if (request.getIdentifier() == null || request.getIdentifier().trim().isEmpty()) {
            return Error.createResponse("Email or phone number is required.",
                    HttpStatus.BAD_REQUEST, "Identifier cannot be empty.");
        }

        String method = request.getMethod() != null ? request.getMethod().trim().toUpperCase() : "";
        if (!"EMAIL".equals(method) && !"PHONE".equals(method)) {
            return Error.createResponse("Method must be EMAIL or PHONE.",
                    HttpStatus.BAD_REQUEST, "Invalid method value.");
        }

        if ("EMAIL".equals(method) && !request.getIdentifier().matches(EMAIL_REGEX)) {
            return Error.createResponse("Invalid email address format.",
                    HttpStatus.BAD_REQUEST, "Please provide a valid email address.");
        }

        return authenticationService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ConfirmResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        if (request.getIdentifier() == null || request.getIdentifier().trim().isEmpty()) {
            return Error.createResponse("Identifier is required.",
                    HttpStatus.BAD_REQUEST, "Provide the email or phone you used to request the OTP.");
        }
        if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
            return Error.createResponse("OTP is required.",
                    HttpStatus.BAD_REQUEST, "Enter the 4-digit code sent to you.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return Error.createResponse("New password is required.",
                    HttpStatus.BAD_REQUEST, "Password cannot be empty.");
        }

        return authenticationService.confirmResetPassword(request, httpRequest);
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(
            @RequestBody ForgotUsernameRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return Error.createResponse("Email address is required.",
                    HttpStatus.BAD_REQUEST, "Provide your registered email address.");
        }
        if (!request.getEmail().trim().matches(EMAIL_REGEX)) {
            return Error.createResponse("Invalid email address format.",
                    HttpStatus.BAD_REQUEST, "Please provide a valid email address.");
        }

        return authenticationService.forgotUsername(request);
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