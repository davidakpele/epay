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
import com.example.admin_api_service.clients.HistoryServiceClient;
import com.example.admin_api_service.clients.UserServiceClient;
import com.example.admin_api_service.clients.VirtualCardServiceClient;
import com.example.admin_api_service.responses.DashboardSummaryResponse;
import com.example.admin_api_service.services.SystemWalletService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@PreAuthorize("isAuthenticated()")
public class BusinessApiController {

    private final UserServiceClient userServiceClient;
    private final SystemWalletService systemWalletService;
    private final HistoryServiceClient historyServiceClient;
    private final VirtualCardServiceClient virtualCardServiceClient;

    public BusinessApiController(UserServiceClient userServiceClient, SystemWalletService systemWalletService, HistoryServiceClient historyServiceClient, VirtualCardServiceClient virtualCardServiceClient) {
        this.userServiceClient = userServiceClient;
        this.systemWalletService = systemWalletService;
        this.historyServiceClient = historyServiceClient;
        this.virtualCardServiceClient = virtualCardServiceClient;
    }

    @PreAuthorize("hasAuthority('ADMIN') and @security.isOwner(#userId)")
    @GetMapping("/{userId}/wallet")
    public ResponseEntity<?> getWallet(@PathVariable Long userId,
            HttpServletRequest request) {
        return ResponseEntity.ok("Hello welcome back");
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> getDashboardSummary(HttpServletRequest request) {
        String token = extractToken(request);
        long totalUsers        = userServiceClient.getTotalUsers(token);
        long totalHistory      = historyServiceClient.getTotalHistory(token);
        
        long totalVirtualCards = virtualCardServiceClient.getTotalVirtualCards(token);

        return ResponseEntity.ok(
                systemWalletService.buildDashboardSummary(totalUsers, totalHistory, totalVirtualCards)
        );
    
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

    private String extractToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
    
}
