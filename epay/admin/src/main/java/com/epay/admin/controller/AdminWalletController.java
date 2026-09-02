package com.epay.admin.controller;

import com.epay.admin.service.AdminWalletManagementService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.dto.AdminWalletDTO;
import com.epay.domain.admin.input.AdminSetPinRequest;
import com.epay.domain.admin.input.AdminWalletActionRequest;
import com.epay.domain.history.entity.TransactionHistory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    private final AdminWalletManagementService walletService;
    private final JwtClaimsHolder              jwtClaims;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('wallet:read')")
    public ResponseEntity<ApiResponse<Page<AdminWalletDTO>>> listAllWallets(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.listAllWallets(pageable)));
    }

    @GetMapping("/frozen")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:read')")
    public ResponseEntity<ApiResponse<Page<AdminWalletDTO>>> listFrozenWallets(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.listFrozenWallets(pageable)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:read')")
    public ResponseEntity<ApiResponse<AdminWalletDTO>> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.getWalletByUserId(userId)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('admin:access')")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.getWalletStats()));
    }

    @PatchMapping("/user/{userId}/freeze")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:freeze')")
    public ResponseEntity<ApiResponse<Void>> freezeWallet(
            @PathVariable Long userId,
            @Valid @RequestBody AdminWalletActionRequest request,
            Authentication auth) {
        walletService.freezeWallet(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Wallet frozen.", null));
    }

    @PatchMapping("/user/{userId}/unfreeze")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:freeze')")
    public ResponseEntity<ApiResponse<Void>> unfreezeWallet(
            @PathVariable Long userId,
            @Valid @RequestBody AdminWalletActionRequest request,
            Authentication auth) {
        walletService.unfreezeWallet(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Wallet unfrozen.", null));
    }

    @PostMapping("/user/{userId}/pin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:update')")
    public ResponseEntity<ApiResponse<Void>> setPin(
            @PathVariable Long userId,
            @Valid @RequestBody AdminSetPinRequest request,
            Authentication auth) {
        walletService.setWalletPin(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Transfer PIN set.", null));
    }

    @DeleteMapping("/user/{userId}/pin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:update')")
    public ResponseEntity<ApiResponse<Void>> resetPin(
            @PathVariable Long userId,
            @Valid @RequestBody AdminWalletActionRequest request,
            Authentication auth) {
        walletService.resetWalletPin(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Transfer PIN cleared.", null));
    }

    @GetMapping("/user/{userId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Page<TransactionHistory>>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TransactionHistory> page = (status != null && !status.isBlank())
                ? walletService.getUserTransactionsByStatus(userId, status, pageable)
                : walletService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(null, page));
    }
}
