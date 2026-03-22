package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.models.accessAndSecurity.AdminPasswordHistory;
import java.util.List;

public interface IAdminPasswordHistoryService {
    AdminPasswordHistory recordPasswordChange(String adminUserId, String newPasswordHash,
                                              String changeReason, String changedBy,
                                              String ipAddress, String userAgent);
 
    boolean isPasswordReused(String adminUserId, String rawPassword, int historyDepth);
 
    List<AdminPasswordHistory> getPasswordHistory(String adminUserId);
 
    AdminPasswordHistory getLatestPasswordRecord(String adminUserId);
 
    boolean isPasswordExpired(String adminUserId, int maxPasswordAgeDays);
}
