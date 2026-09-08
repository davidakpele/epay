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

    Optional<Beneficiary> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    Optional<Beneficiary> findFirstByUserIdAndIsActiveTrue(Long userId);

    List<Beneficiary> findByUserIdAndIsActiveTrueOrderByBeneficiaryNameAsc(Long userId);

    List<Beneficiary> findByUserIdAndBeneficiaryTypeAndIsActiveTrueOrderByBeneficiaryNameAsc(
            Long userId, BeneficiaryType type);

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

    Optional<Beneficiary> findByUserIdAndAccountNumberAndBeneficiaryTypeAndIsActiveTrue(
            Long userId, String accountNumber, BeneficiaryType type);

    Optional<Beneficiary> findByUserIdAndRecipientUsernameAndBeneficiaryTypeAndIsActiveTrue(
            Long userId, String recipientUsername, BeneficiaryType type);

    Optional<Beneficiary> findByUserIdAndRecipientUsernameAndIsActiveTrue(
            Long userId, String recipientUsername);
            
    @Modifying
    @Query("""
           UPDATE Beneficiary b
           SET    b.isActive = false
           WHERE  b.id IN :ids
             AND  b.userId = :userId
           """)
    void softDeleteByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
