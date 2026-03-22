package com.example.admin_api_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagStatus;
import com.example.admin_api_service.enums.AccountFlagType;
import com.example.admin_api_service.models.userAndWalletManagement.AccountFlag;


@Repository
public interface AccountFlagRepository extends JpaRepository<AccountFlag, String> {
    Page<AccountFlag> findAllByStatus(AccountFlagStatus status, Pageable pageable);
    Page<AccountFlag> findAllBySeverity(AccountFlagSeverity severity, Pageable pageable);
    List<AccountFlag> findAllByUserIdAndStatusIn(Long userId, List<AccountFlagStatus> statuses);
    List<AccountFlag> findAllByWalletId(Long walletId);
    boolean existsByUserIdAndFlagTypeAndStatusIn(Long userId, AccountFlagType flagType, List<AccountFlagStatus> statuses);
}
