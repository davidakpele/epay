package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagStatus;
import com.example.admin_api_service.enums.AccountFlagType;
import com.example.admin_api_service.models.userAndWalletManagement.AccountFlag;

public interface IAccountFlagService {
 // Raised by a system rule (AML engine, risk engine)
    AccountFlag raiseSystemFlag(Long userId, Long walletId, AccountFlagType flagType,
                                AccountFlagSeverity severity, String title, String description,
                                String triggerRule, String evidence, String ipAddress);
 
    // Raised manually by an admin
    AccountFlag raiseManualFlag(Long userId, Long walletId, AccountFlagType flagType,
                                AccountFlagSeverity severity, String title, String description,
                                String flaggedBy, String ipAddress);
 
    AccountFlag getFlagById(String flagId);
 
    Page<AccountFlag> getAllFlags(Pageable pageable);
 
    Page<AccountFlag> getFlagsByStatus(AccountFlagStatus status, Pageable pageable);
 
    Page<AccountFlag> getFlagsBySeverity(AccountFlagSeverity severity, Pageable pageable);
 
    List<AccountFlag> getOpenFlagsForUser(Long userId);
 
    List<AccountFlag> getFlagsForWallet(Long walletId);
 
    AccountFlag assignFlag(String flagId, String assignedTo);
 
    AccountFlag resolveFlag(String flagId, String resolvedBy, String resolutionNote);
 
    AccountFlag markFalsePositive(String flagId, String resolvedBy, String resolutionNote);
 
    AccountFlag escalateFlag(String flagId, String escalatedBy);
 
    // Link flag to resulting action taken
    AccountFlag linkToFreeze(String flagId, String freezeId);
 
    AccountFlag linkToRestriction(String flagId, String restrictionId);
 
    boolean hasOpenFlag(Long userId, AccountFlagType flagType);
}
