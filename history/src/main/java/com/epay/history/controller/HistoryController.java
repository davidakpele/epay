package com.epay.history.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.domain.history.dto.TransactionDTO;
import com.epay.domain.history.entity.TransactionAuditLog;
import com.epay.domain.history.enums.TransactionStatus;
import com.epay.history.service.HistoryService;
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

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    // -----------------------------------------------------------------------
    // User: own history
    // -----------------------------------------------------------------------

    /**
     * GET /history?page=0&size=50
     *
     * Returns paginated transaction history for the authenticated user.
     * Default page size is 50. "Load more" → increment page by 1.
     *
     * Response includes:
     *   - content[]     : list of transactions for this page
     *   - page          : current page number (0-based)
     *   - size          : page size
     *   - totalElements : total number of transactions
     *   - hasMore       : true if there are more pages
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        // Cap page size at 100 to prevent abuse
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        Page<TransactionDTO> result = historyService.getByUserId(userId, pageable);

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("hasMore",       !result.isLast());

        return ResponseEntity.ok(ApiResponse.success(null, body));
    }

    /**
     * GET /history/user/{userId}?page=0&size=50
     *
     * Fetch history for a specific user by their ID.
     * Same pagination as /history — designed for both user self-fetch and admin use.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoryByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        Page<TransactionDTO> result = historyService.getByUserId(userId, pageable);

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("hasMore",       !result.isLast());

        return ResponseEntity.ok(ApiResponse.success(null, body));
    }

    /** GET /history/type/{type}?page=0&size=20 */
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

    /** GET /history/status/{status}?page=0&size=20 */
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

    /** GET /history/date-range?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59 */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getByDateRange(
            @RequestAttribute("userId") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getByDateRange(userId, from, to)));
    }

    /** GET /history/{transactionId} */
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getByTransactionId(
            @PathVariable String transactionId) {
        return historyService.getByTransactionId(transactionId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null, dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /history/{transactionId}/timeline — returns just the status timeline */
    @GetMapping("/{transactionId}/timeline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> getTimeline(
            @PathVariable String transactionId) {
        return historyService.getByTransactionId(transactionId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null,
                        (Object) dto.getStatusTimeline())))
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /history/summary?type=DEPOSIT — total delivered amount for a type */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @RequestAttribute("userId") Long userId,
            @RequestParam String type) {
        BigDecimal total = historyService.sumDeliveredByType(userId, type);
        return ResponseEntity.ok(ApiResponse.success(null,
                Map.of("type", type.toUpperCase(), "totalDelivered", total)));
    }

    // -----------------------------------------------------------------------
    // Admin endpoints
    // -----------------------------------------------------------------------

    /** GET /history/admin/wallet/{walletId} */
    @GetMapping("/admin/wallet/{walletId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getByWallet(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getByWalletId(walletId, pageable)));
    }

    /** GET /history/admin/audit/{transactionId} — full audit trail for a transaction */
    @GetMapping("/admin/audit/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TransactionAuditLog>>> getAuditLog(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getAuditLog(transactionId)));
    }

    /** GET /history/admin/reference/{reference} */
    @GetMapping("/admin/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getByReference(
            @PathVariable String reference) {
        return historyService.getByReference(reference)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null, dto)))
                .orElse(ResponseEntity.notFound().build());
    }
}
