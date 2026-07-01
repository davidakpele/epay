package com.pesco.wallet_service.controller;

import com.pesco.wallet_service.payloads.CurrencyRequest;
import com.pesco.wallet_service.repository.WalletRepository;
import com.pesco.wallet_service.services.CurrencyConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin REST API for managing supported currencies.
 *
 * <p>All mutating endpoints are restricted to ROLE_ADMIN / ROLE_SUPER_ADMIN.
 * The public GET endpoint listing enabled currencies is open to all authenticated users.
 *
 * <pre>
 * GET    /admin/currencies          — list all (admin only)
 * GET    /admin/currencies/enabled  — list enabled (any authenticated user)
 * GET    /admin/currencies/{code}   — get one (admin only)
 * POST   /admin/currencies          — create (admin only)
 * PUT    /admin/currencies/{code}   — update symbol/name/enabled (admin only)
 * PATCH  /admin/currencies/{code}/enable   — enable (admin only)
 * PATCH  /admin/currencies/{code}/disable  — disable (admin only)
 * DELETE /admin/currencies/{code}   — hard-delete (admin only)
 * </pre>
 */
@RestController
@RequestMapping("/admin/currencies")
public class CurrencyController {

    private final CurrencyConfigService currencyService;
    private final WalletRepository walletRepository;

    public CurrencyController(CurrencyConfigService currencyService,
                               WalletRepository walletRepository) {
        this.currencyService  = currencyService;
        this.walletRepository = walletRepository;
    }

    /** List all currencies (enabled + disabled). */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> listAll() {
        return currencyService.listAll();
    }

    /** List only enabled currencies — used by the frontend currency selector. */
    @GetMapping("/enabled")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> listEnabled() {
        return currencyService.listEnabled();
    }

    /** Get one currency by ISO code. */
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        return currencyService.getByCode(code);
    }

    /** Add a new currency. */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CurrencyRequest request) {
        return currencyService.create(request);
    }

    /** Update symbol, name, and/or enabled flag. Currency code is immutable. */
    @PutMapping("/{code}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable String code,
                                    @Valid @RequestBody CurrencyRequest request) {
        return currencyService.update(code, request);
    }

    /** Enable a previously disabled currency. */
    @PatchMapping("/{code}/enable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> enable(@PathVariable String code) {
        return currencyService.enable(code);
    }

    /** Disable a currency (hides it; existing balances are untouched). */
    @PatchMapping("/{code}/disable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> disable(@PathVariable String code) {
        return currencyService.disable(code);
    }

    /**
     * Hard-delete a currency.
     * Rejected if any wallet still holds a balance row for this currency code,
     * because removing it would leave orphaned data.
     */
    @DeleteMapping("/{code}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String code) {
        // Safety check: reject if any wallet_balances row references this code
        boolean inUse = walletRepository.existsByCurrencyCode(code.toUpperCase());
        if (inUse) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("message", "Cannot delete " + code.toUpperCase()
                    + " — it is still referenced by existing wallet balances. "
                    + "Disable it instead.");
            return ResponseEntity.badRequest().body(body);
        }
        return currencyService.delete(code);
    }
}
