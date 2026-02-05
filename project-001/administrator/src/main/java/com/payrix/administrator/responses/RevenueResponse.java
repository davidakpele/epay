package com.payrix.administrator.responses;

import java.math.BigDecimal;
import java.util.List;

public class RevenueResponse {
    private Long id;
    private List<Balance> balances;
    
    public BigDecimal getTotalRevenue() {
        if (balances == null || balances.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return balances.stream()
            .map(b -> new BigDecimal(b.getBalance()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public static class Balance {
        private Long id;
        private Long revenueId;
        private String currencyCode;
        private String currencySymbol;
        private String balance;
        
        // Getters and Setters for Balance
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public Long getRevenueId() {
            return revenueId;
        }
        
        public void setRevenueId(Long revenueId) {
            this.revenueId = revenueId;
        }
        
        public String getCurrencyCode() {
            return currencyCode;
        }
        
        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }
        
        public String getCurrencySymbol() {
            return currencySymbol;
        }
        
        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }
        
        public String getBalance() {
            return balance;
        }
        
        public void setBalance(String balance) {
            this.balance = balance;
        }
    }
    
    // Getters and Setters for RevenueResponse
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public List<Balance> getBalances() {
        return balances;
    }
    
    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }
}