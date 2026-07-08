package com.epay.history.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.history.domain.entity.TransactionHistory;
import com.epay.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;


    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TransactionHistory>>> getHistory(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<TransactionHistory> page = historyService.getByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved.", page));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TransactionHistory>>> getByType(
            @RequestAttribute("userId") Long userId,
            @PathVariable String type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<TransactionHistory> page = historyService.getByUserIdAndType(userId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success("Filtered history retrieved.", page));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionHistory>> getByReference(
            @PathVariable String reference) {

        return historyService.getByReference(reference)
                .map(h -> ResponseEntity.ok(ApiResponse.success(null, h)))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TransactionHistory>> getByTransactionId(
            @PathVariable String transactionId) {

        return historyService.getByTransactionId(transactionId)
                .map(h -> ResponseEntity.ok(ApiResponse.success(null, h)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TransactionHistory>>> getByDateRange(
            @RequestAttribute("userId") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<TransactionHistory> list = historyService.getByUserIdAndDateRange(userId, from, to);
        return ResponseEntity.ok(ApiResponse.success("History by date range retrieved.", list));
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<TransactionHistory>>> getByWalletId(
            @PathVariable Long walletId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<TransactionHistory> page = historyService.getByWalletId(walletId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Wallet history retrieved.", page));
    }
}
