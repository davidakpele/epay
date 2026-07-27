package com.epay.auth.controller;

import com.epay.auth.interfaces.IAuthenticationService;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.input.ConfirmResetPasswordRequest;
import com.epay.domain.auth.input.ForgotPasswordRequest;
import com.epay.domain.auth.input.ForgotUsernameRequest;
import com.epay.domain.auth.input.UserSignInRequest;
import com.epay.domain.auth.input.UserSignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authService;

    /** POST /auth/register */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserSignUpRequest request) {
        return authService.createAccount(request);
    }

    /** POST /auth/login */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserSignInRequest request,
                                    HttpServletResponse response,
                                    HttpServletRequest httpRequest) {
        return authService.login(request, response, httpRequest);
    }

    @PostMapping("/send-verify-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String identifier, @RequestParam ContactMethod method) {
        return authService.sendVerificationCode(identifier, method);
    }

    /** GET /auth/verify?token=&id= */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam String token,
                                            @RequestParam Long id) {
        return authService.verifyUser(token, id);
    }

    /** GET /auth/resend-verification?token= */
    @GetMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String token) {
        var result = authService.generateVerificationToken(token);
        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /** POST /auth/forgot-password */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    /** POST /auth/reset-password */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request,
                                            HttpServletRequest httpRequest) {
        return authService.confirmResetPassword(request, httpRequest);
    }

    /** POST /auth/forgot-username */
    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(@Valid @RequestBody ForgotUsernameRequest request) {
        return authService.forgotUsername(request);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam(name = "userId", required = false) Long userId) {
        return authService.logoutUser(userId); 
    }
}
