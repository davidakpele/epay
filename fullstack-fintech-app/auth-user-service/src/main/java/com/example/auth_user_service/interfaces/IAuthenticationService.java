package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;
import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.UserSignInRequest;
import com.example.auth_user_service.payloads.UserSignUpRequest;
import com.example.auth_user_service.responses.VerificationTokenResult;

public interface IAuthenticationService {
    ResponseEntity<?> createAccount(UserSignUpRequest request);

    ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsername(String username);

    ResponseEntity<?> verifyUser(String token, Long id);

    VerificationTokenResult generateVerificationToken(String oldToken);

    ResponseEntity<?> createWallet(Long id);
}



