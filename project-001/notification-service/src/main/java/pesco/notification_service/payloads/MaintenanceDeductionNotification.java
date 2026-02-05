package pesco.notification_service.payloads;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MaintenanceDeductionNotification {

    @NotBlank(message = "Action type is mandatory")
    private String actionType;

    @NotNull(message = "Available balance is mandatory")
    private BigDecimal availableBalance;

    @NotBlank(message = "Currency is mandatory")
    private String currency;

    @NotNull(message = "Fee amount is mandatory")
    @Positive(message = "Fee amount must be positive")
    private BigDecimal feeAmount;

    @NotNull(message = "Previous balance is mandatory")
    private BigDecimal previousBalance;

    @NotBlank(message = "Reason is mandatory")
    private String reason;

    @NotNull(message = "Success flag is mandatory")
    private Boolean success;

    @NotNull(message = "Timestamp is mandatory")
    private OffsetDateTime timestamp;

    @NotNull(message = "Total amount spent is mandatory")
    private BigDecimal totalAmountSpent;

    @NotBlank(message = "User email is mandatory")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotBlank(message = "User first name is mandatory")
    private String userFirstName;

    @NotNull(message = "User ID is mandatory")
    private Long userId;

    @NotBlank(message = "User last name is mandatory")
    private String userLastName;

    @JsonCreator
    public MaintenanceDeductionNotification(
            @JsonProperty("actionType") String actionType,
            @JsonProperty("availableBalance") BigDecimal availableBalance,
            @JsonProperty("currency") String currency,
            @JsonProperty("feeAmount") BigDecimal feeAmount,
            @JsonProperty("previousBalance") BigDecimal previousBalance,
            @JsonProperty("reason") String reason,
            @JsonProperty("success") Boolean success,
            @JsonProperty("timestamp") OffsetDateTime timestamp,
            @JsonProperty("totalAmountSpent") BigDecimal totalAmountSpent,
            @JsonProperty("userEmail") String userEmail,
            @JsonProperty("userFirstName") String userFirstName,
            @JsonProperty("userId") Long userId,
            @JsonProperty("userLastName") String userLastName) {

        this.actionType = actionType;
        this.availableBalance = availableBalance;
        this.currency = currency;
        this.feeAmount = feeAmount;
        this.previousBalance = previousBalance;
        this.reason = reason;
        this.success = success;
        this.timestamp = timestamp;
        this.totalAmountSpent = totalAmountSpent;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.userId = userId;
        this.userLastName = userLastName;
    }

    public MaintenanceDeductionNotification() {}

    public String getActionType() {
        return this.actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public BigDecimal getAvailableBalance() {
        return this.availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getFeeAmount() {
        return this.feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getPreviousBalance() {
        return this.previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTotalAmountSpent() {
        return this.totalAmountSpent;
    }

    public void setTotalAmountSpent(BigDecimal totalAmountSpent) {
        this.totalAmountSpent = totalAmountSpent;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return this.userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLastName() {
        return this.userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

}
