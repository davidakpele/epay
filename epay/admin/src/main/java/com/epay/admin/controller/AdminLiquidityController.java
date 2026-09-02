package com.epay.admin.controller;

import com.epay.admin.service.LiquidityService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.liquidity.dto.LiquidityStatusDTO;
import com.epay.domain.liquidity.entity.PlatformLedger;
import com.epay.domain.liquidity.input.UpdateLiquidityConfigRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/liquidity")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class AdminLiquidityController {

    private final LiquidityService liquidityService;

    @GetMapping("/paystack/status")
    public ResponseEntity<ApiResponse<LiquidityStatusDTO>> paystackStatus() {
        return ResponseEntity.ok(ApiResponse.success(null,
                liquidityService.getPaystackStatus()));
    }

    @GetMapping("/flutterwave/status")
    public ResponseEntity<ApiResponse<LiquidityStatusDTO>> flutterwaveStatus() {
        return ResponseEntity.ok(ApiResponse.success(null,
                liquidityService.getFlutterwaveStatus()));
    }

    @PostMapping("/paystack/sync")
    public ResponseEntity<ApiResponse<LiquidityStatusDTO>> syncPaystack(Authentication auth) {
        String actor = auth != null ? auth.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success("Paystack balance synced.",
                liquidityService.syncPaystackBalance(actor)));
    }

    @PostMapping("/flutterwave/sync")
    public ResponseEntity<ApiResponse<LiquidityStatusDTO>> syncFlutterwave(Authentication auth) {
        String actor = auth != null ? auth.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success("Flutterwave balance synced.",
                liquidityService.syncFlutterwaveBalance(actor)));
    }

    @PostMapping("/paystack/topup")
    public ResponseEntity<ApiResponse<PlatformLedger>> recordPaystackTopUp(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "NGN") String currency,
            @RequestParam(required = false) String description,
            Authentication auth) {
        String actor = auth != null ? auth.getName() : "admin";
        PlatformLedger entry = liquidityService.recordManualTopUp(
                "PAYSTACK", amount, currency,
                description != null ? description : "Manual top-up", actor);
        return ResponseEntity.ok(ApiResponse.success("Top-up recorded.", entry));
    }

    @PostMapping("/flutterwave/topup")
    public ResponseEntity<ApiResponse<PlatformLedger>> recordFlutterwaveTopUp(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "NGN") String currency,
            @RequestParam(required = false) String description,
            Authentication auth) {
        String actor = auth != null ? auth.getName() : "admin";
        PlatformLedger entry = liquidityService.recordManualTopUp(
                "FLUTTERWAVE", amount, currency,
                description != null ? description : "Manual top-up", actor);
        return ResponseEntity.ok(ApiResponse.success("Top-up recorded.", entry));
    }

    @PutMapping("/paystack/config")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<ApiResponse<Object>> updatePaystackConfig(
            @Valid @RequestBody UpdateLiquidityConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Paystack liquidity config updated.",
                liquidityService.updateConfig("PAYSTACK", request)));
    }

    @PutMapping("/flutterwave/config")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<ApiResponse<Object>> updateFlutterwaveConfig(
            @Valid @RequestBody UpdateLiquidityConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Flutterwave liquidity config updated.",
                liquidityService.updateConfig("FLUTTERWAVE", request)));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<Page<PlatformLedger>>> getLedger(
            @RequestParam(required = false) String gateway,
            @PageableDefault(size = 30, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                liquidityService.getLedger(gateway, pageable)));
    }

    @GetMapping("/payout-check")
    public ResponseEntity<ApiResponse<Object>> checkPayout(
            @RequestParam(defaultValue = "PAYSTACK") String gateway,
            @RequestParam BigDecimal amount) {
        boolean canPay = liquidityService.canProcessPayout(gateway, amount);
        return ResponseEntity.ok(ApiResponse.success(null,
                java.util.Map.of(
                        "gateway", gateway.toUpperCase(),
                        "requestedAmount", amount,
                        "payoutAllowed", canPay,
                        "message", canPay
                                ? "Float balance is sufficient."
                                : "Float balance is below threshold — payout blocked.")));
    }
}
