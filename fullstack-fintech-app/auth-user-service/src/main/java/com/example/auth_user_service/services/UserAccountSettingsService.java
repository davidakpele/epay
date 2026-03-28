package com.example.auth_user_service.services;

import com.example.auth_user_service.exceptions.ResourceNotFoundException;
import com.example.auth_user_service.interfaces.IUserAccountSettingsService;
import com.example.auth_user_service.models.UserAccountSettings;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.NotificationUpdateRequest;
import com.example.auth_user_service.payloads.PreferenceUpdateRequest;
import com.example.auth_user_service.repositories.UserAccountSettingsRepository;
import com.example.auth_user_service.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountSettingsService implements IUserAccountSettingsService{

    @Autowired
    private UserAccountSettingsRepository settingsRepository;

    @Autowired
    private UsersRepository usersRepository;

    private Users getUser(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found for user ID: " + userId));
    }

    @Transactional
    @Override
    public UserAccountSettings updateBiometricStatus(Long userId, boolean enableBiometric) {
        getUser(userId);
        UserAccountSettings settings = getOrCreateSettings(userId);
        settings.setIsBiometric(enableBiometric);
        return settingsRepository.save(settings);
    }

    @Transactional
    @Override
    public UserAccountSettings updateSessionTimeout(Long userId, String sessionTimeout) {
        getUser(userId);
        UserAccountSettings settings = getOrCreateSettings(userId);
        settings.setSessionTimeOut(sessionTimeout);
        return settingsRepository.save(settings);
    }


    @Transactional
    @Override
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
    @Override
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
    @Override
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

    @Override
    public UserAccountSettings findByUserId(Long userId) {
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
