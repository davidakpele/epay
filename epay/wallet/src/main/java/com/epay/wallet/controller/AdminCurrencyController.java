package com.epay.wallet.controller;

import com.epay.domain.wallet.input.CreateCurrencyRequest;
import com.epay.domain.wallet.input.UpdateCurrencyRequest;
import com.epay.wallet.service.CurrencyConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin — Currencies", description = "Configure and manage supported currencies and exchange rates")
@RestController
@RequestMapping("/admin/currencies")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class AdminCurrencyController {

    private final CurrencyConfigService currencyConfigService;

    @Operation(
        summary     = "List all currencies",
        description = "Returns every configured currency (active and inactive) with its exchange rate and metadata."
    )
    @GetMapping
    public ResponseEntity<?> getAllCurrencies() {
        return ResponseEntity.ok(currencyConfigService.getAll());
    }

    @Operation(
        summary     = "List active currencies",
        description = "Returns only currencies that are currently enabled and available to users."
    )
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCurrencies() {
        return ResponseEntity.ok(currencyConfigService.getAllActive());
    }

    @Operation(
        summary     = "Get a currency by code",
        description = "Returns configuration details for the given ISO-4217 currency code (e.g. USD, NGN)."
    )
    @GetMapping("/{code}")
    public ResponseEntity<?> getCurrency(@PathVariable String code) {
        return ResponseEntity.ok(currencyConfigService.getByCode(code));
    }

    @Operation(
        summary     = "Create a new currency",
        description = "Adds a new supported currency with its exchange rate. The currency will be added to all existing wallets automatically if defaultEligible is true."
    )
    @PostMapping
    public ResponseEntity<?> createCurrency(
            @Valid @RequestBody CreateCurrencyRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.status(201)
                .body(currencyConfigService.create(request, resolveAdminId(admin)));
    }

    @Operation(
        summary     = "Update a currency configuration",
        description = "Replaces the exchange rate, symbol, or eligibility flags for an existing currency."
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCurrencyRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(currencyConfigService.update(id, request, resolveAdminId(admin)));
    }

    @Operation(
        summary     = "Enable a currency",
        description = "Marks a currency as active so users can hold balances and transact in it."
    )
    @PatchMapping("/{id}/enable")
    public ResponseEntity<?> enableCurrency(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        currencyConfigService.setActive(id, true, resolveAdminId(admin));
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary     = "Disable a currency",
        description = "Marks a currency as inactive. Existing balances are preserved but no new transactions are permitted in this currency."
    )
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
