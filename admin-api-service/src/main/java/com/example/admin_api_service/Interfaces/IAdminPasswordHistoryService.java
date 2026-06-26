package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.models.accessAndSecurity.AdminPasswordHistory;
import java.util.List;

public interface IAdminPasswordHistoryService {
    AdminPasswordHistory recordPasswordChange(Long adminUserId, String newPasswordHash,
                                              String changeReason, String changedBy,
                                              String ipAddress, String userAgent);
 
    boolean isPasswordReused(Long adminUserId, String rawPassword, int historyDepth);
 
    List<AdminPasswordHistory> getPasswordHistory(Long adminUserId);
 
    AdminPasswordHistory getLatestPasswordRecord(Long adminUserId);
 
    boolean isPasswordExpired(Long adminUserId, int maxPasswordAgeDays);
}
