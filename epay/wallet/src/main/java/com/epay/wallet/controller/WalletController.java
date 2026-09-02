package com.epay.wallet.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epay.common.config.interfaces.WalletRateLimited;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.wallet.input.AddCurrencyRequest;
import com.epay.domain.wallet.input.ChangePinRequest;
import com.epay.domain.wallet.input.CreateWalletRequest;
import com.epay.domain.wallet.input.InvestmentCreditRequest;
import com.epay.domain.wallet.input.InvestmentDebitRequest;
import com.epay.domain.wallet.input.MaintenanceDebitRequest;
import com.epay.domain.wallet.input.SavingsCreditRequest;
import com.epay.domain.wallet.input.SavingsDebitRequest;
import com.epay.domain.wallet.input.SetPinRequest;
import com.epay.domain.wallet.input.SwapRequest;
import com.epay.domain.wallet.input.TransferRequest;
import com.epay.domain.wallet.input.WalletRefundRequest;
import com.epay.wallet.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/userId/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_USER') and @security.isOwner(#userId) and @security.hasPermission('wallet:read')")
    public ResponseEntity<?> getWallet(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body("Invalid user ID provided. Please provide a valid user ID.");
        }
        return walletService.getWalletByUserId(userId);
    }

    @GetMapping("/{userId}/balance/{currency}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_USER') and @security.hasPermission('wallet:read')")
    public ResponseEntity<?> getWalletByCurrency(@PathVariable Long userId, @PathVariable String currency) {
        return walletService.getWalletByUserIdAndCurrencyType(userId, currency);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN', 'SUPER_USER')")
    public ResponseEntity<?> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request);
    }

    @PostMapping("/{userId}/currencies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addCurrency(@PathVariable Long userId,
                                          @Valid @RequestBody AddCurrencyRequest request) {
        return walletService.addCurrency(userId, request);
    }

    @PatchMapping("/{userId}/currencies/{currency}/default")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> setDefaultCurrency(@PathVariable Long userId,
                                                 @PathVariable String currency) {
        return walletService.setDefaultCurrency(userId, currency);
    }

    @PostMapping("/create/{userId}/pin")
    @PreAuthorize("hasAnyRole('USER','ADMIN', 'SUPER_USER')")
    @WalletRateLimited(
        keyPrefix      = "pin_setup",
        capacity       = 5,
        duration       = 10,
        timeUnit       = java.util.concurrent.TimeUnit.MINUTES,
        userIdentifier = "#authentication.name",
        coolDownSeconds = 30
    )
    public ResponseEntity<?> setPin(@PathVariable Long userId,@Valid @RequestBody SetPinRequest request, Authentication authentication) {
        String providedPin = request.getPin();
        if (providedPin == null || providedPin.isEmpty()) {
            throw new BadRequestException("Your withdrawal/transfer pin is required", ErrorCode.INVALID_INPUT);
        }
        if (providedPin.length() != 4 || !providedPin.matches("\\d{4}")) {
            throw new BadRequestException("Invalid input. Please provide exactly 4 digits.", ErrorCode.INVALID_INPUT);
        }
        return walletService.setPin(userId, request, authentication);
    }

    @PutMapping("/{userId}/pin")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePin(@PathVariable Long userId,
                                        @Valid @RequestBody ChangePinRequest request) {
        return walletService.changePin(userId, request);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        return walletService.transfer(request, request.getSenderUserId());
    }

    @PostMapping("/{userId}/swap")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> swapCurrency(@PathVariable Long userId,
                                           @Valid @RequestBody SwapRequest request) {
        request.setUserId(userId);
        return walletService.swapCurrency(userId, request);
    }

    @PatchMapping("/balance")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('SUPER_USER')) and @security.hasPermission('wallet:update')")
    public ResponseEntity<?> updateBalance(@RequestParam String currency,
                                            @RequestParam BigDecimal amount,
                                            @RequestParam Long userId,
                                            @RequestParam Long walletId) {
        return walletService.updateBalance(currency, amount, userId, walletId);
    }

    @PostMapping("/refund")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('SUPER_USER')) and @security.hasPermission('wallet:update')")
    public ResponseEntity<?> refund(@Valid @RequestBody WalletRefundRequest request) {
        return walletService.refundWallet(request);
    }

    @PostMapping("/internal/maintenance/debit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> maintenanceDebit(@Valid @RequestBody MaintenanceDebitRequest request) {
        return walletService.processMaintenanceFee(request);
    }

    @PostMapping("/internal/investment/debit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> investmentDebit(@Valid @RequestBody InvestmentDebitRequest request) {
        return walletService.processInvestmentDebit(request);
    }

    @PostMapping("/internal/investment/credit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> investmentCredit(@Valid @RequestBody InvestmentCreditRequest request) {
        return walletService.processInvestmentCredit(request);
    }

    @PostMapping("/internal/savings/debit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> savingsDebit(@Valid @RequestBody SavingsDebitRequest request) {
        return walletService.processSavingsDebit(request);
    }

    @PostMapping("/internal/savings/credit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> savingsCredit(@Valid @RequestBody SavingsCreditRequest request) {
        return walletService.processSavingsCredit(request);
    }
}
