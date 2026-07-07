package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.KycDocument;
import com.epay.domain.auth.enums.KycDocumentType;
import com.epay.domain.auth.enums.KycStatus;
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
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserId(Long userId);

    List<KycDocument> findByUserIdAndStatus(Long userId, KycStatus status);

    Optional<KycDocument> findByUserIdAndDocumentType(Long userId, KycDocumentType documentType);

    Page<KycDocument> findByStatus(KycStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE KycDocument d SET d.status = :status, d.reviewedByUserId = :reviewedBy, " +
           "d.reviewedAt = :reviewedAt, d.rejectionReason = :reason " +
           "WHERE d.id = :documentId")
    void updateReviewDecision(@Param("documentId") Long documentId,
                              @Param("status") KycStatus status,
                              @Param("reviewedBy") Long reviewedByUserId,
                              @Param("reviewedAt") LocalDateTime reviewedAt,
                              @Param("reason") String rejectionReason);

    boolean existsByUserIdAndDocumentTypeAndStatus(Long userId, KycDocumentType documentType, KycStatus status);
}
