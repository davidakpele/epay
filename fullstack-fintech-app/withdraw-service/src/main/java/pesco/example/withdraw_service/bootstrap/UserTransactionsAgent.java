package pesco.example.withdraw_service.bootstrap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import pesco.example.withdraw_service.clients.BlackListServiceClient;
import pesco.example.withdraw_service.clients.HistoryServiceClient;
import pesco.example.withdraw_service.clients.NotificationServiceClient;
import pesco.example.withdraw_service.clients.UserServiceClient;
import pesco.example.withdraw_service.dtos.HistoryDTO;
import pesco.example.withdraw_service.dtos.UserDTO;
import pesco.example.withdraw_service.enums.BanActions;
import pesco.example.withdraw_service.enums.TransactionType;

@Component
public class UserTransactionsAgent {

    private static final Logger log = LoggerFactory.getLogger(UserTransactionsAgent.class);

    private final UserServiceClient userServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final BlackListServiceClient blackListServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public UserTransactionsAgent(UserServiceClient userServiceClient,
            HistoryServiceClient historyServiceClient,
            BlackListServiceClient blackListServiceClient,
            NotificationServiceClient notificationServiceClient) {
        this.userServiceClient = userServiceClient;
        this.historyServiceClient = historyServiceClient;
        this.blackListServiceClient = blackListServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    private static final Set<String> HIGH_RISK_REGIONS = Set.of(
            "Philippines", "Venezuela", "Vietnam", "Yemen", "Haiti");

    // ─────────────────────────────────────────────────────────────────────────
    // Check if user has high volume or frequent transactions
    // ─────────────────────────────────────────────────────────────────────────
    public boolean isHighVolumeOrFrequentTransactions(Long id, String email, String userFirstname,
            String userLastname, Long walletId, String token) {

        String method = "isHighVolumeOrFrequentTransactions";
        long startTime = System.currentTimeMillis();
        log.info("[UserTransactionsAgent] [{}] START | userId={} | walletId={} | email={}", method, id, walletId, email);

        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(
                id, LocalDateTime.now().minusMinutes(10), token);

        if (recentTransactions == null || recentTransactions.isEmpty()) {
            log.info("[UserTransactionsAgent] [{}] RESULT=false | reason=no recent transactions | userId={} | duration={}ms",
                    method, id, System.currentTimeMillis() - startTime);
            return false;
        }

        log.debug("[UserTransactionsAgent] [{}] fetched {} transactions | userId={}", method, recentTransactions.size(), id);

        // ── NULL-SAFE reduction ───────────────────────────────────────────────
        // Filter out null entries AND null amounts before reducing.
        // Without this, BigDecimal::add throws NullPointerException when
        // any tx object or tx.getAmount() is null — even if you map inside,
        // the stream accumulator itself can receive null and crash at add().
        long nullTxCount = recentTransactions.stream().filter(tx -> tx == null).count();
        long nullAmountCount = recentTransactions.stream()
                .filter(tx -> tx != null && tx.getAmount() == null).count();

        if (nullTxCount > 0 || nullAmountCount > 0) {
            log.warn("[UserTransactionsAgent] [{}] DATA WARNING | userId={} | nullTransactions={} | nullAmounts={}",
                    method, id, nullTxCount, nullAmountCount);
        }

        BigDecimal totalAmount = recentTransactions.stream()
                .filter(tx -> tx != null)                    // skip null transaction objects
                .map(tx -> {
                    if (tx.getAmount() == null) {
                        log.warn("[UserTransactionsAgent] [{}] null amount on tx | userId={} | tx={}", method, id, tx);
                        return BigDecimal.ZERO;              // treat null amount as zero
                    }
                    return tx.getAmount();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);   // safe: identity + no nulls in stream

        log.debug("[UserTransactionsAgent] [{}] userId={} | txCount={} | totalAmount={}",
                method, id, recentTransactions.size(), totalAmount);

        boolean isHighVolume = recentTransactions.size() > 5
                || totalAmount.compareTo(new BigDecimal("1000000000.00")) > 0;

        if (isHighVolume) {
            log.warn("[UserTransactionsAgent] [{}] HIGH VOLUME DETECTED | userId={} | walletId={} | txCount={} | totalAmount={} | triggering block",
                    method, id, walletId, recentTransactions.size(), totalAmount);

            blackListServiceClient.blockUserWallet(walletId, BanActions.FRAUDULENT_ACTIVITY, token);
            notificationServiceClient.blockUserWalletNotification(email, userFirstname, userLastname,
                    "Your wallet has been temporarily blocked due to suspicious activity. "
                    + "You recently performed multiple high-value or frequent transactions within a short time. "
                    + "Please contact support to verify your identity and restore access.");

            log.info("[UserTransactionsAgent] [{}] RESULT=true | userId={} | walletId={} | duration={}ms",
                    method, id, walletId, System.currentTimeMillis() - startTime);
            return true;
        }

        log.info("[UserTransactionsAgent] [{}] RESULT=false | userId={} | txCount={} | totalAmount={} | duration={}ms",
                method, id, recentTransactions.size(), totalAmount, System.currentTimeMillis() - startTime);
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Check if account is new and performing high-risk transactions
    // ─────────────────────────────────────────────────────────────────────────
    public boolean isNewAccountAndHighRisk(String username, String token) {
        String method = "isNewAccountAndHighRisk";
        long startTime = System.currentTimeMillis();
        log.info("[UserTransactionsAgent] [{}] START | username={}", method, username);

        UserDTO user = userServiceClient.findByUsername(username, token);

        if (user == null) {
            log.warn("[UserTransactionsAgent] [{}] RESULT=false | reason=user not found | username={} | duration={}ms",
                    method, username, System.currentTimeMillis() - startTime);
            return false;
        }

        boolean isNewAndNotEnabled = !user.isEnabled();
        boolean isVeryNewAccount = user.getCreatedOn().isAfter(LocalDateTime.now().minusMinutes(1));
        boolean result = isNewAndNotEnabled || isVeryNewAccount;

        log.info("[UserTransactionsAgent] [{}] RESULT={} | username={} | enabled={} | createdOn={} | isVeryNew={} | duration={}ms",
                method, result, username, user.isEnabled(), user.getCreatedOn(), isVeryNewAccount,
                System.currentTimeMillis() - startTime);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Check for fraudulent activity (deposit + transfer within 1 hour)
    // ─────────────────────────────────────────────────────────────────────────
    public boolean isFraudulentBehavior(Long userId, String email, String userFirstname,
            String userLastname, String token) {

        String method = "isFraudulentBehavior";
        long startTime = System.currentTimeMillis();
        log.info("[UserTransactionsAgent] [{}] START | userId={} | email={}", method, userId, email);

        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(
                userId, LocalDateTime.now().minusHours(1), token);

        if (recentTransactions == null || recentTransactions.isEmpty()) {
            log.info("[UserTransactionsAgent] [{}] RESULT=false | reason=no recent transactions | userId={} | duration={}ms",
                    method, userId, System.currentTimeMillis() - startTime);
            return false;
        }

        log.debug("[UserTransactionsAgent] [{}] fetched {} transactions | userId={}", method, recentTransactions.size(), userId);

        boolean hasDeposit = recentTransactions.stream()
                .filter(tx -> tx != null)
                .anyMatch(tx -> tx.getType() == TransactionType.DEPOSIT);

        boolean hasTransfer = recentTransactions.stream()
                .filter(tx -> tx != null)
                .anyMatch(tx -> tx.getType() == TransactionType.DEBITED);

        log.debug("[UserTransactionsAgent] [{}] userId={} | hasDeposit={} | hasTransfer={}",
                method, userId, hasDeposit, hasTransfer);

        if (hasDeposit && hasTransfer) {
            log.warn("[UserTransactionsAgent] [{}] FRAUD DETECTED | userId={} | hasDeposit={} | hasTransfer={} | triggering block",
                    method, userId, hasDeposit, hasTransfer);

            blackListServiceClient.blockUserWallet(userId, BanActions.FRAUDULENT_ACTIVITY, token);
            userServiceClient.blockUserAccount(userId, token);
            notificationServiceClient.blockUserWalletNotification(email, userFirstname, userLastname, buildFraudAlertMessage());

            log.info("[UserTransactionsAgent] [{}] RESULT=true | userId={} | user and wallet blocked | duration={}ms",
                    method, userId, System.currentTimeMillis() - startTime);
            return true;
        }

        log.info("[UserTransactionsAgent] [{}] RESULT=false | userId={} | duration={}ms",
                method, userId, System.currentTimeMillis() - startTime);
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Check if user wallet is on the blacklist
    // ─────────────────────────────────────────────────────────────────────────
    public boolean isFromBlacklistedAddress(Long id, String token) {
        String method = "isFromBlacklistedAddress";
        long startTime = System.currentTimeMillis();
        log.info("[UserTransactionsAgent] [{}] START | walletId={}", method, id);

        Boolean exists = blackListServiceClient.FindByWalletId(id, token);
        boolean result = exists != null && exists;

        log.info("[UserTransactionsAgent] [{}] RESULT={} | walletId={} | duration={}ms",
                method, result, id, System.currentTimeMillis() - startTime);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Check if transaction involves a high-risk region
    // ─────────────────────────────────────────────────────────────────────────
    public boolean isHighRiskRegion(String region) {
        String method = "isHighRiskRegion";
        boolean result = HIGH_RISK_REGIONS.contains(region);
        log.info("[UserTransactionsAgent] [{}] RESULT={} | region={}", method, result, region);
        return result;
    }

    private String buildFraudAlertMessage() {
        return "Your account has been temporarily blocked due to suspicious transaction activity. "
                + "Within a short period, a deposit and an outgoing transfer were detected, which violates our security policy. "
                + "For your protection, your account and wallet have been restricted. "
                + "Please contact support to verify your identity and restore access.";
    }
}