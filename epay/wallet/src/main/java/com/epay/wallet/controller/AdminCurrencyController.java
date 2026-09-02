package com.epay.wallet.controller;

import com.epay.domain.wallet.input.CreateCurrencyRequest;
import com.epay.domain.wallet.input.UpdateCurrencyRequest;
import com.epay.wallet.service.CurrencyConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Admin currency configuration management.
 *
 * Base: /admin/currencies
 *
 * Handles the SupportedCurrency catalogue — adding new currencies,
 * enabling/disabling them, and viewing current state.
 * Freeze/unfreeze is handled by the admin module AdminWalletController.
 */
@RestController
@RequestMapping("/admin/currencies")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class AdminCurrencyController {

    private final CurrencyConfigService currencyConfigService;

    @GetMapping
    public ResponseEntity<?> getAllCurrencies() {
        return ResponseEntity.ok(currencyConfigService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCurrencies() {
        return ResponseEntity.ok(currencyConfigService.getAllActive());
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getCurrency(@PathVariable String code) {
        return ResponseEntity.ok(currencyConfigService.getByCode(code));
    }

    @PostMapping
    public ResponseEntity<?> createCurrency(
            @Valid @RequestBody CreateCurrencyRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.status(201)
                .body(currencyConfigService.create(request, resolveAdminId(admin)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCurrencyRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(currencyConfigService.update(id, request, resolveAdminId(admin)));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<?> enableCurrency(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        currencyConfigService.setActive(id, true, resolveAdminId(admin));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableCurrency(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        currencyConfigService.setActive(id, false, resolveAdminId(admin));
        return ResponseEntity.ok().build();
    }

    private Long resolveAdminId(UserDetails admin) {
        return 0L;
    }
}
