package com.epay.history.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.epay.domain.history.entity.TransactionHistory;


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

    @Query("SELECT h FROM TransactionHistory h WHERE h.walletId = :walletId ORDER BY h.createdAt DESC")
    Page<TransactionHistory> findByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT h FROM TransactionHistory h WHERE h.userId = :userId " +
           "AND h.createdAt BETWEEN :from AND :to ORDER BY h.createdAt DESC")
    List<TransactionHistory> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(h) FROM TransactionHistory h WHERE h.userId = :userId AND h.transactionType = :type")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    Page<TransactionHistory> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @Query("""
           SELECT h FROM TransactionHistory h
           WHERE (:userId    IS NULL OR h.userId = :userId)
             AND (:status    IS NULL OR h.status = :status)
             AND (:type      IS NULL OR h.transactionType = :type)
             AND (:currency  IS NULL OR h.currencyType = :currency)
             AND (:minAmount IS NULL OR h.grossAmount >= :minAmount)
             AND (:maxAmount IS NULL OR h.grossAmount <= :maxAmount)
             AND (:from      IS NULL OR h.createdAt >= :from)
             AND (:to        IS NULL OR h.createdAt <= :to)
             AND (:amlFlag   IS NULL OR h.amlFlag = :amlFlag)
           ORDER BY h.createdAt DESC
           """)
    Page<TransactionHistory> adminSearch(
            @Param("userId")    Long userId,
            @Param("status")    String status,
            @Param("type")      String type,
            @Param("currency")  String currency,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to,
            @Param("amlFlag")   Boolean amlFlag,
            Pageable pageable);

    @Modifying
    @Query("UPDATE TransactionHistory h SET h.adminNote = :note, h.reviewedBy = :reviewedBy WHERE h.transactionId = :txnId")
    void updateAdminNote(@Param("txnId") String txnId,
                         @Param("note") String note,
                         @Param("reviewedBy") String reviewedBy);

    @Modifying
    @Query("UPDATE TransactionHistory h SET h.amlFlag = :flag, h.complianceNote = :note WHERE h.transactionId = :txnId")
    void updateAmlFlag(@Param("txnId") String txnId,
                       @Param("flag") boolean flag,
                       @Param("note") String note);

    @Modifying
    @Query("UPDATE TransactionHistory h SET h.disputeStatus = :disputeStatus, h.disputeReference = :disputeRef WHERE h.transactionId = :txnId")
    void updateDisputeStatus(@Param("txnId") String txnId,
                             @Param("disputeStatus") String disputeStatus,
                             @Param("disputeRef") String disputeRef);

    @Query("SELECT COUNT(h) FROM TransactionHistory h WHERE h.userId = :userId AND h.createdAt >= :from")
    long countByUserIdSince(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(h.grossAmount), 0) FROM TransactionHistory h WHERE h.userId = :userId AND h.transactionType = :type")
    BigDecimal sumByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}
