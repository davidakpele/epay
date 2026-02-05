package pesco.example.authentication_service.servicesImplementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.authentication_service.exceptions.ResourceNotFoundException;
import pesco.example.authentication_service.models.UserAccountSettings;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.repositories.UserAccountSettingsRepository;
import pesco.example.authentication_service.repositories.UsersRepository;

@Service
public class UserAccountSettingsService {

    @Autowired
    private UserAccountSettingsRepository settingsRepository;

    @Autowired
    private UsersRepository usersRepository;

    /* Validate user exists */
    private Users getUser(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found for user ID: " + userId));
    }

    @Transactional
    public UserAccountSettings updateBiometricStatus(Long userId, boolean enableBiometric) {
        getUser(userId);
        UserAccountSettings settings = getOrCreateSettings(userId);
        settings.setIsBiometric(enableBiometric);
        return settingsRepository.save(settings);
    }

    @Transactional
    public UserAccountSettings updateSessionTimeout(Long userId, String sessionTimeout) {
        getUser(userId);
        UserAccountSettings settings = getOrCreateSettings(userId);
        settings.setSessionTimeOut(sessionTimeout);
        return settingsRepository.save(settings);
    }


    @Transactional
    public UserAccountSettings updateNotificationSettings(Long userId, NotificationUpdateRequest request) {
        getUser(userId);
        UserAccountSettings settings = getOrCreateSettings(userId);

        if (request.getEmail() != null)
            settings.setIsEmailAlert(request.getEmail());

        if (request.getTransactionAlerts() != null)
            settings.setIsTransactionAlert(request.getTransactionAlerts());

        if (request.getLoginAlerts() != null)
            settings.setIsLoginAlert(request.getLoginAlerts());

        if (request.getMarketingEmails() != null)
            settings.setIsReceiveMarketingNews(request.getMarketingEmails());

        if (request.getSms() != null)
            settings.setIsReceiveSmsMessage(request.getSms());

        return settingsRepository.save(settings);
    }

    @Transactional
    public UserAccountSettings updatePreferences(Long userId, PreferenceUpdateRequest request) {
        getUser(userId);
        UserAccountSettings settings = getOrCreateSettings(userId);

        if (request.getLanguage() != null)
            settings.setPreferredLanguage(request.getLanguage());

        if (request.getTimezone() != null)
            settings.setTimeZone(request.getTimezone());

        return settingsRepository.save(settings);
    }

    @Transactional
    public UserAccountSettings createDefaultSettings(Long userId) {
        Users user = getUser(userId);

        UserAccountSettings settings = new UserAccountSettings();
        settings.setUser(user); 
        settings.setIsBiometric(false);
        settings.setSessionTimeOut("30");
        settings.setIsEmailAlert(true);
        settings.setIsTransactionAlert(true);
        settings.setIsLoginAlert(true);
        settings.setIsReceiveMarketingNews(false);
        settings.setIsReceiveSmsMessage(false);
        settings.setPreferredLanguage("English");
        settings.setTimeZone("Africa/Lagos");

        return settingsRepository.save(settings);
    }


    public static class NotificationUpdateRequest {
        private Boolean email;
        private Boolean transactionAlerts;
        private Boolean loginAlerts;
        private Boolean marketingEmails;
        private Boolean sms;

        public Boolean getEmail() { return email; }
        public void setEmail(Boolean email) { this.email = email; }

        public Boolean getTransactionAlerts() { return transactionAlerts; }
        public void setTransactionAlerts(Boolean transactionAlerts) { this.transactionAlerts = transactionAlerts; }

        public Boolean getLoginAlerts() { return loginAlerts; }
        public void setLoginAlerts(Boolean loginAlerts) { this.loginAlerts = loginAlerts; }

        public Boolean getMarketingEmails() { return marketingEmails; }
        public void setMarketingEmails(Boolean marketingEmails) { this.marketingEmails = marketingEmails; }

        public Boolean getSms() { return sms; }
        public void setSms(Boolean sms) { this.sms = sms; }
    }

    public static class PreferenceUpdateRequest {
        private String language;
        private String timezone;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
    }

    // UserAccountSettingsService
    public UserAccountSettings findByUserId(Long userId) {
        // validate user exists
        usersRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found for user ID: " + userId));

        return settingsRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User account settings not found for user ID: " + userId));
    }

    private UserAccountSettings getOrCreateSettings(Long userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Users user = getUser(userId);
                    UserAccountSettings settings = new UserAccountSettings();
                    settings.setUser(user); 
                    return settings;
                });
    }


}
