package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.FreezeType;
import com.example.admin_api_service.enums.WalletFreezeStatus;
import com.example.admin_api_service.models.userAndWalletManagement.WalletFreeze;
import java.util.List;

@Repository
public interface WalletFreezeRepository extends JpaRepository<WalletFreeze, String> {
    List<WalletFreeze> findAllByWalletIdAndStatus(Long walletId, WalletFreezeStatus status);
    List<WalletFreeze> findAllByUserIdAndStatus(Long userId, WalletFreezeStatus status);
    Page<WalletFreeze> findAllByWalletId(Long walletId, Pageable pageable);
    Page<WalletFreeze> findAllByStatus(WalletFreezeStatus status, Pageable pageable);
    boolean existsByWalletIdAndFreezeTypeAndStatus(Long walletId, FreezeType freezeType, WalletFreezeStatus status);
    boolean existsByWalletIdAndStatusAndFreezeTypeIn(Long walletId, WalletFreezeStatus status, List<FreezeType> types);
    List<WalletFreeze> findAllByStatusAndExpiresAtBefore(WalletFreezeStatus status, LocalDateTime now);
}