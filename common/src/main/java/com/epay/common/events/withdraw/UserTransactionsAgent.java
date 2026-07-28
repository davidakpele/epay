package com.epay.common.events.withdraw;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.epay.common.interfaces.IBlacklistPort;
import com.epay.common.interfaces.IHistoryReadPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.history.dto.TransactionDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserTransactionsAgent {

    private static final Set<String> HIGH_RISK_REGIONS     = Set.of(
            "Philippines", "Venezuela", "Vietnam", "Yemen", "Haiti");
    private static final BigDecimal  HIGH_VOLUME_THRESHOLD  = new BigDecimal("1000000000.00");
    private static final int         HIGH_FREQ_COUNT        = 5;
    private static final int         RECENT_FETCH_LIMIT     = 100;

    private final UserRepository               userRepository;
    private final IHistoryReadPort             historyReadPort;
    private final IBlacklistPort               blacklistPort;
    private final IWalletNotificationPublisher notificationPublisher;

    public boolean isHighVolumeOrFrequentTransactions(Long userId, String email,
            String firstName, String lastName, Long walletId) {

        log.info("[Agent.HighVolume] START userId={} walletId={}", userId, walletId);

        List<TransactionDTO> recent = fetchRecent(userId, 10);
        if (recent.isEmpty()) {
            log.info("[Agent.HighVolume] RESULT=false — no recent tx userId={}", userId);
            return false;
        }

        BigDecimal totalAmount = recent.stream()
                .filter(tx -> tx.getAmount() != null && tx.getAmount().getGross() != null)
                .map(tx -> tx.getAmount().getGross())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        boolean triggered = recent.size() > HIGH_FREQ_COUNT
                || totalAmount.compareTo(HIGH_VOLUME_THRESHOLD) > 0;

        if (triggered) {
            log.warn("[Agent.HighVolume] TRIGGERED userId={} walletId={} txCount={} total={}",
                    userId, walletId, recent.size(), totalAmount);
            notifyBlock(email, firstName, lastName,
                    "Your wallet has been temporarily blocked due to suspicious high-volume activity. "
                    + "Multiple high-value or frequent transactions were detected within a short period. "
                    + "Please contact support to restore access.");
            return true;
        }

        log.info("[Agent.HighVolume] RESULT=false userId={} txCount={} total={}",
                userId, recent.size(), totalAmount);
        return false;
    }

    public boolean isNewAccountAndHighRisk(String username) {
        log.info("[Agent.NewAccount] START username={}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    boolean notEnabled = !user.isEnabled();
                    boolean veryNew    = user.getCreatedAt() != null
                            && user.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1));
                    boolean result = notEnabled || veryNew;
                    log.info("[Agent.NewAccount] RESULT={} username={} enabled={} createdAt={}",
                            result, username, user.isEnabled(), user.getCreatedAt());
                    return result;
                })
                .orElseGet(() -> {
                    log.warn("[Agent.NewAccount] user not found username={}", username);
                    return false;
                });
    }


    public boolean isFraudulentBehavior(Long userId, String email,
            String firstName, String lastName, Long walletId) {

        log.info("[Agent.Fraud] START userId={}", userId);

        List<TransactionDTO> recent = fetchRecent(userId, 60);
        if (recent.isEmpty()) {
            log.info("[Agent.Fraud] RESULT=false — no recent tx userId={}", userId);
            return false;
        }

        boolean hasDeposit = recent.stream()
                .anyMatch(tx -> "DEPOSIT".equalsIgnoreCase(tx.getTransactionType()));

        boolean hasOutbound = recent.stream()
                .anyMatch(tx -> "DEBIT".equalsIgnoreCase(tx.getDebitCredit())
                        && !"DEPOSIT".equalsIgnoreCase(tx.getTransactionType()));

        if (hasDeposit && hasOutbound) {
            log.warn("[Agent.Fraud] TRIGGERED userId={} — deposit + outbound within 1h", userId);

            // Lock the account in DB
            userRepository.lockAccount(userId, LocalDateTime.now(),
                    "Auto-locked: deposit-and-transfer fraud pattern detected");

            notifyBlock(email, firstName, lastName,
                    "Your account has been blocked due to suspicious activity. "
                    + "A deposit followed immediately by an outgoing transfer was detected, "
                    + "which violates our security policy. "
                    + "Please contact support to verify your identity and restore access.");
            return true;
        }

        log.info("[Agent.Fraud] RESULT=false userId={} hasDeposit={} hasOutbound={}",
                userId, hasDeposit, hasOutbound);
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Blacklisted address
    // ─────────────────────────────────────────────────────────────────────────

    public boolean isFromBlacklistedAddress(Long userId) {
        boolean result = blacklistPort.isAccountBlacklisted(userId);
        log.info("[Agent.Blacklist] userId={} result={}", userId, result);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. High-risk region
    // ─────────────────────────────────────────────────────────────────────────

    public boolean isHighRiskRegion(String region) {
        boolean result = region != null && HIGH_RISK_REGIONS.contains(region);
        log.info("[Agent.Region] region={} result={}", region, result);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches the latest RECENT_FETCH_LIMIT transactions and filters
     * to those within the last {@code minutesBack} minutes.
     */
    private List<TransactionDTO> fetchRecent(Long userId, int minutesBack) {
        try {
            List<TransactionDTO> all = historyReadPort.findRecentByUserId(userId, RECENT_FETCH_LIMIT);
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesBack);
            return all.stream()
                    .filter(tx -> tx.getCreatedAt() != null
                            && tx.getCreatedAt().isAfter(cutoff))
                    .toList();
        } catch (Exception e) {
            log.warn("[Agent] History fetch failed userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private void notifyBlock(String email, String firstName, String lastName, String message) {
        try {
            notificationPublisher.publishBlockUserWallet(email, firstName, lastName, message);
        } catch (Exception e) {
            log.warn("[Agent] Block notification failed for {}: {}", email, e.getMessage());
        }
    }
}
