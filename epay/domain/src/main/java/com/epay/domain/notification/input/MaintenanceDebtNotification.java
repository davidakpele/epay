package com.epay.domain.notification.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MaintenanceDebtNotification {

    private String eventType;      
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private Long userId;
    private String currency;

    private BigDecimal feeAmount;
    private BigDecimal deductedAmount;
    private BigDecimal debtAmount;
    private BigDecimal walletBalance;
    private String billingMonth;

    private BigDecimal repaidAmount;
    private BigDecimal remainingDebt;
    private BigDecimal newWalletBalance;
    private Boolean fullySettled;

    @JsonCreator
    public MaintenanceDebtNotification(
            @JsonProperty("eventType")       String eventType,
            @JsonProperty("userEmail")       String userEmail,
            @JsonProperty("userFirstName")   String userFirstName,
            @JsonProperty("userLastName")    String userLastName,
            @JsonProperty("userId")          Long userId,
            @JsonProperty("currency")        String currency,
            @JsonProperty("feeAmount")       BigDecimal feeAmount,
            @JsonProperty("deductedAmount")  BigDecimal deductedAmount,
            @JsonProperty("debtAmount")      BigDecimal debtAmount,
            @JsonProperty("walletBalance")   BigDecimal walletBalance,
            @JsonProperty("billingMonth")    String billingMonth,
            @JsonProperty("repaidAmount")    BigDecimal repaidAmount,
            @JsonProperty("remainingDebt")   BigDecimal remainingDebt,
            @JsonProperty("newWalletBalance") BigDecimal newWalletBalance,
            @JsonProperty("fullySettled")    Boolean fullySettled) {
        this.eventType = eventType;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userId = userId;
        this.currency = currency;
        this.feeAmount = feeAmount;
        this.deductedAmount = deductedAmount;
        this.debtAmount = debtAmount;
        this.walletBalance = walletBalance;
        this.billingMonth = billingMonth;
        this.repaidAmount = repaidAmount;
        this.remainingDebt = remainingDebt;
        this.newWalletBalance = newWalletBalance;
        this.fullySettled = fullySettled;
    }

    public MaintenanceDebtNotification() {}
}
