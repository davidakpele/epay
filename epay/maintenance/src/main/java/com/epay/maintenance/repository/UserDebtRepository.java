package com.epay.maintenance.repository;

import com.epay.domain.maintenance.entity.UserDebt;
import com.epay.domain.maintenance.enums.DebtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDebtRepository extends JpaRepository<UserDebt, Long> {

    @Query("""
           SELECT d FROM UserDebt d
           WHERE d.userId = :userId
             AND UPPER(d.currencyCode) = UPPER(:currency)
           """)
    Optional<UserDebt> findByUserIdAndCurrency(
            @Param("userId") Long userId,
            @Param("currency") String currency);

    @Query("""
           SELECT d FROM UserDebt d
           WHERE d.userId = :userId
             AND d.status IN ('ACTIVE', 'PARTIAL')
           ORDER BY d.currencyCode
           """)
    List<UserDebt> findActiveDebtsByUserId(@Param("userId") Long userId);

    @Query("""
           SELECT COUNT(d) > 0 FROM UserDebt d
           WHERE d.userId = :userId
             AND UPPER(d.currencyCode) = UPPER(:currency)
             AND d.status IN ('ACTIVE', 'PARTIAL')
           """)
    boolean hasActiveDebt(@Param("userId") Long userId, @Param("currency") String currency);

    Page<UserDebt> findByStatus(DebtStatus status, Pageable pageable);

    @Query("""
           SELECT d FROM UserDebt d
           WHERE d.status IN ('ACTIVE', 'PARTIAL')
             AND d.createdAt <= :cutoff
           ORDER BY d.createdAt ASC
           """)
    Page<UserDebt> findDebtsOlderThan(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable);

    /** Count of users with active debt — dashboard stat */
    @Query("SELECT COUNT(DISTINCT d.userId) FROM UserDebt d WHERE d.status IN ('ACTIVE', 'PARTIAL')")
    long countUsersWithActiveDebt();

    /** Count of debts by status — dashboard stat */
    @Query("SELECT d.status, COUNT(d) FROM UserDebt d GROUP BY d.status")
    List<Object[]> countByStatus();

    @Query("""
           SELECT d.currencyCode, SUM(d.totalDebt)
           FROM UserDebt d
           WHERE d.status IN ('ACTIVE', 'PARTIAL')
           GROUP BY d.currencyCode
           """)
    List<Object[]> sumActiveDebtByCurrency();

    @Modifying
    @Query("""
           UPDATE UserDebt d
           SET d.totalDebt = d.totalDebt + :amount,
               d.status = 'ACTIVE',
               d.lastActivityDate = :now
           WHERE d.id = :id
           """)
    void addDebt(@Param("id") Long id, @Param("amount") BigDecimal amount, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
           UPDATE UserDebt d
           SET d.totalDebt   = d.totalDebt - :amount,
               d.totalRepaid = d.totalRepaid + :amount,
               d.lastActivityDate = :now
           WHERE d.id = :id
           """)
    void reduceDebt(@Param("id") Long id, @Param("amount") BigDecimal amount, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
           UPDATE UserDebt d
           SET d.totalDebt  = 0,
               d.status     = 'SETTLED',
               d.settledAt  = :now,
               d.lastActivityDate = :now
           WHERE d.id = :id
           """)
    void markSettled(@Param("id") Long id, @Param("now") LocalDateTime now);
}
