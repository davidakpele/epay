package pesco.example.authentication_service.dtos;

import java.math.BigDecimal;
import java.util.List;

public class WalletDTO {
    private Long id;
    private List<BalanceDTO> balances;
    private Long userId;

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

    public void setUser(Long userId) {
        this.userId = userId;
    }

    public List<BalanceDTO> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceDTO> balances) {
        this.balances = balances;
    }

    public static class BalanceDTO {
        private String currencyCode;
        private String currencySymbol;
        private BigDecimal balance;

        // Getters and Setters
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

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }
}
