package com.epay.history.repository;

import com.epay.domain.history.entity.TransactionStateEntry;
import com.epay.domain.history.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionStateEntryRepository extends JpaRepository<TransactionStateEntry, Long> {

    @Query("SELECT e FROM TransactionStateEntry e " +
           "WHERE e.transactionId = :transactionId " +
           "ORDER BY e.sortOrder ASC")
    List<TransactionStateEntry> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT e FROM TransactionStateEntry e " +
           "WHERE e.transactionId = :transactionId AND e.stateName = :stateName")
    Optional<TransactionStateEntry> findByTransactionIdAndStateName(
            @Param("transactionId") String transactionId,
            @Param("stateName")     TransactionStatus stateName);

    @Modifying
    @Query("UPDATE TransactionStateEntry e " +
           "SET e.visitedAt = :visitedAt, e.actor = :actor, e.message = :message, " +
           "    e.onFailure = false, e.failureState = null " +
           "WHERE e.transactionId = :transactionId AND e.stateName = :stateName")
    int visitState(@Param("transactionId") String transactionId,
                   @Param("stateName")     TransactionStatus stateName,
                   @Param("visitedAt")     Instant visitedAt,
                   @Param("actor")         String actor,
                   @Param("message")       String message);

    @Modifying
    @Query("UPDATE TransactionStateEntry e " +
           "SET e.visitedAt = :visitedAt, e.actor = :actor, e.message = :message, " +
           "    e.onFailure = true, e.failureState = :failureState " +
           "WHERE e.transactionId = :transactionId AND e.stateName = :stateName")
    int visitStateFailed(@Param("transactionId") String transactionId,
                         @Param("stateName")     TransactionStatus stateName,
                         @Param("visitedAt")     Instant visitedAt,
                         @Param("actor")         String actor,
                         @Param("message")       String message,
                         @Param("failureState")  TransactionStatus failureState);

    boolean existsByTransactionId(String transactionId);
}
