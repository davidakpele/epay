package com.payrix.administrator.controllers;

import com.payrix.administrator.exceptions.Error;
import com.payrix.administrator.dtos.AdminDTO;
import com.payrix.administrator.dtos.AdminUserVerificationDTO;
import com.payrix.administrator.payloads.LoginRequest;
import com.payrix.administrator.services.AuthService;
import com.payrix.administrator.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
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
    
    @GetMapping("/username/{username}")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        if (username == null || username.isEmpty()) {
            return Error.createResponse("Username is require.*", HttpStatus.BAD_REQUEST, "Username is require.*");
        } else if (adminUserService.getUserByUsername(username) == null) {
            return Error.createResponse("User with username " + username + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        AdminDTO userDTO = adminUserService.findPublicUserByUsername(username);

        return ResponseEntity.ok(userDTO);
    }

}