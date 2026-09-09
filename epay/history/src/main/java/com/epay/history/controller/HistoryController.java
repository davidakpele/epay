package com.epay.history.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.domain.history.dto.TransactionDTO;
import com.epay.domain.history.entity.TransactionAuditLog;
import com.epay.domain.history.enums.TransactionStatus;
import com.epay.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Transaction History", description = "Query, filter, and manage transaction history records")
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(
        summary     = "Get own transaction history",
        description = "Returns a paginated list of the authenticated user's transactions, newest first."
    )
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        Page<TransactionDTO> result = historyService.getByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(null, buildPageBody(result)));
    }

    @Operation(
        summary     = "Get transaction history by user ID",
        description = "Returns a paginated transaction history for the specified user. Accessible by the owner, admin, or customer service."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoryByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        Page<TransactionDTO> result = historyService.getByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(null, buildPageBody(result)));
    }

    @Operation(
        summary     = "Get transactions by type",
        description = "Filters the authenticated user's history to a specific transaction type (e.g. TRANSFER_DEBIT, DEPOSIT, SWAP)."
    )
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getByType(
            @RequestAttribute("userId") Long userId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getByUserIdAndType(userId, type, pageable)));
    }

    @Operation(
        summary     = "Get transactions by status",
        description = "Filters the authenticated user's history to a specific status (e.g. DELIVERED, FAILED, PENDING)."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getByStatus(
            @RequestAttribute("userId") Long userId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        TransactionStatus txnStatus = TransactionStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getByUserIdAndStatus(userId, txnStatus, pageable)));
    }

    @Operation(
        summary     = "Filter transactions by multiple criteria",
        description = "Multi-criteria search across a user's transaction history. Supports date range, type, currency, and status filters simultaneously."
    )
    @GetMapping("/user/{userId}/filter")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> filterByUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(defaultValue = "ALL")  String transactionType,
            @RequestParam(required = false)      String currency,
            @RequestParam(required = false)      String status,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size) {

        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        TransactionStatus txnStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                txnStatus = TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid status value: '" + status
                                + "'. Valid values: " + java.util.Arrays.toString(TransactionStatus.values())));
            }
        }
        Page<TransactionDTO> result = historyService.filterByUser(
                userId,
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59),
                transactionType, currency, txnStatus, pageable);

        Map<String, Object> body = buildPageBody(result);
        body.put("filters", Map.of(
                "userId",          userId,
                "fromDate",        fromDate.toString(),
                "toDate",          toDate.toString(),
                "transactionType", transactionType.toUpperCase(),
                "currency",        currency != null ? currency.toUpperCase() : "ALL",
                "status",          status   != null ? status.toUpperCase()   : "ALL"
        ));
        return ResponseEntity.ok(ApiResponse.success(null, body));
    }

    @Operation(
        summary     = "Get a transaction by transaction ID",
        description = "Returns the full details of a single transaction using its unique transaction ID string."
    )
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getByTransactionId(
            @PathVariable String transactionId) {
        return historyService.getByTransactionId(transactionId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null, dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary     = "Get transaction status timeline",
        description = "Returns the full state-machine timeline and status history for a transaction."
    )
    @GetMapping("/{transactionId}/timeline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> getTimeline(@PathVariable String transactionId) {
        return historyService.getByTransactionId(transactionId)
                .map(dto -> {
                    Map<String, Object> timeline = new java.util.LinkedHashMap<>();
                    timeline.put("stateMachine",  dto.getStateMachine());
                    timeline.put("statusHistory", dto.getStatusHistory());
                    return ResponseEntity.ok(ApiResponse.success(null, (Object) timeline));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary     = "Get transaction amount summary by type",
        description = "Returns the total delivered amount for a given transaction type for the authenticated user."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @RequestAttribute("userId") Long userId,
            @RequestParam String type) {
        BigDecimal total = historyService.sumDeliveredByType(userId, type);
        return ResponseEntity.ok(ApiResponse.success(null,
                Map.of("type", type.toUpperCase(), "totalDelivered", total)));
    }

    @Operation(
        summary     = "Get all transactions for a wallet (admin)",
        description = "Returns a paginated list of all transactions associated with a specific wallet ID."
    )
    @GetMapping("/admin/wallet/{walletId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getByWallet(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getByWalletId(walletId, pageable)));
    }

    @Operation(
        summary     = "Get audit log for a transaction (admin)",
        description = "Returns the complete admin audit trail of every action taken on the given transaction."
    )
    @GetMapping("/admin/audit/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<TransactionAuditLog>>> getAuditLog(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getAuditLog(transactionId)));
    }

    @Operation(
        summary     = "Find a transaction by reference (admin)",
        description = "Looks up a transaction using its external or internal reference string."
    )
    @GetMapping("/admin/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getByReference(
            @PathVariable String reference) {
        return historyService.getByReference(reference)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null, dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary     = "Hide a transaction from the user",
        description = "Soft-hides a transaction so it no longer appears in the user's history view. The record is preserved for admin access."
    )
    @DeleteMapping("/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> hideTransaction(
            @PathVariable String transactionId,
            @RequestAttribute("userId") Long userId) {
        try {
            historyService.hideFromUser(transactionId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(
        summary     = "List all hidden transactions (admin)",
        description = "Returns every transaction that has been soft-hidden by a user, paginated."
    )
    @GetMapping("/admin/hidden")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHiddenTransactions(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(Math.min(size, 100) > 0 ? page : 0,
                Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(null,
                buildPageBody(historyService.getHiddenTransactions(pageable))));
    }

    @Operation(
        summary     = "List hidden transactions for a user (admin)",
        description = "Returns all transactions hidden by a specific user."
    )
    @GetMapping("/admin/hidden/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHiddenByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(null,
                buildPageBody(historyService.getHiddenByUserId(userId, pageable))));
    }

    @Operation(
        summary     = "Get all transactions for a user — admin view",
        description = "Returns the complete, unfiltered transaction history for a user including hidden records. Admin only."
    )
    @GetMapping("/admin/user/{userId}/all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllByUserAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(null,
                buildPageBody(historyService.getByUserIdAdmin(userId, pageable))));
    }

    @Operation(
        summary     = "Restore a hidden transaction (admin)",
        description = "Makes a previously hidden transaction visible again in the user's history."
    )
    @PatchMapping("/admin/{transactionId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<String>> restoreTransaction(
            @PathVariable String transactionId) {
        try {
            historyService.restoreToUser(transactionId);
            return ResponseEntity.ok(ApiResponse.success(null,
                    "Transaction " + transactionId + " restored to user visibility."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private java.util.LinkedHashMap<String, Object> buildPageBody(Page<TransactionDTO> result) {
        java.util.LinkedHashMap<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("hasMore",       !result.isLast());
        return body;
    }
}
