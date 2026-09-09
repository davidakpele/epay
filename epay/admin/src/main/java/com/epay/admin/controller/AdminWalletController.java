package com.epay.admin.controller;

import com.epay.admin.service.AdminWalletManagementService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.dto.AdminWalletDTO;
import com.epay.domain.admin.input.AdminSetPinRequest;
import com.epay.domain.admin.input.AdminWalletActionRequest;
import com.epay.domain.history.entity.TransactionHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin — Wallets", description = "Inspect, freeze, unfreeze, and manage user wallets and transfer PINs")
@RestController
@RequestMapping("/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    private final AdminWalletManagementService walletService;
    private final JwtClaimsHolder              jwtClaims;

    @Operation(
        summary     = "List all wallets",
        description = "Returns a paginated list of every wallet on the platform with balance summaries."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('wallet:read')")
    public ResponseEntity<ApiResponse<Page<AdminWalletDTO>>> listAllWallets(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.listAllWallets(pageable)));
    }

    @Operation(
        summary     = "List frozen wallets",
        description = "Returns all wallets that are currently frozen and cannot process transactions."
    )
    @GetMapping("/frozen")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:read')")
    public ResponseEntity<ApiResponse<Page<AdminWalletDTO>>> listFrozenWallets(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.listFrozenWallets(pageable)));
    }

    @Operation(
        summary     = "Get wallet for a user",
        description = "Returns the full wallet details (all currency balances and settings) for the specified user."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:read')")
    public ResponseEntity<ApiResponse<AdminWalletDTO>> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.getWalletByUserId(userId)));
    }

    @Operation(
        summary     = "Get wallet platform statistics",
        description = "Returns platform-wide wallet stats: total wallets, active vs frozen counts, and total balances by currency."
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('admin:access')")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(null, walletService.getWalletStats()));
    }

    @Operation(
        summary     = "Freeze a user's wallet",
        description = "Locks the wallet so no transactions can be processed. A reason must be supplied for audit purposes."
    )
    @PatchMapping("/user/{userId}/freeze")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:freeze')")
    public ResponseEntity<ApiResponse<Void>> freezeWallet(
            @PathVariable Long userId,
            @Valid @RequestBody AdminWalletActionRequest request,
            Authentication auth) {
        walletService.freezeWallet(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Wallet frozen.", null));
    }

    @Operation(
        summary     = "Unfreeze a user's wallet",
        description = "Restores normal operation to a previously frozen wallet."
    )
    @PatchMapping("/user/{userId}/unfreeze")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:freeze')")
    public ResponseEntity<ApiResponse<Void>> unfreezeWallet(
            @PathVariable Long userId,
            @Valid @RequestBody AdminWalletActionRequest request,
            Authentication auth) {
        walletService.unfreezeWallet(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Wallet unfrozen.", null));
    }

    @Operation(
        summary     = "Set a wallet transfer PIN (admin)",
        description = "Assigns or resets the 4-digit transfer PIN for a user's wallet. Used for customer support PIN recovery."
    )
    @PostMapping("/user/{userId}/pin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:update')")
    public ResponseEntity<ApiResponse<Void>> setPin(
            @PathVariable Long userId,
            @Valid @RequestBody AdminSetPinRequest request,
            Authentication auth) {
        walletService.setWalletPin(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Transfer PIN set.", null));
    }

    @Operation(
        summary     = "Reset a wallet transfer PIN (admin)",
        description = "Clears the existing transfer PIN so the user must set a new one on next login."
    )
    @DeleteMapping("/user/{userId}/pin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('wallet:update')")
    public ResponseEntity<ApiResponse<Void>> resetPin(
            @PathVariable Long userId,
            @Valid @RequestBody AdminWalletActionRequest request,
            Authentication auth) {
        walletService.resetWalletPin(userId, request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Transfer PIN cleared.", null));
    }

    @Operation(
        summary     = "Get transaction history for a user's wallet",
        description = "Returns paginated transaction history for a user. Optionally filter by status (e.g. DELIVERED, FAILED)."
    )
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
