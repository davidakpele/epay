package com.epay.history.repository;

import com.epay.domain.history.entity.TransactionStatusHistory;
import com.epay.domain.history.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionStatusHistoryRepository extends JpaRepository<TransactionStatusHistory, Long> {

    @Query("SELECT h FROM TransactionStatusHistory h " +
           "WHERE h.transactionId = :transactionId " +
           "ORDER BY h.sortOrder ASC")
    List<TransactionStatusHistory> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT h FROM TransactionStatusHistory h " +
           "WHERE h.transactionId = :transactionId AND h.status = :status " +
           "ORDER BY h.sortOrder ASC")
    List<TransactionStatusHistory> findByTransactionIdAndStatus(
            @Param("transactionId") String transactionId,
            @Param("status")        TransactionStatus status);

    long countByTransactionId(String transactionId);
}
