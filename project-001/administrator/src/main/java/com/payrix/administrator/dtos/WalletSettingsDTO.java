package com.payrix.administrator.dtos;

public class WalletSettingsDTO {
    private Long id;
    private Long walletId;
    private String password;
    private boolean isSecure;

    public WalletSettingsDTO(Long id, Long walletId, String password, boolean isSecure) {
        this.id = id;
        this.walletId = walletId;
        this.password = password;
        this.isSecure = isSecure;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
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
