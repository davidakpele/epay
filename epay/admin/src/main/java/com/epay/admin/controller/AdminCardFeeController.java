package com.epay.admin.controller;

import com.epay.admin.service.CardFeeConfigService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.virtual_card.entity.CardFeeConfig;
import com.epay.domain.virtual_card.enums.CardType;
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


@RestController
@RequestMapping("/admin/virtual-cards/fees")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class AdminCardFeeController {

    private final CardFeeConfigService feeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardFeeConfig>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(null, feeService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardFeeConfig>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(null, feeService.getById(id)));
    }

    @GetMapping("/currency/{code}")
    public ResponseEntity<ApiResponse<List<CardFeeConfig>>> getByCurrency(
            @PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(null, feeService.getByCurrency(code)));
    }


    @GetMapping("/currency/{code}/{type}")
    public ResponseEntity<ApiResponse<CardFeeConfig>> getByCurrencyAndType(
            @PathVariable String code,
            @PathVariable CardType type) {
        return ResponseEntity.ok(ApiResponse.success(null,
                feeService.getByCurrencyAndType(code, type)));
    }

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

    @PatchMapping("/{id}/amount")
    public ResponseEntity<ApiResponse<CardFeeConfig>> updateAmount(
            @PathVariable Long id,
            @RequestBody UpdateAmountRequest request,
            Authentication auth) {
        Long adminId = extractAdminId(auth);
        return ResponseEntity.ok(ApiResponse.success("Fee amount updated.",
                feeService.updateFee(id, request.getFeeAmount(), adminId)));
    }


    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<CardFeeConfig>> enable(
            @PathVariable Long id,
            Authentication auth) {
        Long adminId = extractAdminId(auth);
        return ResponseEntity.ok(ApiResponse.success(
                "Card fee enabled — users will be charged on issuance.",
                feeService.setActive(id, true, adminId)));
    }

 
    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<CardFeeConfig>> disable(
            @PathVariable Long id,
            Authentication auth) {
        Long adminId = extractAdminId(auth);
        return ResponseEntity.ok(ApiResponse.success(
                "Card fee disabled — issuance is now free for this currency/type.",
                feeService.setActive(id, false, adminId)));
    }

    @Data
    public static class UpdateFeeRequest {
        @DecimalMin(value = "0.0", message = "Fee amount must not be negative")
        private BigDecimal feeAmount;
        private String description;
        private Boolean active;
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
        if (principal instanceof com.epay.domain.auth.entity.User user) {
            return user.getId();
        }
        return 0L;
    }
}
