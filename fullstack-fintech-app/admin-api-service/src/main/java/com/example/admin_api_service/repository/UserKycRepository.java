package com.example.admin_api_service.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.KycStatus;
import com.example.admin_api_service.enums.KycTier;
import com.example.admin_api_service.models.userAndWalletManagement.UserKyc;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserKycRepository extends JpaRepository<UserKyc, String> {
    Optional<UserKyc> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Page<UserKyc> findAllByStatus(KycStatus status, Pageable pageable);
    Page<UserKyc> findAllByTier(KycTier tier, Pageable pageable);
    List<UserKyc> findAllByStatusAndExpiresAtBefore(KycStatus status, LocalDateTime now);
}