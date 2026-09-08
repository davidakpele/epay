package com.epay.admin.controller;

import com.epay.admin.service.AdminTransactionService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.dto.AdminTransactionDTO;
import com.epay.domain.admin.input.AdminFlagTransactionRequest;
import com.epay.domain.admin.input.AdminTransactionNoteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final AdminTransactionService transactionService;
    private final JwtClaimsHolder         jwtClaims;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Page<AdminTransactionDTO>>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Boolean amlFlag,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.searchTransactions(
                        userId, status, type, currency,
                        minAmount, maxAmount, from, to, amlFlag, pageable)));
    }

    @GetMapping("/{txnId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<AdminTransactionDTO>> getTransaction(
            @PathVariable String txnId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.getByTransactionId(txnId)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Page<AdminTransactionDTO>>> getUserTransactions(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.getByUserId(userId, pageable)));
    }

    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Object>> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.getTransactionStats(userId)));
    }

    @PatchMapping("/{txnId}/note")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:update')")
    public ResponseEntity<ApiResponse<AdminTransactionDTO>> addNote(
            @PathVariable String txnId,
            @Valid @RequestBody AdminTransactionNoteRequest request,
            Authentication auth) {
        Long adminId = jwtClaims.getUserId();
        String reviewer = adminId != null ? "admin:" + adminId : "admin";
        return ResponseEntity.ok(ApiResponse.success("Note added.",
                transactionService.addNote(txnId, request, reviewer)));
    }

    @PatchMapping("/{txnId}/flag")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('payment:update')")
    public ResponseEntity<ApiResponse<AdminTransactionDTO>> flagTransaction(
            @PathVariable String txnId,
            @Valid @RequestBody AdminFlagTransactionRequest request,
            Authentication auth) {
        Long adminId = jwtClaims.getUserId();
        String reviewer = adminId != null ? "admin:" + adminId : "admin";
        return ResponseEntity.ok(ApiResponse.success("Transaction flagged.",
                transactionService.flagTransaction(txnId, request, reviewer)));
    }
}
