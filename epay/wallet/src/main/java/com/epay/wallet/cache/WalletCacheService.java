package com.epay.wallet.cache;

import com.epay.domain.wallet.entity.CurrencyBalance;
import com.epay.domain.wallet.entity.Wallet;
import com.epay.wallet.cache.model.WalletBalanceCacheEntry;
import com.epay.wallet.cache.model.WalletCacheEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCacheService {

    private static final String KEY_PREFIX  = "wallet_cache:user:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, Object> redisTemplate;

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    public Optional<WalletCacheEntry> get(Long userId) {
        try {
            Object cached = redisTemplate.opsForValue().get(key(userId));
            if (cached instanceof WalletCacheEntry entry) {
                return Optional.of(entry);
            }
        } catch (Exception e) {
            log.warn("Redis read error for userId={}: {}", userId, e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<WalletBalanceCacheEntry> getWalletBalance(Long userId, String currencyCode) {
        return get(userId).map(entry -> entry.getWallet(currencyCode.toUpperCase()));
    }

    public void put(Long userId, WalletCacheEntry entry) {
        try {
            redisTemplate.opsForValue().set(key(userId), entry, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis write error for userId={}: {}", userId, e.getMessage());
        }
    }

    @Async
    public void updateBalance(Long userId, String currencyCode, BigDecimal newAvailableBalance,
                              BigDecimal newLedgerBalance, String lastTransactionId) {
        try {
            WalletCacheEntry userCache = get(userId).orElse(null);
            if (userCache == null) {
                log.debug("No cache entry for userId={}, skipping async update", userId);
                return;
            }

            String code = currencyCode.toUpperCase();
            WalletBalanceCacheEntry existing = userCache.getWallet(code);

            WalletBalanceCacheEntry updated;
            if (existing != null) {
                updated = WalletBalanceCacheEntry.builder()
                        .walletId(existing.getWalletId())
                        .currency(code)
                        .walletType(existing.getWalletType())
                        .walletStatus(existing.getWalletStatus())
                        .availableBalance(newAvailableBalance)
                        .ledgerBalance(newLedgerBalance)
                        .holdBalance(existing.getHoldBalance())
                        .reservedBalance(existing.getReservedBalance())
                        .minimumBalance(existing.getMinimumBalance())
                        .dailyLimit(existing.getDailyLimit())
                        .monthlyLimit(existing.getMonthlyLimit())
                        .dailySpent(existing.getDailySpent())
                        .monthlySpent(existing.getMonthlySpent())
                        .pendingDebit(existing.getPendingDebit())
                        .pendingCredit(existing.getPendingCredit())
                        .lastTransactionId(lastTransactionId)
                        .transactionVersion(existing.getTransactionVersion() + 1)
                        .checksum(buildChecksum(code, newAvailableBalance, newLedgerBalance))
                        .updatedAt(Instant.now())
                        .build();
            } else {
                updated = WalletBalanceCacheEntry.builder()
                        .currency(code)
                        .walletType("SECONDARY")
                        .walletStatus("ACTIVE")
                        .availableBalance(newAvailableBalance)
                        .ledgerBalance(newLedgerBalance)
                        .holdBalance(BigDecimal.ZERO)
                        .reservedBalance(BigDecimal.ZERO)
                        .dailySpent(BigDecimal.ZERO)
                        .monthlySpent(BigDecimal.ZERO)
                        .pendingDebit(BigDecimal.ZERO)
                        .pendingCredit(BigDecimal.ZERO)
                        .lastTransactionId(lastTransactionId)
                        .transactionVersion(1L)
                        .checksum(buildChecksum(code, newAvailableBalance, newLedgerBalance))
                        .updatedAt(Instant.now())
                        .build();
            }

            userCache.updateWalletBalance(code, updated);
            put(userId, userCache);

            log.debug("Cache updated: userId={} currency={} availableBalance={}",
                    userId, code, newAvailableBalance);

        } catch (Exception e) {
            log.error("Redis async update failed for userId={} currency={}: {}",
                    userId, currencyCode, e.getMessage());
        }
    }

    public WalletCacheEntry warmFromWallet(Long userId, Wallet wallet,
                                           String kycLevel, String defaultCurrency,
                                           List<String> supportedCurrencies) {
        WalletCacheEntry entry = WalletCacheEntry.builder()
                .userId(String.valueOf(userId))
                .kycLevel(kycLevel)
                .status("ACTIVE")
                .defaultCurrency(defaultCurrency)
                .supportedCurrencies(supportedCurrencies)
                .lastUpdated(Instant.now())
                .build();

        for (CurrencyBalance balance : wallet.getBalances()) {
            String code = balance.getCurrencyCode().toUpperCase();
            WalletBalanceCacheEntry cacheBalance = WalletBalanceCacheEntry.builder()
                    .walletId("WALLET_" + code + "_" + wallet.getId())
                    .currency(code)
                    .walletType(balance.isDefault() ? "PRIMARY" : "SECONDARY")
                    .walletStatus(wallet.isActive() ? "ACTIVE" : "FROZEN")
                    .availableBalance(balance.getBalance())
                    .ledgerBalance(balance.getBalance())
                    .holdBalance(BigDecimal.ZERO)
                    .reservedBalance(BigDecimal.ZERO)
                    .dailySpent(BigDecimal.ZERO)
                    .monthlySpent(BigDecimal.ZERO)
                    .pendingDebit(BigDecimal.ZERO)
                    .pendingCredit(BigDecimal.ZERO)
                    .transactionVersion(0L)
                    .checksum(buildChecksum(code, balance.getBalance(), balance.getBalance()))
                    .updatedAt(Instant.now())
                    .build();

            entry.updateWalletBalance(code, cacheBalance);
        }

        put(userId, entry);
        return entry;
    }

    public void evict(Long userId) {
        try {
            redisTemplate.delete(key(userId));
        } catch (Exception e) {
            log.warn("Redis evict error for userId={}: {}", userId, e.getMessage());
        }
    }

    private String buildChecksum(String currency, BigDecimal available, BigDecimal ledger) {
        try {
            String raw = currency + ":" + available.toPlainString() + ":" + ledger.toPlainString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(hash).substring(0, 10);
        } catch (Exception e) {
            return "sha256:unknown";
        }
    }
}
