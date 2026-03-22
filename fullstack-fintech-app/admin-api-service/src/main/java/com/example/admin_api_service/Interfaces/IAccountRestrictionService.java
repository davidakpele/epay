package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AccountRestrictionStatus;
import com.example.admin_api_service.enums.AccountRestrictionType;
import com.example.admin_api_service.models.userAndWalletManagement.AccountRestriction;

public interface IAccountRestrictionService {
    AccountRestriction applyRestriction(Long userId, Long walletId,
                                        AccountRestrictionType restrictionType,
                                        String reason, String internalNote,
                                        BigDecimal limitAmount, String limitCurrency,
                                        String externalReference,
                                        String appliedBy, String ipAddress,
                                        LocalDateTime expiresAt);
 
    AccountRestriction getRestrictionById(String restrictionId);
 
    Page<AccountRestriction> getAllRestrictions(Pageable pageable);
 
    Page<AccountRestriction> getRestrictionsByStatus(AccountRestrictionStatus status, Pageable pageable);
 
    List<AccountRestriction> getActiveRestrictionsByUser(Long userId);
 
    List<AccountRestriction> getActiveRestrictionsByWallet(Long walletId);
 
    AccountRestriction liftRestriction(String restrictionId, String liftedBy, String liftNote);
 
    boolean hasActiveRestriction(Long userId, AccountRestrictionType restrictionType);
 
    boolean isLoginDisabled(Long userId);
 
    boolean isTransactionDisabled(Long userId);
 
    void expireStaleRestrictions();
}
