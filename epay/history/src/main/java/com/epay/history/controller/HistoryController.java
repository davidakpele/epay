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

    
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistory(
            @RequestAttribute("userId") Long userId,
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


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
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
                transactionType,
                currency,
                txnStatus,
                pageable);

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("content",       result.getContent());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("hasMore",       !result.isLast());
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getByDateRange(
            @RequestAttribute("userId") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getByDateRange(userId, from, to)));
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getByTransactionId(
            @PathVariable String transactionId) {
        return historyService.getByTransactionId(transactionId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null, dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{transactionId}/timeline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> getTimeline(
            @PathVariable String transactionId) {
        return historyService.getByTransactionId(transactionId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null,
                        (Object) dto.getStatusTimeline())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @RequestAttribute("userId") Long userId,
            @RequestParam String type) {
        BigDecimal total = historyService.sumDeliveredByType(userId, type);
        return ResponseEntity.ok(ApiResponse.success(null,
                Map.of("type", type.toUpperCase(), "totalDelivered", total)));
    }

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

    @GetMapping("/admin/audit/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<TransactionAuditLog>>> getAuditLog(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                historyService.getAuditLog(transactionId)));
    }

    @GetMapping("/admin/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getByReference(
            @PathVariable String reference) {
        return historyService.getByReference(reference)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(null, dto)))
                .orElse(ResponseEntity.notFound().build());
    }
}