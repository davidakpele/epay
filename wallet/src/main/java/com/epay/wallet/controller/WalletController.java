package com.epay.wallet.controller;

import com.epay.domain.wallet.input.AddCurrencyRequest;
import com.epay.domain.wallet.input.ChangePinRequest;
import com.epay.domain.wallet.input.CreateWalletRequest;
import com.epay.domain.wallet.input.InvestmentCreditRequest;
import com.epay.domain.wallet.input.InvestmentDebitRequest;
import com.epay.domain.wallet.input.MaintenanceDebitRequest;
import com.epay.domain.wallet.input.SavingsCreditRequest;
import com.epay.domain.wallet.input.SavingsDebitRequest;
import com.epay.domain.wallet.input.SetPinRequest;
import com.epay.domain.wallet.input.TransferRequest;
import com.epay.domain.wallet.input.WalletRefundRequest;
import com.epay.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // --- Read ---

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getWallet(@PathVariable Long userId) {
        return walletService.getWalletByUserId(userId);
    }

    @GetMapping("/{userId}/balance/{currency}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getWalletByCurrency(@PathVariable Long userId,
                                                  @PathVariable String currency) {
        return walletService.getWalletByUserIdAndCurrencyType(userId, currency);
    }

    // --- Create wallet ---

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request);
    }

    // --- Currency management ---

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

    // --- PIN management ---

    @PostMapping("/{userId}/pin")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> setPin(@PathVariable Long userId,
                                     @Valid @RequestBody SetPinRequest request) {
        return walletService.setPin(userId, request);
    }

    @PutMapping("/{userId}/pin")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePin(@PathVariable Long userId,
                                        @Valid @RequestBody ChangePinRequest request) {
        return walletService.changePin(userId, request);
    }

    // --- Transfer ---

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        return walletService.transfer(request, request.getSenderUserId());
    }

    // --- Balance update (internal — called by other modules) ---

    @PatchMapping("/balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<?> updateBalance(@RequestParam String currency,
                                            @RequestParam BigDecimal amount,
                                            @RequestParam Long userId,
                                            @RequestParam Long walletId) {
        return walletService.updateBalance(currency, amount, userId, walletId);
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
    public ResponseEntity<?> refund(@Valid @RequestBody WalletRefundRequest request) {
        return walletService.refundWallet(request);
    }

    // --- Internal module endpoints (maintenance, investment, savings) ---

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
