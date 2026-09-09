package com.epay.auth.controller;

import com.epay.auth.interfaces.IAuthenticationService;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.input.ConfirmResetPasswordRequest;
import com.epay.domain.auth.input.ForgotPasswordRequest;
import com.epay.domain.auth.input.ForgotUsernameRequest;
import com.epay.domain.auth.input.UserSignInRequest;
import com.epay.domain.auth.input.UserSignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User registration, login, verification, and password recovery")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authService;

    @Operation(
        summary     = "Register a new user account",
        description = "Creates a new USER account and sends an email verification code."
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserSignUpRequest request) {
        return authService.createAccount(request);
    }

    @Operation(
        summary     = "Authenticate and receive JWT tokens",
        description = "Validates credentials and returns an access token plus refresh token in an HttpOnly cookie."
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserSignInRequest request,
                                    HttpServletResponse response,
                                    HttpServletRequest httpRequest) {
        return authService.login(request, response, httpRequest);
    }

    @Operation(
        summary     = "Send an account verification code",
        description = "Sends a verification code to the supplied identifier (email or phone) via the chosen contact method."
    )
    @PostMapping("/send-verify-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String identifier,
                                                   @RequestParam ContactMethod method) {
        return authService.sendVerificationCode(identifier, method);
    }

    @Operation(
        summary     = "Verify an account via token",
        description = "Activates a user account using the token sent in the verification email."
    )
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam String token,
                                            @RequestParam Long id) {
        return authService.verifyUser(token, id);
    }

    @Operation(
        summary     = "Re-send the email verification link",
        description = "Generates a new verification token and re-sends the activation email."
    )
    @GetMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String token) {
        var result = authService.generateVerificationToken(token);
        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @Operation(
        summary     = "Initiate a password reset",
        description = "Sends a one-time password-reset link to the registered email address."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @Operation(
        summary     = "Confirm and apply a new password",
        description = "Validates the reset token and updates the user's password."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request,
                                            HttpServletRequest httpRequest) {
        return authService.confirmResetPassword(request, httpRequest);
    }

    @Operation(
        summary     = "Recover a forgotten username",
        description = "Sends the user's username to their registered email address."
    )
    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(@Valid @RequestBody ForgotUsernameRequest request) {
        return authService.forgotUsername(request);
    }

    @Operation(
        summary     = "Log out the current user",
        description = "Invalidates the current session and clears authentication cookies."
    )
    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam(name = "userId", required = false) Long userId) {
        return authService.logoutUser(userId);
    }
}
