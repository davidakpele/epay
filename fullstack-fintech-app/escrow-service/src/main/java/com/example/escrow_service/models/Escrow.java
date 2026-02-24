package com.example.escrow_service.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "escrow")
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "escrow_balances", joinColumns = @JoinColumn(name = "escrow_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code")),
            @AttributeOverride(name = "currencySymbol", column = @Column(name = "currency_symbol")),
            @AttributeOverride(name = "balance", column = @Column(name = "balance"))
    })
    private List<CurrencyBalance> balances = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdOn;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    public Escrow() {
    }

    public Escrow(Long id, List<CurrencyBalance> balances, 
                  LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.balances = balances;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CurrencyBalance> getBalances() {
        return balances;
    }

    public void setBalances(List<CurrencyBalance> balances) {
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

    public void addBalance(CurrencyBalance balance) {
        if (this.balances == null) {
            this.balances = new ArrayList<>();
        }
        this.balances.add(balance);
    }

    public void removeBalance(CurrencyBalance balance) {
        if (this.balances != null) {
            this.balances.remove(balance);
        }
    }

    // toString method
    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", balances=" + balances +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Escrow wallet = (Escrow) o;

        return id != null ? id.equals(wallet.id) : wallet.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
