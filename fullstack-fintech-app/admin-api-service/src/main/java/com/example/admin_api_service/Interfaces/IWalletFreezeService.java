package com.example.admin_api_service.Interfaces;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.FreezeReason;
import com.example.admin_api_service.enums.FreezeType;
import com.example.admin_api_service.enums.WalletFreezeStatus;
import com.example.admin_api_service.models.userAndWalletManagement.WalletFreeze;
import java.util.List;

public interface IWalletFreezeService {
    WalletFreeze freezeWallet(Long walletId, Long userId,
                              FreezeType freezeType, FreezeReason freezeReason,
                              String reasonNote, String externalReference,
                              String frozenBy, String ipAddress,
                              LocalDateTime expiresAt);
 
    WalletFreeze unfreezeWallet(String freezeId, String unfrozenBy, String unfreezeNote);
 
    WalletFreeze getFreezeById(String freezeId);
 
    Page<WalletFreeze> getAllFreezes(Pageable pageable);
 
    Page<WalletFreeze> getFreezesByStatus(WalletFreezeStatus status, Pageable pageable);
 
    List<WalletFreeze> getActiveFreezesByWallet(Long walletId);
 
    List<WalletFreeze> getActiveFreezesByUser(Long userId);
 
    Page<WalletFreeze> getFreezesByWallet(Long walletId, Pageable pageable);
 
    boolean isWalletFrozen(Long walletId);
 
    boolean isWalletDebitBlocked(Long walletId);
 
    boolean isWalletCreditBlocked(Long walletId);
 
    void expireAutoFreezes();
}
