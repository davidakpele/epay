package com.epay.wallet.cache.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletBalanceCacheEntry {

    private String walletId;
    private String currency;
    private String walletType;      
    private String walletStatus;   

    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;
    private BigDecimal holdBalance;
    private BigDecimal reservedBalance;
    private BigDecimal minimumBalance;

    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal dailySpent;
    private BigDecimal monthlySpent;

    private BigDecimal pendingDebit;
    private BigDecimal pendingCredit;

    private String lastTransactionId;
    private Long   transactionVersion;

    private String checksum;

    private Instant updatedAt;
}
