package com.epay.auth.repository;

import com.epay.auth.domain.entity.KycVerification;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {

    @Query("SELECT v FROM KycVerification v WHERE v.user.id = :userId AND v.tier = :tier " +
           "ORDER BY v.createdAt DESC LIMIT 1")
    Optional<KycVerification> findLatestByUserIdAndTier(@Param("userId") Long userId, @Param("tier") KycTier tier);

    List<KycVerification> findByUserId(Long userId);

    Page<KycVerification> findByStatus(KycStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE KycVerification v SET v.status = :status, v.reviewedByUserId = :reviewedBy, " +
           "v.reviewedAt = :reviewedAt, v.rejectionReason = :reason, v.internalNote = :note " +
           "WHERE v.id = :verificationId")
    void updateReviewDecision(@Param("verificationId") Long verificationId,
                              @Param("status") KycStatus status,
                              @Param("reviewedBy") Long reviewedByUserId,
                              @Param("reviewedAt") LocalDateTime reviewedAt,
                              @Param("reason") String rejectionReason,
                              @Param("note") String internalNote);

    boolean existsByUserIdAndTierAndStatus(Long userId, KycTier tier, KycStatus status);
}
