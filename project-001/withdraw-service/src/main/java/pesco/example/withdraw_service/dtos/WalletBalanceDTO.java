package pesco.example.withdraw_service.dtos;

public class WalletBalanceDTO {
    private String currency_code;
    private String symbol;
    private String balance;


    public WalletBalanceDTO() {
    }

    public WalletBalanceDTO(String currency_code, String symbol, String balance) {
        this.currency_code = currency_code;
        this.symbol = symbol;
        this.balance = balance;
    }

    public String getCurrency_code() {
        return this.currency_code;
    }

    public void setCurrency_code(String currency_code) {
        this.currency_code = currency_code;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

}