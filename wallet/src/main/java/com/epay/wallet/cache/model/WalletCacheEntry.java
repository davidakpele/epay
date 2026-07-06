package com.epay.wallet.cache.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletCacheEntry {

    private String userId;
    private String kycLevel;
    private String status;
    private String defaultCurrency;
    private List<String> supportedCurrencies;
    private String country;
    private String timezone;
    private Instant lastLoginAt;
    private Instant lastUpdated;

    @Builder.Default
    private Map<String, WalletBalanceCacheEntry> wallets = new HashMap<>();

    public void updateWalletBalance(String currencyCode, WalletBalanceCacheEntry entry) {
        this.wallets.put(currencyCode.toUpperCase(), entry);
        this.lastUpdated = Instant.now();
    }

    public WalletBalanceCacheEntry getWallet(String currencyCode) {
        return this.wallets.get(currencyCode.toUpperCase());
    }

    public boolean hasWallet(String currencyCode) {
        return this.wallets.containsKey(currencyCode.toUpperCase());
    }
}
