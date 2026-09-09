package com.epay.maintenance.repository;

import com.epay.domain.maintenance.entity.UserMonthlyActivity;
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
public interface UserMonthlyActivityRepository extends JpaRepository<UserMonthlyActivity, Long> {

    @Query("""
           SELECT a FROM UserMonthlyActivity a
           WHERE a.userId = :userId
             AND UPPER(a.currencyCode) = UPPER(:currency)
             AND a.monthYear = :monthYear
           """)
    Optional<UserMonthlyActivity> findByUserIdAndCurrencyAndMonth(
            @Param("userId") Long userId,
            @Param("currency") String currency,
            @Param("monthYear") LocalDate monthYear);

    @Query("""
           SELECT a FROM UserMonthlyActivity a
           WHERE a.monthYear = :monthYear
             AND a.hasActivity = true
             AND a.processed = false
           ORDER BY a.userId, a.currencyCode
           """)
    Page<UserMonthlyActivity> findUnprocessedForMonth(
            @Param("monthYear") LocalDate monthYear,
            Pageable pageable);

    @Query("""
           SELECT COUNT(a) FROM UserMonthlyActivity a
           WHERE a.monthYear = :monthYear
             AND a.hasActivity = true
             AND a.processed = false
           """)
    long countUnprocessedForMonth(@Param("monthYear") LocalDate monthYear);

    @Modifying
    @Query("UPDATE UserMonthlyActivity a SET a.processed = true WHERE a.id = :id")
    void markProcessed(@Param("id") Long id);

    @Query("SELECT a FROM UserMonthlyActivity a WHERE a.userId = :userId ORDER BY a.monthYear DESC")
    List<UserMonthlyActivity> findByUserId(@Param("userId") Long userId);

    @Query("""
           SELECT a FROM UserMonthlyActivity a
           WHERE a.userId = :userId AND a.monthYear = :monthYear
           ORDER BY a.currencyCode
           """)
    List<UserMonthlyActivity> findByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("monthYear") LocalDate monthYear);

    @Modifying
    @Query("""
           UPDATE UserMonthlyActivity a
           SET a.transactionCount = a.transactionCount + 1,
               a.totalVolume      = a.totalVolume + :amount,
               a.hasActivity      = true
           WHERE a.userId = :userId
             AND UPPER(a.currencyCode) = UPPER(:currency)
             AND a.monthYear = :monthYear
           """)
    int incrementActivity(
            @Param("userId") Long userId,
            @Param("currency") String currency,
            @Param("monthYear") LocalDate monthYear,
            @Param("amount") java.math.BigDecimal amount);
}
