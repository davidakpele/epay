package com.pesco.wallet_service.dtos;

import java.util.List;

public class WalletRestResponse {
    private Long id;
    private Long userId;
    private List<BalanceRestResponse> balances;
    private String createdOn;
    private String updatedOn;
    
    // constructors, getters, setters
    public WalletRestResponse() {}
    
    public WalletRestResponse(Long id, Long userId, List<BalanceRestResponse> balances, 
                            String createdOn, String updatedOn) {
        this.id = id;
        this.userId = userId;
        this.balances = balances;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<BalanceRestResponse> getBalances() {
        return this.balances;
    }

    public void setBalances(List<BalanceRestResponse> balances) {
        this.balances = balances;
    }

    public String getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }


}


