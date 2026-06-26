package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.util.Optional;

import com.example.admin_api_service.enums.TwoFactorMethod;
import com.example.admin_api_service.models.accessAndSecurity.AdminTwoFactorAuth;

public interface IAdminTwoFactorAuthService {
    AdminTwoFactorAuth initiate2FA(Long adminUserId, TwoFactorMethod method);
 
    AdminTwoFactorAuth verify2FA(Long adminUserId, TwoFactorMethod method, String code);
 
    void enable2FA(Long adminUserId, TwoFactorMethod method);
 
    void disable2FA(Long adminUserId, TwoFactorMethod method, String disabledBy);
 
    boolean validate2FACode(Long adminUserId, String code);
 
    boolean validateBackupCode(Long adminUserId, String backupCode);
 
    List<String> regenerateBackupCodes(Long adminUserId);
 
    Optional<AdminTwoFactorAuth> getActive2FA(Long adminUserId);
 
    List<AdminTwoFactorAuth> getAll2FAMethods(Long adminUserId);
 
    boolean is2FAEnabled(Long adminUserId);
 
    boolean is2FALocked(Long adminUserId);
 
    void resetFailedAttempts(Long adminUserId);
 
    void incrementFailedAttempts(Long adminUserId);
}
