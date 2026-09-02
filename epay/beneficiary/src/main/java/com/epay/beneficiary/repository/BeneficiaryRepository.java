package com.epay.beneficiary.repository;

import com.epay.domain.beneficiary.entity.Beneficiary;
import com.epay.domain.beneficiary.enums.BeneficiaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    /** Find a single active beneficiary owned by this user. */
    Optional<Beneficiary> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    /** Find the first active beneficiary for a user (used by GetById endpoint). */
    Optional<Beneficiary> findFirstByUserIdAndIsActiveTrue(Long userId);

    /** All active beneficiaries for a user, alphabetically sorted. */
    List<Beneficiary> findByUserIdAndIsActiveTrueOrderByBeneficiaryNameAsc(Long userId);

    /** Active beneficiaries filtered by type, alphabetically sorted. */
    List<Beneficiary> findByUserIdAndBeneficiaryTypeAndIsActiveTrueOrderByBeneficiaryNameAsc(
            Long userId, BeneficiaryType type);

    /**
     * Full-text search across name, account number, and recipient username.
     * Case-insensitive. Only returns active records.
     */
    @Query("""
           SELECT b FROM Beneficiary b
           WHERE  b.userId   = :userId
             AND  b.isActive = true
             AND (LOWER(b.beneficiaryName)    LIKE LOWER(CONCAT('%', :term, '%'))
               OR b.accountNumber             LIKE CONCAT('%', :term, '%')
               OR LOWER(b.recipientUsername)  LIKE LOWER(CONCAT('%', :term, '%')))
           ORDER  BY b.beneficiaryName ASC
           """)
    List<Beneficiary> search(@Param("userId") Long userId, @Param("term") String term);

    /** Duplicate check: does an active BANK beneficiary with this account number already exist? */
    Optional<Beneficiary> findByUserIdAndAccountNumberAndBeneficiaryTypeAndIsActiveTrue(
            Long userId, String accountNumber, BeneficiaryType type);

    /** Duplicate check: does an active USER beneficiary with this username already exist? */
    Optional<Beneficiary> findByUserIdAndRecipientUsernameAndBeneficiaryTypeAndIsActiveTrue(
            Long userId, String recipientUsername, BeneficiaryType type);

    /** Look up a specific user beneficiary by username. */
    Optional<Beneficiary> findByUserIdAndRecipientUsernameAndIsActiveTrue(
            Long userId, String recipientUsername);

    /**
     * Bulk soft-delete: set isActive = false for all matching id + userId pairs.
     */
    @Modifying
    @Query("""
           UPDATE Beneficiary b
           SET    b.isActive = false
           WHERE  b.id IN :ids
             AND  b.userId = :userId
           """)
    void softDeleteByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
