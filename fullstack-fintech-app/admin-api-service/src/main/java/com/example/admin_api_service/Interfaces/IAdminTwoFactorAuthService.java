package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.util.Optional;

import com.example.admin_api_service.enums.TwoFactorMethod;
import com.example.admin_api_service.models.accessAndSecurity.AdminTwoFactorAuth;

public interface IAdminTwoFactorAuthService {
    AdminTwoFactorAuth initiate2FA(String adminUserId, TwoFactorMethod method);
 
    AdminTwoFactorAuth verify2FA(String adminUserId, TwoFactorMethod method, String code);
 
    void enable2FA(String adminUserId, TwoFactorMethod method);
 
    void disable2FA(String adminUserId, TwoFactorMethod method, String disabledBy);
 
    boolean validate2FACode(String adminUserId, String code);
 
    boolean validateBackupCode(String adminUserId, String backupCode);
 
    List<String> regenerateBackupCodes(String adminUserId);
 
    Optional<AdminTwoFactorAuth> getActive2FA(String adminUserId);
 
    List<AdminTwoFactorAuth> getAll2FAMethods(String adminUserId);
 
    boolean is2FAEnabled(String adminUserId);
 
    boolean is2FALocked(String adminUserId);
 
    void resetFailedAttempts(String adminUserId);
 
    void incrementFailedAttempts(String adminUserId);
}
