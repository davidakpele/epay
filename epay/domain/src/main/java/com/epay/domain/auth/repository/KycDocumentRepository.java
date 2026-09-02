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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    @Query("SELECT d FROM KycDocument d WHERE d.user.id = :userId")
    List<KycDocument> findByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM KycDocument d WHERE d.user.id = :userId AND d.status = :status")
    List<KycDocument> findByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") KycStatus status);

    @Query("SELECT d FROM KycDocument d WHERE d.user.id = :userId AND d.documentType = :type")
    Optional<KycDocument> findByUserIdAndDocumentType(@Param("userId") Long userId,
                                                       @Param("type") KycDocumentType type);

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

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM KycDocument d WHERE d.user.id = :userId AND d.documentType = :type AND d.status = :status")
    boolean existsByUserIdAndDocumentTypeAndStatus(@Param("userId") Long userId,
                                                    @Param("type") KycDocumentType type,
                                                    @Param("status") KycStatus status);
}
