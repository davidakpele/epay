package com.epay.bank.controller;

import com.epay.bank.service.UserBankService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.bank.dto.PaystackDtos.PayStackBankList;
import com.epay.domain.bank.dto.PaystackDtos.PaystackAccountData;
import com.epay.domain.bank.entity.UserBankList;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class UserBankController {

    private final UserBankService bankService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserBankList>> createBank(
            @Valid @RequestBody UserBankList request) {
        UserBankList saved = bankService.createBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account saved successfully.", saved));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<UserBankList>>> getByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, bankService.findByUserId(userId)));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserBankList>> getById(@PathVariable Long id) {
        return bankService.findById(id)
                .map(b -> ResponseEntity.ok(ApiResponse.success(null, b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteByIds(
            @RequestBody List<Long> ids) {
        bankService.deleteByIds(ids);
        return ResponseEntity.ok(ApiResponse.success("Bank account(s) removed.", null));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PayStackBankList>>> listBanks() {
        return ResponseEntity.ok(ApiResponse.success(null, bankService.fetchAllBanks()));
    }
    
    @GetMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaystackAccountData>> verifyAccount(
            @RequestParam String accountNumber,
            @RequestParam String bankCode) {
        PaystackAccountData data = bankService.verifyExternal(accountNumber, bankCode);
        return ResponseEntity.ok(ApiResponse.success(null, data));
    }
}
