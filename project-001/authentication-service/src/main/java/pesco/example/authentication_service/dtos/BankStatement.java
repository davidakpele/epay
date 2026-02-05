package pesco.example.authentication_service.dtos;

public class BankStatement {
    private String id;
    private String date;
    private String description;
    private String currencyType;
    private String type;
    private Double amount;
    private Double balance;
    private String reference;

    public BankStatement() {
    }

    public BankStatement(String id, String date, String description, String currencyType, String type, Double amount, Double balance, String reference) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.currencyType = currencyType;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.reference = reference;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getBalance() {
        return this.balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    
}

