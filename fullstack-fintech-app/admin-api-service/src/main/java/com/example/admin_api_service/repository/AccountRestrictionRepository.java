package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AccountRestrictionStatus;
import com.example.admin_api_service.enums.AccountRestrictionType;
import com.example.admin_api_service.models.userAndWalletManagement.AccountRestriction;

@Repository
public interface AccountRestrictionRepository extends JpaRepository<AccountRestriction, String> {
    List<AccountRestriction> findAllByUserIdAndStatus(Long userId, AccountRestrictionStatus status);
    List<AccountRestriction> findAllByWalletIdAndStatus(Long walletId, AccountRestrictionStatus status);
    Page<AccountRestriction> findAllByStatus(AccountRestrictionStatus status, Pageable pageable);
    boolean existsByUserIdAndRestrictionTypeAndStatus(Long userId, AccountRestrictionType type, AccountRestrictionStatus status);
    List<AccountRestriction> findAllByStatusAndExpiresAtBefore(AccountRestrictionStatus status, LocalDateTime now);
}
