package com.example.admin_api_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.admin_api_service.dto.AdminDTO;
import com.example.admin_api_service.dto.AdminUserVerificationDTO;
import com.example.admin_api_service.exceptions.ApiErrorReponse;
import com.example.admin_api_service.payloads.LoginRequest;
import com.example.admin_api_service.services.AuthService;
import com.example.admin_api_service.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthApiController {
    
    private final AuthService authService;
    private final UserService adminUserService;

    public AuthApiController(AuthService authService, UserService adminUserService) {
        this.authService = authService;
        this.adminUserService = adminUserService;
    }

    @PostMapping("/login")
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
    
    @GetMapping("/username/{username}")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        if (username == null || username.isEmpty()) {
            return ApiErrorReponse.createResponse("Username is require.*", HttpStatus.BAD_REQUEST, "Username is require.*");
        } else if (adminUserService.getUserByUsername(username) == null) {
            return ApiErrorReponse.createResponse("User with username " + username + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        AdminDTO userDTO = adminUserService.findPublicUserByUsername(username);

        return ResponseEntity.ok(userDTO);
    }

}