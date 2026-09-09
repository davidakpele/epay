package com.epay.admin.controller;

import com.epay.admin.service.CardFeeConfigService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.virtual_card.entity.CardFeeConfig;
import com.epay.domain.virtual_card.enums.CardType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Admin — Virtual Card Fees", description = "Configure and manage virtual card issuance fee schedules")
@RestController
@RequestMapping("/admin/virtual-cards/fees")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class AdminCardFeeController {

    private final CardFeeConfigService feeService;

    @Operation(
        summary     = "List all card fee configurations",
        description = "Returns every fee configuration across all currencies and card types (active and inactive)."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CardFeeConfig>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(null, feeService.getAll()));
    }

    @Operation(
        summary     = "Get a card fee configuration by ID",
        description = "Returns the fee configuration record for the given database ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardFeeConfig>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(null, feeService.getById(id)));
    }

    @Operation(
        summary     = "List fee configs for a currency",
        description = "Returns all fee configurations for the specified ISO-4217 currency code."
    )
    @GetMapping("/currency/{code}")
    public ResponseEntity<ApiResponse<List<CardFeeConfig>>> getByCurrency(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(null, feeService.getByCurrency(code)));
    }

    @Operation(
        summary     = "Get fee config for a currency and card type",
        description = "Returns the specific fee configuration for the combination of currency code and card type (VIRTUAL or PHYSICAL)."
    )
    @GetMapping("/currency/{code}/{type}")
    public ResponseEntity<ApiResponse<CardFeeConfig>> getByCurrencyAndType(
            @PathVariable String code, @PathVariable CardType type) {
        return ResponseEntity.ok(ApiResponse.success(null,
                feeService.getByCurrencyAndType(code, type)));
    }

    @Operation(
        summary     = "Update a card fee configuration",
        description = "Updates the fee amount, description, or active status for an existing fee config."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CardFeeConfig>> update(
            @PathVariable Long id,
            @RequestBody UpdateFeeRequest request,
            Authentication auth) {
        Long adminId = extractAdminId(auth);
        return ResponseEntity.ok(ApiResponse.success("Fee config updated.",
                feeService.update(id, request.getFeeAmount(),
                        request.getDescription(), request.getActive(), adminId)));
    }

    @Operation(
        summary     = "Update the fee amount",
        description = "Changes only the fee amount charged on card issuance for the specified config."
    )
    @PatchMapping("/{id}/amount")
    public ResponseEntity<ApiResponse<CardFeeConfig>> updateAmount(
            @PathVariable Long id,
            @RequestBody UpdateAmountRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Fee amount updated.",
                feeService.updateFee(id, request.getFeeAmount(), extractAdminId(auth))));
    }

    @Operation(
        summary     = "Enable a card fee config",
        description = "Activates the fee schedule — users will be charged the configured fee on card issuance."
    )
    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<CardFeeConfig>> enable(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Card fee enabled — users will be charged on issuance.",
                feeService.setActive(id, true, extractAdminId(auth))));
    }

    @Operation(
        summary     = "Disable a card fee config",
        description = "Deactivates the fee schedule — card issuance is free for this currency/type while disabled."
    )
    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<CardFeeConfig>> disable(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Card fee disabled — issuance is now free for this currency/type.",
                feeService.setActive(id, false, extractAdminId(auth))));
    }

    @Data
    public static class UpdateFeeRequest {
        @DecimalMin(value = "0.0", message = "Fee amount must not be negative")
        private BigDecimal feeAmount;
        private String     description;
        private Boolean    active;
    }

    @Data
    public static class UpdateAmountRequest {
        @NotNull(message = "feeAmount is required")
        @DecimalMin(value = "0.0", message = "Fee amount must not be negative")
        private BigDecimal feeAmount;
    }

    private Long extractAdminId(Authentication auth) {
        if (auth == null) return 0L;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Object uid = jwt.getClaim("userId");
            if (uid instanceof Number n) return n.longValue();
        }
        if (principal instanceof com.epay.domain.auth.entity.User user) return user.getId();
        return 0L;
    }
}
