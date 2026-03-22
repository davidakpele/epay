package com.example.admin_api_service.Interfaces;

import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.admin_api_service.payloads.LoginRequest;

public interface IAuthService {
    ResponseEntity<?> login(LoginRequest request, HttpServletResponse response, HttpServletRequest httpRequest);

    ResponseEntity<?> refreshToken(String refreshToken, HttpServletResponse response);

    ResponseEntity<?> logout(HttpServletResponse response);

    ResponseEntity<?> validateToken(String token);
}