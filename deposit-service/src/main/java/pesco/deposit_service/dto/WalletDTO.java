package pesco.deposit_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WalletDTO {
    @JsonProperty("walletId")
    private Long id;
    
    @JsonProperty("wallet_balances")
    private List<BalanceDTO> balances;
    
    private Long userId;
    
    @JsonProperty("hasTransferPin")
    private Boolean hasTransferPin;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BalanceDTO> getBalances() {
        return this.balances;
    }

    public void setBalances(List<BalanceDTO> balances) {
        this.balances = balances;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getHasTransferPin() {
        return this.hasTransferPin;
    }

    public void setHasTransferPin(Boolean hasTransferPin) {
        this.hasTransferPin = hasTransferPin;
    }

    public static class BalanceDTO {
        @JsonProperty("currency_code")
        private String currencyCode;
        
        @JsonProperty("symbol")
        private String currencySymbol;
        
        @JsonProperty("balance")
        private String balance;

        // ADD GETTERS AND SETTERS
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
}