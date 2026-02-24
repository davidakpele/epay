package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;

import com.pesco.wallet_service.enums.Currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    // transaction payloads
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
    private String description;
    private String region;
    private Currency currencyType;
    private String transferpin;

    // credit crypto user payloads
    private Long recipientUserId;
    private Long creditorUserId;
    private BigDecimal profit;
}