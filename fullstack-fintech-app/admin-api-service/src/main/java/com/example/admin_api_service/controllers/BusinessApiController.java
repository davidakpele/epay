package com.example.admin_api_service.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/admin")
@PreAuthorize("isAuthenticated()")
public class BusinessApiController {

    // Only ADMIN who owns the resource
    @PreAuthorize("hasAuthority('ADMIN') and @security.isOwner(#userId)")
    @GetMapping("/{userId}/wallet")
    public ResponseEntity<?> getWallet(@PathVariable Long userId,
            HttpServletRequest request) {
        return ResponseEntity.ok("Hello welcome back");
    }


    // ADMIN can access any user, USER can only access their own
    @PreAuthorize("@security.isOwnerOrAdmin(#userId)")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long userId,
            HttpServletRequest request) {
        return ResponseEntity.ok("Profile data");
    }

    // SUPER_ADMIN only
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.noContent().build();
    }

    // Multiple roles
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
    
}
