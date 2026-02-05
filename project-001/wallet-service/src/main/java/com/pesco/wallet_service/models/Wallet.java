package com.pesco.wallet_service.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wallet_balances", joinColumns = @JoinColumn(name = "wallet_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code")),
            @AttributeOverride(name = "currencySymbol", column = @Column(name = "currency_symbol")),
            @AttributeOverride(name = "balance", column = @Column(name = "balance"))
    })
    private List<CurrencyBalanceMapStruct> balances = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdOn;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    // Default constructor
    public Wallet() {
    }

    // Constructor with parameters
    public Wallet(Long id, Long userId, List<CurrencyBalanceMapStruct> balances, 
                  LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.userId = userId;
        this.balances = balances;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CurrencyBalanceMapStruct> getBalances() {
        return balances;
    }

    public void setBalances(List<CurrencyBalanceMapStruct> balances) {
        this.balances = balances;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    // Helper methods to add/remove balances
    public void addBalance(CurrencyBalanceMapStruct balance) {
        if (this.balances == null) {
            this.balances = new ArrayList<>();
        }
        this.balances.add(balance);
    }

    public void removeBalance(CurrencyBalanceMapStruct balance) {
        if (this.balances != null) {
            this.balances.remove(balance);
        }
    }

    // toString method
    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", userId=" + userId +
                ", balances=" + balances +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wallet wallet = (Wallet) o;

        return id != null ? id.equals(wallet.id) : wallet.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
