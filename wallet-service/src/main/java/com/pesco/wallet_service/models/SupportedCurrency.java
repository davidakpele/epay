package com.pesco.wallet_service.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a currency that the platform supports.
 * Replaces the hardcoded {@code Currency} enum so currencies can be
 * added, updated, and deactivated at runtime via the admin API.
 *
 * DB table: {@code supported_currencies}
 */
@Entity
@Table(name = "supported_currencies",
        uniqueConstraints = @UniqueConstraint(name = "uq_currency_code", columnNames = "code"))
public class SupportedCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ISO 4217 currency code, e.g. "USD", "NGN". Always stored upper-case. */
    @Column(nullable = false, length = 10)
    private String code;

    /** Unicode symbol, e.g. "$", "₦". */
    @Column(nullable = false, length = 10)
    private String symbol;

    /** Human-readable name, e.g. "US Dollar", "Nigerian Naira". */
    @Column(nullable = false, length = 60)
    private String name;

    /**
     * When {@code false} the currency is hidden from the UI and new wallets
     * will not include it, but existing balances are left untouched.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public SupportedCurrency() {}

    public SupportedCurrency(String code, String symbol, String name) {
        this.code   = code.toUpperCase();
        this.symbol = symbol;
        this.name   = name;
        this.enabled = true;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getCode()                  { return code; }
    public void   setCode(String code)       { this.code = code != null ? code.toUpperCase() : null; }

    public String getSymbol()                { return symbol; }
    public void   setSymbol(String symbol)   { this.symbol = symbol; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public boolean isEnabled()               { return enabled; }
    public void    setEnabled(boolean v)     { this.enabled = v; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
