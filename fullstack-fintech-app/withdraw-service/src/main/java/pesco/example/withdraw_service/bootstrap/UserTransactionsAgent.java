package pesco.example.withdraw_service.bootstrap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    // Check if user has high volume or frequent transactions
    public boolean isHighVolumeOrFrequentTransactions(Long id, String email, String userFirstname, String userLastname, Long walletId, String token) {
        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(id,
                LocalDateTime.now().minusMinutes(10), token);

        if (recentTransactions == null || recentTransactions.isEmpty()) {
            return false;
        }

        BigDecimal totalAmount = recentTransactions.stream()
                .map(HistoryDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (recentTransactions.size() > 5 || totalAmount.compareTo(new BigDecimal("10000.00")) > 0) {
            blackListServiceClient.blockUserWallet(walletId, BanActions.FRAUDULENT_ACTIVITY, token);
            notificationServiceClient.blockUserWalletNotification(email, userFirstname, userLastname, 
                "Your wallet has been temporarily blocked due to suspicious activity. You recently performed multiple high-value or frequent transactions within a short time. Please contact support to verify your identity and restore access.");
            return true;
        }
        return false;
    }

    // Check if account is new and performing high-risk transactions
    public boolean isNewAccountAndHighRisk(String username, String token) {
        UserDTO user = userServiceClient.findByUsername(username, token);
        
        if (user == null)
            return false;
        return !user.isEnabled() || user.getCreatedOn().isAfter(LocalDateTime.now().minusMinutes(1));
    }

    // Check for fraudulent activity
    public boolean isFraudulentBehavior(Long userId, String email, String userFirstname, String userLastname, String token) {

        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(
                userId,
                LocalDateTime.now().minusHours(1),
                token
        );

        boolean hasDeposit = recentTransactions.stream()
                .anyMatch(tx -> tx.getType() == TransactionType.DEPOSIT);

        boolean hasTransfer = recentTransactions.stream()
                .anyMatch(tx -> tx.getType() == TransactionType.DEBITED);

        if (hasDeposit && hasTransfer) {

            // Block wallet
            blackListServiceClient.blockUserWallet(userId, BanActions.FRAUDULENT_ACTIVITY, token);

            // Block user account
            userServiceClient.blockUserAccount(userId, token);

            // Send notification to the user
            notificationServiceClient.blockUserWalletNotification(
                    email,
                    userFirstname, userLastname,
                    buildFraudAlertMessage()
            );

            return true;
        }

        return false;
    }


    // Check if user is from the black list users
    public boolean isFromBlacklistedAddress(Long id, String token) {
        Boolean exists = blackListServiceClient.FindByWalletId(id, token);
        return exists != null && exists;
    }

    // Transactions Involving High-Risk Regions
    public boolean isHighRiskRegion(String region) {
        return HIGH_RISK_REGIONS.contains(region);
    }

    private String buildFraudAlertMessage() {
        return "Your account has been temporarily blocked due to suspicious transaction activity. "
                + "Within a short period, a deposit and an outgoing transfer were detected, which violates our security policy. "
                + "For your protection, your account and wallet have been restricted. "
                + "Please contact support to verify your identity and restore access.";
    }

    

}
