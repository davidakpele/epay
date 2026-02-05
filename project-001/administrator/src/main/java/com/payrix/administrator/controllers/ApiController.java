package com.payrix.administrator.controllers;

import com.payrix.administrator.dtos.AdminUserVerificationDTO;
import com.payrix.administrator.payloads.LoginRequest;
import com.payrix.administrator.services.AuthService;
import com.payrix.administrator.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApiController {
    
    private final AuthService authService;
    private final UserService adminUserService;

    public ApiController(AuthService authService, UserService adminUserService) {
        this.authService = authService;
        this.adminUserService = adminUserService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response,  HttpServletRequest httpRequest) {
        return authService.login(loginRequest, response, httpRequest);
    }

    @PostMapping("/verify-user")
    public ResponseEntity<Boolean> verifyUser(@RequestBody AdminUserVerificationDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean verified = adminUserService.isUserExists(dto.getUsername()); 
        return ResponseEntity.ok(verified);
    }
    
}