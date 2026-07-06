package com.epay.wallet.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "wallet_settings")
@Data
public class WalletSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "is_secure", nullable = false)
    private boolean isSecure;


    public WalletSettings() {
    }


    public WalletSettings(Long id, Wallet wallet, String password, boolean isSecure) {
        this.id = id;
        this.wallet = wallet;
        this.password = password;
        this.isSecure = isSecure;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wallet getWallet() {
        return this.wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIsSecure() {
        return this.isSecure;
    }

    public boolean getIsSecure() {
        return this.isSecure;
    }

    public void setIsSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

}