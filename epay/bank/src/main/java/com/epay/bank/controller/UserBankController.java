package com.epay.bank.controller;

import com.epay.bank.service.UserBankService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.bank.dto.PaystackDtos.PayStackBankList;
import com.epay.domain.bank.dto.PaystackDtos.PaystackAccountData;
import com.epay.domain.bank.entity.UserBankList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bank Accounts", description = "Save and manage external bank accounts for withdrawals")
@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class UserBankController {

    private final UserBankService bankService;

    @Operation(
        summary     = "Save a bank account",
        description = "Adds a verified external bank account to the user's profile for use in withdrawals."
    )
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserBankList>> createBank(
            @Valid @RequestBody UserBankList request) {
        UserBankList saved = bankService.createBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank account saved successfully.", saved));
    }

    @Operation(
        summary     = "List saved bank accounts for a user",
        description = "Returns all bank accounts previously saved by the given user."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<UserBankList>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, bankService.findByUserId(userId)));
    }

    @Operation(
        summary     = "Get a bank account by ID",
        description = "Returns a single saved bank account record by its database ID."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserBankList>> getById(@PathVariable Long id) {
        return bankService.findById(id)
                .map(b -> ResponseEntity.ok(ApiResponse.success(null, b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary     = "Delete saved bank accounts",
        description = "Removes one or more saved bank account records by their IDs."
    )
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteByIds(@RequestBody List<Long> ids) {
        bankService.deleteByIds(ids);
        return ResponseEntity.ok(ApiResponse.success("Bank account(s) removed.", null));
    }

    @Operation(
        summary     = "List all supported Nigerian banks",
        description = "Fetches the current list of banks from Paystack, including bank codes needed for account verification."
    )
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PayStackBankList>>> listBanks() {
        return ResponseEntity.ok(ApiResponse.success(null, bankService.fetchAllBanks()));
    }

    @Operation(
        summary     = "Verify a bank account number",
        description = "Resolves an account number against the given bank code via Paystack and returns the account holder's name."
    )
    @GetMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaystackAccountData>> verifyAccount(
            @RequestParam String accountNumber,
            @RequestParam String bankCode) {
        PaystackAccountData data = bankService.verifyExternal(accountNumber, bankCode);
        return ResponseEntity.ok(ApiResponse.success(null, data));
    }
}
