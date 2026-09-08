package com.epay.common.events.withdraw;

import com.epay.common.interfaces.IBlacklistPort;
import com.epay.common.interfaces.IHistoryReadPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.history.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserTransactionsAgent {


    @Value("${epay.risk.high-freq-count:20}")
    private int highFreqCount;

    @Value("${epay.risk.freq-window-minutes:60}")
    private int freqWindowMinutes;

    @Value("${epay.risk.high-volume-threshold:5000000}")
    private BigDecimal highVolumeThreshold;

    @Value("${epay.risk.volume-window-minutes:60}")
    private int volumeWindowMinutes;

    @Value("${epay.risk.rapid-cycle-min-outbound:3}")
    private int rapidCycleMinOutbound;

    @Value("${epay.risk.rapid-cycle-window-minutes:30}")
    private int rapidCycleWindowMinutes;

    @Value("${epay.risk.failed-tx-threshold:10}")
    private int failedTxThreshold;

    @Value("${epay.risk.new-account-hours:24}")
    private int newAccountHours;

    @Value("${epay.risk.min-account-age-minutes:30}")
    private int minAccountAgeMinutes;

    @Value("${epay.risk.soft-block-score:60}")
    private int softBlockScore;

   
    @Value("${epay.risk.hard-block-score:80}")
    private int hardBlockScore;

    private static final Set<String> HIGH_RISK_REGIONS = Set.of(
            "North Korea", "Iran", "Syria", "Cuba", "Russia"
    );

    private static final int FETCH_LIMIT = 200;

    private final UserRepository               userRepository;
    private final IHistoryReadPort             historyReadPort;
    private final IBlacklistPort               blacklistPort;
    private final IWalletNotificationPublisher notificationPublisher;

    public boolean isHighVolumeOrFrequentTransactions(Long userId, String email,
            String firstName, String lastName, Long walletId) {

        RiskScore score = evaluateRisk(userId, email, firstName, lastName, walletId);

        if (score.getTotal() >= softBlockScore) {
            log.warn("[Risk] SOFT_BLOCK userId={} score={} signals={}",
                    userId, score.getTotal(), score.getSignals());
            if (score.getTotal() < hardBlockScore) {

                sendSoftBlockNotification(email, firstName, lastName, score);
            }
            return true;
        }

        if (score.getTotal() > 0) {
            log.info("[Risk] ALLOW with signals userId={} score={} signals={}",
                    userId, score.getTotal(), score.getSignals());
        }
        return false;
    }

    public boolean isFraudulentBehavior(Long userId, String email,
            String firstName, String lastName, Long walletId) {

        RiskScore score = evaluateRisk(userId, email, firstName, lastName, walletId);

        if (score.getTotal() >= hardBlockScore) {
            log.warn("[Risk] HARD_BLOCK userId={} score={} signals={}",
                    userId, score.getTotal(), score.getSignals());
            sendHardBlockNotification(email, firstName, lastName, score);
            return true;
        }

        return false;
    }

    public boolean isNewAccountAndHighRisk(String username) {
        return userRepository.findByUsername(username).map(user -> {

            if (!user.isEnabled()) {
                log.info("[Risk.NewAccount] blocked — account not enabled username={}", username);
                return true;
            }

            if (user.getCreatedAt() == null) return false;

            LocalDateTime minAgeThreshold = LocalDateTime.now().minusMinutes(minAccountAgeMinutes);
            boolean tooNew = user.getCreatedAt().isAfter(minAgeThreshold);

            if (tooNew) {
                long minutesOld = java.time.Duration.between(
                        user.getCreatedAt(), LocalDateTime.now()).toMinutes();
                log.info("[Risk.NewAccount] blocked — account only {} min old, min required={} username={}",
                        minutesOld, minAccountAgeMinutes, username);
            }
            return tooNew;

        }).orElse(false); 
    }

    public RiskScore evaluateRisk(Long userId, String email,
            String firstName, String lastName, Long walletId) {

        RiskScore score = new RiskScore();

        if (blacklistPort.isAccountBlacklisted(userId)) {
            score.add(100, "BLACKLISTED_ACCOUNT");
            return score; 
        }

        List<TransactionDTO> recentAll = fetchRecent(userId, Math.max(freqWindowMinutes, volumeWindowMinutes));
        if (recentAll.isEmpty()) return score;
        LocalDateTime freqCutoff = LocalDateTime.now().minusMinutes(freqWindowMinutes);
        long debitCount = recentAll.stream()
                .filter(tx -> "DEBIT".equalsIgnoreCase(tx.getDebitCredit()))
                .filter(tx -> txCreatedAt(tx) != null && txCreatedAt(tx).isAfter(freqCutoff))
                .count();

        if (debitCount > highFreqCount) {
            int freqSignal = debitCount > (highFreqCount * 2) ? 25 : 15;
            score.add(freqSignal, "HIGH_FREQ_DEBITS:" + debitCount + "_in_" + freqWindowMinutes + "min");
        }

        LocalDateTime volCutoff = LocalDateTime.now().minusMinutes(volumeWindowMinutes);
        BigDecimal outboundTotal = recentAll.stream()
                .filter(tx -> "DEBIT".equalsIgnoreCase(tx.getDebitCredit()))
                .filter(tx -> txCreatedAt(tx) != null && txCreatedAt(tx).isAfter(volCutoff))
                .filter(tx -> tx.getAmount() != null && tx.getAmount().getGross() != null)
                .map(tx -> tx.getAmount().getGross())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (outboundTotal.compareTo(highVolumeThreshold) > 0) {
            int volSignal = outboundTotal.compareTo(highVolumeThreshold.multiply(BigDecimal.valueOf(2))) > 0
                    ? 25 : 15;
            score.add(volSignal, "HIGH_VOLUME_OUTBOUND:" + outboundTotal.toPlainString());
        }

        LocalDateTime cycleCutoff = LocalDateTime.now().minusMinutes(rapidCycleWindowMinutes);
        List<TransactionDTO> recentCycleWindow = recentAll.stream()
                .filter(tx -> txCreatedAt(tx) != null && txCreatedAt(tx).isAfter(cycleCutoff))
                .toList();

        boolean hasRecentDeposit = recentCycleWindow.stream()
                .anyMatch(tx -> "DEPOSIT".equalsIgnoreCase(tx.getTransactionType())
                        || "CREDIT".equalsIgnoreCase(tx.getDebitCredit()));

        long rapidOutboundCount = recentCycleWindow.stream()
                .filter(tx -> "DEBIT".equalsIgnoreCase(tx.getDebitCredit())
                        && !"FEE".equalsIgnoreCase(tx.getTransactionType()))
                .count();

        if (hasRecentDeposit && rapidOutboundCount >= rapidCycleMinOutbound) {
            score.add(30, "RAPID_CYCLE:" + rapidOutboundCount
                    + "_outbound_after_deposit_within_" + rapidCycleWindowMinutes + "min");
        }

        long failedCount = recentAll.stream()
                .filter(tx -> tx.getStatus() != null
                        && (tx.getStatus().name().equalsIgnoreCase("FAILED")
                            || tx.getStatus().name().equalsIgnoreCase("CANCELLED")))
                .filter(tx -> txCreatedAt(tx) != null && txCreatedAt(tx).isAfter(freqCutoff))
                .count();

        if (failedCount >= failedTxThreshold) {
            score.add(20, "EXCESSIVE_FAILURES:" + failedCount);
        }

        if (score.getTotal() > 0) {
            log.debug("[Risk] userId={} totalScore={} signals={}", userId, score.getTotal(), score.getSignals());
        }

        return score;
    }

    public boolean isFromBlacklistedAddress(Long userId) {
        boolean result = blacklistPort.isAccountBlacklisted(userId);
        if (result) log.warn("[Risk.Blacklist] userId={} is blacklisted", userId);
        return result;
    }

    public boolean isHighRiskRegion(String region) {
        boolean result = region != null && HIGH_RISK_REGIONS.contains(region);
        if (result) log.warn("[Risk.Region] high-risk region detected: {}", region);
        return result;
    }

    private void sendSoftBlockNotification(String email, String firstName,
            String lastName, RiskScore score) {
        try {
            notificationPublisher.publishBlockUserWallet(email, firstName, lastName,
                    "This transaction has been declined due to unusual activity on your account. "
                    + "If you believe this is a mistake, please contact our support team. "
                    + "Your account remains active and you can continue using ePay normally.");
        } catch (Exception e) {
            log.warn("[Risk] Soft-block notification failed for {}: {}", email, e.getMessage());
        }
    }

    private void sendHardBlockNotification(String email, String firstName,
            String lastName, RiskScore score) {
        try {
            notificationPublisher.publishBlockUserWallet(email, firstName, lastName,
                    "Your transaction has been flagged for security review and could not be processed. "
                    + "Our risk team will review your account within 24 hours. "
                    + "If you need immediate assistance, please contact support with reference: "
                    + "RISK-" + System.currentTimeMillis() + ". "
                    + "We apologise for any inconvenience.");
        } catch (Exception e) {
            log.warn("[Risk] Hard-block notification failed for {}: {}", email, e.getMessage());
        }
    }

    private List<TransactionDTO> fetchRecent(Long userId, int minutesBack) {
        try {
            return historyReadPort.findRecentByUserId(userId, FETCH_LIMIT).stream()
                    .filter(tx -> txCreatedAt(tx) != null
                            && txCreatedAt(tx).isAfter(LocalDateTime.now().minusMinutes(minutesBack)))
                    .toList();
        } catch (Exception e) {
            log.warn("[Risk] History fetch failed userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private static LocalDateTime txCreatedAt(TransactionDTO tx) {
        if (tx.getTimestamps() == null || tx.getTimestamps().getCreatedAt() == null) return null;
        return java.time.LocalDateTime.ofInstant(
                tx.getTimestamps().getCreatedAt(), java.time.ZoneOffset.UTC);
    }

    public static class RiskScore {
        private int total = 0;
        private final java.util.List<String> signals = new java.util.ArrayList<>();

        public void add(int points, String signal) {
            this.total = Math.min(100, this.total + points);
            this.signals.add(signal + "(+" + points + ")");
        }

        public int getTotal() { return total; }
        public java.util.List<String> getSignals() { return java.util.Collections.unmodifiableList(signals); }

        public boolean isAllow()     { return total < 30; }
        public boolean isReview()    { return total >= 30 && total < 60; }
        public boolean isSoftBlock() { return total >= 60 && total < 80; }
        public boolean isHardBlock() { return total >= 80; }

        @Override
        public String toString() {
            return "RiskScore{total=" + total + ", signals=" + signals + "}";
        }
    }
}
