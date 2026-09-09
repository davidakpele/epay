package com.epay.admin.controller;

import com.epay.admin.service.AdminTransactionService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.dto.AdminTransactionDTO;
import com.epay.domain.admin.input.AdminFlagTransactionRequest;
import com.epay.domain.admin.input.AdminTransactionNoteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Admin — Transactions", description = "Search, inspect, flag, and annotate transactions across all users")
@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final AdminTransactionService transactionService;
    private final JwtClaimsHolder         jwtClaims;

    @Operation(
        summary     = "Search transactions",
        description = "Full-text and multi-criteria search across all platform transactions. Filter by userId, status, type, currency, amount range, date range, or AML flag."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Page<AdminTransactionDTO>>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Boolean amlFlag,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.searchTransactions(
                        userId, status, type, currency, minAmount, maxAmount, from, to, amlFlag, pageable)));
    }

    @Operation(
        summary     = "Get a transaction by transaction ID",
        description = "Returns the full admin view of a single transaction including AML flags, notes, and status history."
    )
    @GetMapping("/{txnId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<AdminTransactionDTO>> getTransaction(@PathVariable String txnId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.getByTransactionId(txnId)));
    }

    @Operation(
        summary     = "Get all transactions for a user",
        description = "Returns a paginated list of all transactions belonging to the specified user."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Page<AdminTransactionDTO>>> getUserTransactions(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.getByUserId(userId, pageable)));
    }

    @Operation(
        summary     = "Get transaction statistics for a user",
        description = "Returns aggregated stats (total count, volume by type, failure rate) for the specified user's transactions."
    )
    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('payment:read')")
    public ResponseEntity<ApiResponse<Object>> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                transactionService.getTransactionStats(userId)));
    }

    @Operation(
        summary     = "Add an admin note to a transaction",
        description = "Attaches an internal note to a transaction for compliance or customer-service reference. The note is not visible to the user."
    )
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

    @Operation(
        summary     = "Flag a transaction for review",
        description = "Marks a transaction with an AML or compliance flag and records the reviewer's reason."
    )
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
