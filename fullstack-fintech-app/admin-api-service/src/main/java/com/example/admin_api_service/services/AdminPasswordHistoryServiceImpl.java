package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminPasswordHistoryService;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminPasswordHistory;
import com.example.admin_api_service.repository.AdminPasswordHistoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminPasswordHistoryServiceImpl implements IAdminPasswordHistoryService {

    private final AdminPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordHistoryServiceImpl(AdminPasswordHistoryRepository passwordHistoryRepository,
                                           PasswordEncoder passwordEncoder) {
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminPasswordHistory recordPasswordChange(Long adminUserId, String newPasswordHash,
                                                     String changeReason, String changedBy,
                                                     String ipAddress, String userAgent) {
        AdminPasswordHistory history = new AdminPasswordHistory();
        history.setAdminUserId(adminUserId);
        history.setPasswordHash(newPasswordHash);
        history.setChangeReason(changeReason);
        history.setChangedBy(changedBy);
        history.setIpAddress(ipAddress);
        history.setUserAgent(userAgent);
        return passwordHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordReused(Long adminUserId, String rawPassword, int historyDepth) {
        List<AdminPasswordHistory> recentHistory = passwordHistoryRepository
                .findTopNByAdminUserIdOrderByCreatedOnDesc(adminUserId, historyDepth);

        return recentHistory.stream()
                .anyMatch(history -> passwordEncoder.matches(rawPassword, history.getPasswordHash()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPasswordHistory> getPasswordHistory(Long adminUserId) {
        return passwordHistoryRepository.findAllByAdminUserIdOrderByCreatedOnDesc(adminUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPasswordHistory getLatestPasswordRecord(Long adminUserId) {
        return passwordHistoryRepository.findTopByAdminUserIdOrderByCreatedOnDesc(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AdminPasswordHistory", "adminUserId", adminUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordExpired(Long adminUserId, int maxPasswordAgeDays) {
        return passwordHistoryRepository.findTopByAdminUserIdOrderByCreatedOnDesc(adminUserId)
                .map(latest -> latest.getCreatedOn()
                        .plusDays(maxPasswordAgeDays)
                        .isBefore(LocalDateTime.now()))
                .orElse(false);
    }
}