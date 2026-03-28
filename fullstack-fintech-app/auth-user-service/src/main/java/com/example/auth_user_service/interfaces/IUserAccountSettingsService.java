package com.example.auth_user_service.interfaces;

import com.example.auth_user_service.models.UserAccountSettings;
import com.example.auth_user_service.payloads.NotificationUpdateRequest;
import com.example.auth_user_service.payloads.PreferenceUpdateRequest;

public interface IUserAccountSettingsService {
    UserAccountSettings updateBiometricStatus(Long userId, boolean enableBiometric);

    UserAccountSettings updateSessionTimeout(Long userId, String sessionTimeout);

    UserAccountSettings updateNotificationSettings(Long userId, NotificationUpdateRequest request);

    UserAccountSettings updatePreferences(Long userId, PreferenceUpdateRequest request);

    UserAccountSettings createDefaultSettings(Long userId);

    UserAccountSettings findByUserId(Long userId);
    

}
