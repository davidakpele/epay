package com.example.admin_api_service.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.admin_api_service.clients.UserServiceClient;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@PreAuthorize("isAuthenticated()")
public class BusinessApiController {

    private final UserServiceClient userServiceClient;
    
    public BusinessApiController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    // Only ADMIN who owns the resource
    @PreAuthorize("hasAuthority('ADMIN') and @security.isOwner(#userId)")
    @GetMapping("/{userId}/wallet")
    public ResponseEntity<?> getWallet(@PathVariable Long userId,
            HttpServletRequest request) {
        return ResponseEntity.ok("Hello welcome back");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/count")
    public ResponseEntity<?> getTotalUsers(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long count = userServiceClient.getTotalUsers(token);
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", count);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("@security.isOwnerOrAdmin(#userId)")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long userId,
            HttpServletRequest request) {
        return ResponseEntity.ok("Profile data");
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok("Users list");
    }

    @PreAuthorize("@security.isOwnerOrAdmin(#userId)")
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("transactions", List.of());
        return ResponseEntity.ok(result);
    }
    

    @PostMapping("/api/verify-user")
    public ResponseEntity<?> verifyUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
        }
        // call your service here
        boolean verified = true;
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("verified", verified);
        return ResponseEntity.ok(result);
    }
    
}
