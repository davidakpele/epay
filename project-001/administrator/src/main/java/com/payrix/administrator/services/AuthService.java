package com.payrix.administrator.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.payrix.administrator.models.User;
import com.payrix.administrator.payloads.LoginRequest;
import com.payrix.administrator.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.AuthenticationException;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, 
                      JwtService jwtService, 
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        Map<String, Object> authResponse = new HashMap<>();
        Optional<User> userInfo = userRepository.findByUsername(request.getUsername());
        if (userInfo.isEmpty()) {
            authResponse.put("status", HttpStatus.BAD_REQUEST.value());
            authResponse.put("success", false);
            authResponse.put("message", "Invalid user credentials.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            

            User user = userInfo.get();
            String accessToken = jwtService.generateToken(user, user.getId());
            String refreshToken = jwtService.generateRefreshToken(user); 
            session.setAttribute("jwtToken", accessToken); 
            
            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            
            setTokenCookies(response, accessToken, refreshToken);
            authResponse.put("status", HttpStatus.OK.value());
            authResponse.put("success", true);
            authResponse.put("message", "User logged in successfully.");
            authResponse.put("accessToken", accessToken);
            authResponse.put("refreshToken", refreshToken);
            authResponse.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole()
            ));
            authResponse.put("expiresIn", jwtService.getExpirationDate(accessToken));

            return ResponseEntity.ok(authResponse);
            
        } catch (AuthenticationException ex) {
            authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            authResponse.put("success", false);
            authResponse.put("message", "Invalid user credentials.");
            authResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
        }
    }

    public ResponseEntity<?> refreshToken(String refreshToken, HttpServletResponse response) {
        Map<String, Object> authResponse = new HashMap<>();

        try {
            // Validate refresh token
            if (refreshToken == null || refreshToken.isEmpty()) {
                authResponse.put("status", HttpStatus.BAD_REQUEST.value());
                authResponse.put("success", false);
                authResponse.put("message", "Refresh token is required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
            }

            // Check if token is a refresh token
            String tokenType = jwtService.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                authResponse.put("status", HttpStatus.BAD_REQUEST.value());
                authResponse.put("success", false);
                authResponse.put("message", "Invalid token type. Refresh token required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
            }

            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);
            
            // Load user by username
            Optional<User> userInfo = userRepository.findByUsername(username);
            if (userInfo.isEmpty()) {
                authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                authResponse.put("success", false);
                authResponse.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
            }

            User user = userInfo.get();
            
            // Validate refresh token against user
            if (!jwtService.isTokenValid(refreshToken, user)) {
                authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                authResponse.put("success", false);
                authResponse.put("message", "Invalid refresh token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
            }

            // Generate new tokens
            String newAccessToken = jwtService.generateToken(user, user.getId());
            String newRefreshToken = jwtService.generateRefreshToken(user);
            
            // Update response headers and cookies
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            setTokenCookies(response, newAccessToken, newRefreshToken);
            
            // Build response
            authResponse.put("status", HttpStatus.OK.value());
            authResponse.put("success", true);
            authResponse.put("message", "Token refreshed successfully.");
            authResponse.put("accessToken", newAccessToken);
            authResponse.put("refreshToken", newRefreshToken);
            authResponse.put("expiresIn", jwtService.getExpirationDate(newAccessToken));

            return ResponseEntity.ok(authResponse);
            
        } catch (Exception ex) {
            authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            authResponse.put("success", false);
            authResponse.put("message", "Token refresh failed.");
            authResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }
    }

    public ResponseEntity<?> logout(HttpServletResponse response) {
        Map<String, Object> authResponse = new HashMap<>();
        
        try {
            // Clear authentication context
            SecurityContextHolder.clearContext();
            
            // Clear cookies
            clearTokenCookies(response);
            
            authResponse.put("status", HttpStatus.OK.value());
            authResponse.put("success", true);
            authResponse.put("message", "Logged out successfully.");
            
            return ResponseEntity.ok(authResponse);
            
        } catch (Exception ex) {
            authResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            authResponse.put("success", false);
            authResponse.put("message", "Logout failed.");
            authResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(authResponse);
        }
    }

    public ResponseEntity<?> validateToken(String token) {
        Map<String, Object> authResponse = new HashMap<>();
        
        try {
            // Validate token
            boolean isValid = jwtService.validateToken(token);
            if (!isValid) {
                authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                authResponse.put("success", false);
                authResponse.put("message", "Invalid token.");
                authResponse.put("valid", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
            }
            
            // Check if token expired
            boolean isExpired = jwtService.isTokenExpired(token);
            if (isExpired) {
                authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                authResponse.put("success", false);
                authResponse.put("message", "Token expired.");
                authResponse.put("valid", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
            }
            
            // Extract user info
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            List<String> roles = jwtService.extractRoles(token);
            long remainingValidity = jwtService.getRemainingValidity(token);
            
            // Build response
            authResponse.put("status", HttpStatus.OK.value());
            authResponse.put("success", true);
            authResponse.put("message", "Token is valid.");
            authResponse.put("valid", true);
            authResponse.put("username", username);
            authResponse.put("userId", userId);
            authResponse.put("roles", roles);
            authResponse.put("remainingValidityMs", remainingValidity);
            authResponse.put("tokenType", jwtService.getTokenType(token));
            
            return ResponseEntity.ok(authResponse);
            
        } catch (Exception ex) {
            authResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            authResponse.put("success", false);
            authResponse.put("message", "Token validation failed.");
            authResponse.put("valid", false);
            authResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access token cookie (short-lived)
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(24 * 60 * 60); // 24 hours in seconds
        
        // Refresh token cookie (long-lived)
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
        
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        // Clear access token cookie
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        
        // Clear refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

}