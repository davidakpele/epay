package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IWalletFreezeService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.WalletFreezeStatus;
import com.example.admin_api_service.models.userAndWalletManagement.WalletFreeze;
import com.example.admin_api_service.payloads.UnfreezePayload;
import com.example.admin_api_service.payloads.WalletFreezePayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet-freezes")
public class WalletFreezeController {

    private final IWalletFreezeService walletFreezeService;

    public WalletFreezeController(IWalletFreezeService walletFreezeService) {
        this.walletFreezeService = walletFreezeService;
    }

    @PostMapping("/wallets/{walletId}")
    public ResponseEntity<ApiResponse<WalletFreeze>> freezeWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody WalletFreezePayload payload,
            @AuthenticationPrincipal String adminId) {

        WalletFreeze freeze = walletFreezeService.freezeWallet(
                walletId,
                payload.getUserId(),
                payload.getFreezeType(),
                payload.getFreezeReason(),
                payload.getReasonNote(),
                payload.getExternalReference(),
                adminId,
                adminId, payload.getExpiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(freeze, "Wallet frozen successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WalletFreeze>>> getAllFreezes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) WalletFreezeStatus status) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<WalletFreeze> freezes = status != null
                ? walletFreezeService.getFreezesByStatus(status, pageable)
                : walletFreezeService.getAllFreezes(pageable);

        return ResponseEntity.ok(ApiResponse.success(freezes));
    }

    @GetMapping("/{freezeId}")
    public ResponseEntity<ApiResponse<WalletFreeze>> getFreezeById(
            @PathVariable String freezeId) {
        return ResponseEntity.ok(ApiResponse.success(
                walletFreezeService.getFreezeById(freezeId)));
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<ApiResponse<List<WalletFreeze>>> getFreezesByWallet(
            @PathVariable Long walletId) {
        return ResponseEntity.ok(ApiResponse.success(
                walletFreezeService.getActiveFreezesByWallet(walletId)));
    }

    @GetMapping("/wallets/{walletId}/active")
    public ResponseEntity<ApiResponse<Page<WalletFreeze>>> getActiveFreezesByWallet(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                walletFreezeService.getFreezesByWallet(
                        walletId,
                        PageRequest.of(page, size, Sort.by("createdOn").descending()))));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Page<WalletFreeze>>> getFreezesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                walletFreezeService.getFreezesByWallet(
                        userId,
                        PageRequest.of(page, size, Sort.by("createdOn").descending()))));
    }

    @PatchMapping("/{freezeId}/unfreeze")
    public ResponseEntity<ApiResponse<WalletFreeze>> unfreezeWallet(
            @PathVariable String freezeId,
            @Valid @RequestBody UnfreezePayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                walletFreezeService.unfreezeWallet(freezeId, adminId, payload.getUnfreezeNote()),
                "Wallet unfrozen successfully"));
    }

    @GetMapping("/wallets/{walletId}/debit-blocked")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isDebitBlocked(
            @PathVariable Long walletId) {

        boolean blocked = walletFreezeService.isWalletDebitBlocked(walletId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("debitBlocked", blocked)));
    }

    @GetMapping("/wallets/{walletId}/credit-blocked")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isCreditBlocked(
            @PathVariable Long walletId) {

        boolean blocked = walletFreezeService.isWalletCreditBlocked(walletId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("creditBlocked", blocked)));
    }

    @GetMapping("/wallets/{walletId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWalletFreezeStatus(
            @PathVariable Long walletId) {

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "walletId", walletId,
                "debitBlocked", walletFreezeService.isWalletDebitBlocked(walletId),
                "creditBlocked", walletFreezeService.isWalletCreditBlocked(walletId),
                "activeFreezes", walletFreezeService.getActiveFreezesByWallet(walletId).size()
        )));
    }
}