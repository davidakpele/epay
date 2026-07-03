package com.epay.auth.interfaces;

import org.springframework.http.ResponseEntity;
import com.epay.auth.domain.entity.User;
import com.epay.domain.auth.input.ConfirmResetPasswordRequest;
import com.epay.domain.auth.input.ForgotPasswordRequest;
import com.epay.domain.auth.input.ForgotUsernameRequest;
import com.epay.domain.auth.input.UserSignInRequest;
import com.epay.domain.auth.input.UserSignUpRequest;
import com.epay.domain.auth.response.VerificationTokenResult;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthenticationService {
    ResponseEntity<?> createAccount(UserSignUpRequest request);

    ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response, HttpServletRequest httpRequest);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    ResponseEntity<?> verifyUser(String token, Long id);

    VerificationTokenResult generateVerificationToken(String oldToken);

    ResponseEntity<?> createWallet(Long id);

    ResponseEntity<?> forgotPassword(ForgotPasswordRequest request);

    ResponseEntity<?> confirmResetPassword(ConfirmResetPasswordRequest request, HttpServletRequest httpRequest);

    ResponseEntity<?> forgotUsername(ForgotUsernameRequest request);
}