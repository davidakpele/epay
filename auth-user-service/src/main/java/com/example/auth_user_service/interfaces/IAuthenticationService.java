package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.ConfirmResetPasswordRequest;
import com.example.auth_user_service.payloads.ForgotPasswordRequest;
import com.example.auth_user_service.payloads.ForgotUsernameRequest;
import com.example.auth_user_service.payloads.UserSignInRequest;
import com.example.auth_user_service.payloads.UserSignUpRequest;
import com.example.auth_user_service.responses.VerificationTokenResult;

public interface IAuthenticationService {
    ResponseEntity<?> createAccount(UserSignUpRequest request);

    ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response, HttpServletRequest httpRequest);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsername(String username);

    ResponseEntity<?> verifyUser(String token, Long id);

    VerificationTokenResult generateVerificationToken(String oldToken);

    ResponseEntity<?> createWallet(Long id);

    /**
     * Step 1 — generate a 4-digit OTP and send it to the user's email/phone.
     */
    ResponseEntity<?> forgotPassword(ForgotPasswordRequest request);

    /**
     * Step 2 — verify the OTP, validate the new password, and update it.
     */
    ResponseEntity<?> confirmResetPassword(ConfirmResetPasswordRequest request, HttpServletRequest httpRequest);

    /**
     * Looks up the account by email and sends the username back to that address.
     */
    ResponseEntity<?> forgotUsername(ForgotUsernameRequest request);
}



