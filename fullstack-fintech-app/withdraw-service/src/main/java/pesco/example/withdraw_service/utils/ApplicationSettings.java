package pesco.example.withdraw_service.utils;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class ApplicationSettings {

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    @Value("${revenue-service.base-url}")
    private String revenueServiceBaseUrl;

    @Value("${history-service.base-url}")
    private String historyServiceBaseUrl;

    @Value("${wallet-service.base-url}")
    private String walletServiceBaseUrl;

    @Value("${notification-service.base-url}")
    private String notificationServiceBaseUrl;

    @Value("${banklist-service.base-url}")
    private String banklistServiceBaseUrl;

    @Value("${blacklist-service.base-url}")
    private String blacklistServiceUrl;

    // Public getter methods
    public String getAuthServiceBaseUrl() {
        return authServiceBaseUrl;
    }

    public String getRevenueServiceBaseUrl() {
        return revenueServiceBaseUrl;
    }

    public String getHistoryServiceBaseUrl() {
        return historyServiceBaseUrl;
    }

    public String getWalletServiceBaseUrl() {
        return walletServiceBaseUrl;
    }

    public String getNotificationBaseUrl() {
        return notificationServiceBaseUrl;
    }

    public String banklistBaseUrl() {
        return banklistServiceBaseUrl;
    }

    public String blackListBaseUrl() {
        return blacklistServiceUrl;
    }
}
