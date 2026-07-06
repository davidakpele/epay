package com.epay.wallet.controller;

import com.epay.domain.wallet.input.CreateCurrencyRequest;
import com.epay.domain.wallet.input.UpdateCurrencyRequest;
import com.epay.wallet.service.CurrencyConfigService;
import com.epay.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wallet")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletService          walletService;
    private final CurrencyConfigService  currencyConfigService;

    // --- Wallet admin controls ---

    @PatchMapping("/{userId}/freeze")
    public ResponseEntity<?> freezeWallet(@PathVariable Long userId,
                                           @AuthenticationPrincipal UserDetails admin) {
        return walletService.setWalletActive(userId, false, resolveAdminId(admin));
    }

    @PatchMapping("/{userId}/unfreeze")
    public ResponseEntity<?> unfreezeWallet(@PathVariable Long userId,
                                             @AuthenticationPrincipal UserDetails admin) {
        return walletService.setWalletActive(userId, true, resolveAdminId(admin));
    }

    // --- Currency catalog CRUD ---

    @GetMapping("/currencies")
    public ResponseEntity<?> getAllCurrencies() {
        return ResponseEntity.ok(currencyConfigService.getAll());
    }

    @GetMapping("/currencies/active")
    public ResponseEntity<?> getActiveCurrencies() {
        return ResponseEntity.ok(currencyConfigService.getAllActive());
    }

    @GetMapping("/currencies/{code}")
    public ResponseEntity<?> getCurrency(@PathVariable String code) {
        return ResponseEntity.ok(currencyConfigService.getByCode(code));
    }

    @PostMapping("/currencies")
    public ResponseEntity<?> createCurrency(@Valid @RequestBody CreateCurrencyRequest request,
                                             @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.status(201)
                .body(currencyConfigService.create(request, resolveAdminId(admin)));
    }

    @PutMapping("/currencies/{id}")
    public ResponseEntity<?> updateCurrency(@PathVariable Long id,
                                             @Valid @RequestBody UpdateCurrencyRequest request,
                                             @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(currencyConfigService.update(id, request, resolveAdminId(admin)));
    }

    @PatchMapping("/currencies/{id}/enable")
    public ResponseEntity<?> enableCurrency(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails admin) {
        currencyConfigService.setActive(id, true, resolveAdminId(admin));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/currencies/{id}/disable")
    public ResponseEntity<?> disableCurrency(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails admin) {
        currencyConfigService.setActive(id, false, resolveAdminId(admin));
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------

    private Long resolveAdminId(UserDetails admin) {
        // Username is the JWT subject; real ID lookup would use a port/service
        // For now return 0L as placeholder — wire to UserRepository when needed
        return 0L;
    }
}
