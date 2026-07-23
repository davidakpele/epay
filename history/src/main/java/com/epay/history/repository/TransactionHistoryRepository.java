package com.epay.history.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.epay.domain.history.entity.TransactionHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @deprecated Superseded by {@link com.epay.history.repository.TransactionRepository}.
 * Kept for backward compatibility with existing transaction_history table data.
 * New code must use TransactionRepository + Transaction entity.
 */
@Deprecated(since = "2.0", forRemoval = false)
@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    @Query("SELECT h FROM TransactionHistory h WHERE h.userId = :userId ORDER BY h.createdAt DESC")
    Page<TransactionHistory> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT h FROM TransactionHistory h WHERE h.userId = :userId AND h.transactionType = :type ORDER BY h.createdAt DESC")
    Page<TransactionHistory> findByUserIdAndType(@Param("userId") Long userId,
                                                  @Param("type") String type,
                                                  Pageable pageable);

    @Query("SELECT h FROM TransactionHistory h WHERE h.reference = :reference")
    Optional<TransactionHistory> findByReference(@Param("reference") String reference);

    @Query("SELECT h FROM TransactionHistory h WHERE h.transactionId = :txnId")
    Optional<TransactionHistory> findByTransactionId(@Param("txnId") String transactionId);

    @Query("SELECT h FROM TransactionHistory h WHERE h.idempotencyKey = :key")
    Optional<TransactionHistory> findByIdempotencyKey(@Param("key") String key);

    @Query("SELECT h FROM TransactionHistory h WHERE h.walletId = :walletId ORDER BY h.createdAt DESC")
    Page<TransactionHistory> findByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT h FROM TransactionHistory h WHERE h.userId = :userId " +
           "AND h.createdAt BETWEEN :from AND :to ORDER BY h.createdAt DESC")
    List<TransactionHistory> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(h) FROM TransactionHistory h WHERE h.userId = :userId AND h.transactionType = :type")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT SUM(h.grossAmount) FROM TransactionHistory h WHERE h.userId = :userId " +
           "AND h.transactionType = :type AND h.status = 'SUCCESS'")
    java.math.BigDecimal sumByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT COUNT(h) > 0 FROM TransactionHistory h WHERE h.idempotencyKey = :key")
    boolean existsByIdempotencyKey(@Param("key") String key);
}
