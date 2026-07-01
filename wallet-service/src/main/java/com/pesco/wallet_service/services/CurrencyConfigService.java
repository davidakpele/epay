package com.pesco.wallet_service.services;

import com.pesco.wallet_service.models.SupportedCurrency;
import com.pesco.wallet_service.payloads.CurrencyRequest;
import com.pesco.wallet_service.repository.SupportedCurrencyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the set of currencies the platform supports.
 *
 * <p>On first startup the service seeds the database with the original 10
 * currencies that were previously hardcoded as the {@code Currency} enum.
 * After that, admins can add / update / enable / disable currencies via the
 * REST API without touching any code.
 */
@Service
public class CurrencyConfigService {

    private final SupportedCurrencyRepository repo;

    public CurrencyConfigService(SupportedCurrencyRepository repo) {
        this.repo = repo;
    }

    // ── Startup seed ──────────────────────────────────────────────────────────

    /**
     * Seeds the original 10 currencies on first run.
     * Skipped on subsequent starts because {@code existsByCodeIgnoreCase} returns true.
     */
    @PostConstruct
    @Transactional
    public void seedDefaultCurrencies() {
        Object[][] defaults = {
            { "USD", "$",   "US Dollar"            },
            { "EUR", "€",   "Euro"                 },
            { "NGN", "₦",   "Nigerian Naira"       },
            { "GBP", "£",   "British Pound"        },
            { "JPY", "¥",   "Japanese Yen"         },
            { "AUD", "A$",  "Australian Dollar"    },
            { "CAD", "C$",  "Canadian Dollar"      },
            { "CHF", "Fr",  "Swiss Franc"          },
            { "CNY", "¥",   "Chinese Yuan"         },
            { "INR", "₹",   "Indian Rupee"         },
        };
        for (Object[] row : defaults) {
            String code = (String) row[0];
            if (!repo.existsByCodeIgnoreCase(code)) {
                repo.save(new SupportedCurrency(code, (String) row[1], (String) row[2]));
            }
        }
    }

    // ── Public lookup helpers used by WalletService / AccountWrapper ─────────

    /** Returns all enabled currencies (used when creating a wallet). */
    public List<SupportedCurrency> getEnabledCurrencies() {
        return repo.findAllEnabled();
    }

    /**
     * Returns the symbol for a given currency code.
     * Falls back to {@code "?"} if the code is unknown — safe for existing data.
     */
    public String getSymbol(String code) {
        return repo.findByCodeIgnoreCase(code)
                   .map(SupportedCurrency::getSymbol)
                   .orElse("?");
    }

    /**
     * Returns {@code true} if the code belongs to an enabled currency.
     * Used for input validation in {@link com.pesco.wallet_service.services.WalletService}.
     */
    public boolean isSupported(String code) {
        return repo.findByCodeIgnoreCase(code)
                   .map(SupportedCurrency::isEnabled)
                   .orElse(false);
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    /** List all currencies (enabled + disabled). Admin view. */
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(repo.findAllOrdered());
    }

    /** List only enabled currencies. Public / user-facing. */
    public ResponseEntity<?> listEnabled() {
        return ResponseEntity.ok(repo.findAllEnabled());
    }

    /** Get one currency by code. */
    public ResponseEntity<?> getByCode(String code) {
        Optional<SupportedCurrency> opt = repo.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) {
            return notFound("Currency not found: " + code.toUpperCase());
        }
        return ResponseEntity.ok(opt.get());
    }

    /** Add a brand-new currency. Rejects duplicates. */
    @Transactional
    public ResponseEntity<?> create(CurrencyRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (repo.existsByCodeIgnoreCase(code)) {
            return conflict("Currency already exists: " + code);
        }

        SupportedCurrency currency = new SupportedCurrency(
                code,
                request.getSymbol().trim(),
                request.getName().trim()
        );
        if (request.getEnabled() != null) {
            currency.setEnabled(request.getEnabled());
        }

        SupportedCurrency saved = repo.save(currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Update symbol, name, and/or enabled flag. Code (PK) is immutable. */
    @Transactional
    public ResponseEntity<?> update(String code, CurrencyRequest request) {
        Optional<SupportedCurrency> opt = repo.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) {
            return notFound("Currency not found: " + code.toUpperCase());
        }

        SupportedCurrency currency = opt.get();

        if (request.getSymbol() != null && !request.getSymbol().isBlank()) {
            currency.setSymbol(request.getSymbol().trim());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            currency.setName(request.getName().trim());
        }
        if (request.getEnabled() != null) {
            currency.setEnabled(request.getEnabled());
        }

        return ResponseEntity.ok(repo.save(currency));
    }

    /** Enable a currency (makes it available for new wallets and transactions). */
    @Transactional
    public ResponseEntity<?> enable(String code) {
        return setEnabled(code, true);
    }

    /** Disable a currency (hides it from the UI; existing balances untouched). */
    @Transactional
    public ResponseEntity<?> disable(String code) {
        return setEnabled(code, false);
    }

    /**
     * Hard-delete a currency.
     * <strong>Only safe if no wallet_balances rows reference this code.</strong>
     * The controller enforces this constraint by checking wallet data first.
     */
    @Transactional
    public ResponseEntity<?> delete(String code) {
        Optional<SupportedCurrency> opt = repo.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) {
            return notFound("Currency not found: " + code.toUpperCase());
        }
        repo.delete(opt.get());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Currency " + code.toUpperCase() + " deleted.");
        return ResponseEntity.ok(body);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<?> setEnabled(String code, boolean enabled) {
        Optional<SupportedCurrency> opt = repo.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) {
            return notFound("Currency not found: " + code.toUpperCase());
        }
        SupportedCurrency c = opt.get();
        c.setEnabled(enabled);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("currency", repo.save(c));
        body.put("message", "Currency " + code.toUpperCase() + (enabled ? " enabled." : " disabled."));
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<?> notFound(String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    private ResponseEntity<?> conflict(String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
