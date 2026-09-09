package com.epay.maintenance.repository;

import com.epay.domain.maintenance.entity.MaintenanceFeeTransaction;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceFeeTransactionRepository extends JpaRepository<MaintenanceFeeTransaction, Long> {

    Optional<MaintenanceFeeTransaction> findByReferenceId(String referenceId);

    @Query("""
           SELECT COUNT(t) > 0 FROM MaintenanceFeeTransaction t
           WHERE t.userId = :userId
             AND UPPER(t.currencyCode) = UPPER(:currency)
             AND t.monthYear = :monthYear
             AND t.status NOT IN ('FAILED', 'WAIVED')
           """)
    boolean existsBillForMonth(
            @Param("userId") Long userId,
            @Param("currency") String currency,
            @Param("monthYear") LocalDate monthYear);

    @Query("""
           SELECT t FROM MaintenanceFeeTransaction t
           WHERE t.userId = :userId
           ORDER BY t.createdAt DESC
           """)
    Page<MaintenanceFeeTransaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
           SELECT t FROM MaintenanceFeeTransaction t
           WHERE t.userId = :userId AND t.monthYear = :monthYear
           ORDER BY t.currencyCode
           """)
    List<MaintenanceFeeTransaction> findByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("monthYear") LocalDate monthYear);

    @Query("""
           SELECT t FROM MaintenanceFeeTransaction t
           WHERE t.userId = :userId
             AND t.status IN ('DEBT', 'PARTIAL')
             AND t.repaid = false
           ORDER BY t.createdAt ASC
           """)
    List<MaintenanceFeeTransaction> findActiveDebtsByUserId(@Param("userId") Long userId);

    @Query("""
           SELECT t FROM MaintenanceFeeTransaction t
           WHERE t.userId = :userId
             AND UPPER(t.currencyCode) = UPPER(:currency)
             AND t.status IN ('DEBT', 'PARTIAL')
             AND t.repaid = false
           ORDER BY t.createdAt ASC
           """)
    List<MaintenanceFeeTransaction> findActiveDebtsByUserIdAndCurrency(
            @Param("userId") Long userId,
            @Param("currency") String currency);

    @Query("SELECT t FROM MaintenanceFeeTransaction t WHERE t.status = :status ORDER BY t.createdAt DESC")
    Page<MaintenanceFeeTransaction> findByStatus(@Param("status") MaintenanceFeeStatus status, Pageable pageable);

    @Query("SELECT t FROM MaintenanceFeeTransaction t WHERE t.monthYear = :monthYear ORDER BY t.userId, t.currencyCode")
    Page<MaintenanceFeeTransaction> findByMonth(@Param("monthYear") LocalDate monthYear, Pageable pageable);

    @Query("""
           SELECT t FROM MaintenanceFeeTransaction t
           WHERE t.monthYear = :monthYear AND t.status = :status
           ORDER BY t.userId, t.currencyCode
           """)
    Page<MaintenanceFeeTransaction> findByMonthAndStatus(
            @Param("monthYear") LocalDate monthYear,
            @Param("status") MaintenanceFeeStatus status,
            Pageable pageable);

    @Query("""
           SELECT t.status, COUNT(t)
           FROM MaintenanceFeeTransaction t
           WHERE t.monthYear = :monthYear
           GROUP BY t.status
           """)
    List<Object[]> countByStatusForMonth(@Param("monthYear") LocalDate monthYear);

    @Query("""
           SELECT COALESCE(SUM(t.deductedAmount), 0)
           FROM MaintenanceFeeTransaction t
           WHERE t.monthYear = :monthYear
             AND t.status IN ('DEDUCTED', 'PARTIAL', 'REPAID')
           """)
    java.math.BigDecimal sumDeductedForMonth(@Param("monthYear") LocalDate monthYear);

    @Query("""
           SELECT COALESCE(SUM(t.debtAmount), 0)
           FROM MaintenanceFeeTransaction t
           WHERE t.monthYear = :monthYear
             AND t.repaid = false
           """)
    java.math.BigDecimal sumOutstandingDebtForMonth(@Param("monthYear") LocalDate monthYear);

    @Query("SELECT DISTINCT t.monthYear FROM MaintenanceFeeTransaction t ORDER BY t.monthYear DESC")
    List<LocalDate> findDistinctMonths();

    @Modifying
    @Query("""
           UPDATE MaintenanceFeeTransaction t
           SET t.repaid = true,
               t.repaidAt = CURRENT_TIMESTAMP,
               t.status = 'REPAID',
               t.debtAmount = 0
           WHERE t.id = :id
           """)
    void markRepaid(@Param("id") Long id);
}
