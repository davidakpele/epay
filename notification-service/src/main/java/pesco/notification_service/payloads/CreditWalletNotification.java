package pesco.notification_service.payloads;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreditWalletNotification {
    @NotBlank(message = "Recipient email is mandatory")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    @NotNull(message = "Transfer amount is required")
    @Positive(message = "Transfer amount must be positive")
    private BigDecimal transferAmount;

    @NotBlank(message = "Sender's full name is mandatory")
    private String senderFullName;

    @NotBlank(message = "Receiver's full name is mandatory")
    private String receiverFullName;

    @NotNull(message = "Recipient's total balance is required")
    private BigDecimal recipientTotalBalance;
    
    @NotBlank(message = "Currency is mandatory")
    private String currency;

    @NotBlank(message = "Transaction Id is mandatory")
    private String transactionId;

    @NotNull(message = "Previous balance is required")
    private BigDecimal previousBalance;
    // Default constructor
    public CreditWalletNotification() {
    }

    // Constructor to map from JSON
    @JsonCreator
    public CreditWalletNotification(
            @JsonProperty("recipientEmail") String recipientEmail,
            @JsonProperty("transferAmount") BigDecimal transferAmount,
            @JsonProperty("senderFullName") String senderFullName,
            @JsonProperty("receiverFullName") String receiverFullName,
            @JsonProperty("recipientTotalBalance") BigDecimal recipientTotalBalance,
            @JsonProperty("currency") String currency,
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("previousBalance") BigDecimal previousBalance) {
        this.recipientEmail = recipientEmail;
        this.transferAmount = transferAmount;
        this.senderFullName = senderFullName;
        this.receiverFullName = receiverFullName;
        this.recipientTotalBalance = recipientTotalBalance;
        this.currency = currency;
        this.transactionId = transactionId;
        this.previousBalance = previousBalance;
    }


    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public BigDecimal getTransferAmount() {
        return this.transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getSenderFullName() {
        return this.senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getReceiverFullName() {
        return this.receiverFullName;
    }

    public void setReceiverFullName(String receiverFullName) {
        this.receiverFullName = receiverFullName;
    }

    public BigDecimal getRecipientTotalBalance() {
        return this.recipientTotalBalance;
    }

    public void setRecipientTotalBalance(BigDecimal recipientTotalBalance) {
        this.recipientTotalBalance = recipientTotalBalance;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getPreviousBalance() {
        return this.previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

}

