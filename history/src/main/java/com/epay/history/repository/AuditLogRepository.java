package com.epay.history.repository;

import com.epay.domain.history.entity.TransactionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<TransactionAuditLog, Long> {

    @Query("SELECT a FROM TransactionAuditLog a WHERE a.transactionId = :txnId ORDER BY a.createdAt ASC")
    List<TransactionAuditLog> findByTransactionId(@Param("txnId") String transactionId);

    @Query("SELECT a FROM TransactionAuditLog a WHERE a.performedBy = :actor ORDER BY a.createdAt DESC")
    Page<TransactionAuditLog> findByPerformedBy(@Param("actor") String performedBy, Pageable pageable);
}