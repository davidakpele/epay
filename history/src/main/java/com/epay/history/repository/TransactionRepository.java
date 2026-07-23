package com.epay.history.repository;

import com.epay.domain.history.entity.Transaction;
import com.epay.domain.history.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByReference(String reference);

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByTransactionId(String transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.transactionType = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndType(@Param("userId") Long userId,
                                          @Param("type") String type,
                                          Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.currentStatus = :status ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") TransactionStatus status,
                                             Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("SELECT t FROM Transaction t WHERE t.currentStatus = :status ORDER BY t.createdAt ASC")
    List<Transaction> findByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.grossAmount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.transactionType = :type " +
           "AND t.currentStatus = com.epay.domain.history.enums.TransactionStatus.DELIVERED")
    BigDecimal sumDeliveredByUserIdAndType(@Param("userId") Long userId,
                                            @Param("type") String type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.transactionType = :type")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}
