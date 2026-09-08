package com.epay.history.repository;

import com.epay.domain.history.entity.TransactionAllowedTransition;
import com.epay.domain.history.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionAllowedTransitionRepository extends JpaRepository<TransactionAllowedTransition, Long> {

    @Query("SELECT t FROM TransactionAllowedTransition t " +
           "WHERE t.transactionId = :transactionId " +
           "ORDER BY t.sortOrder ASC")
    List<TransactionAllowedTransition> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT t FROM TransactionAllowedTransition t " +
           "WHERE t.transactionId = :transactionId AND t.fromState = :fromState " +
           "ORDER BY t.sortOrder ASC")
    List<TransactionAllowedTransition> findByTransactionIdAndFromState(
            @Param("transactionId") String transactionId,
            @Param("fromState")     TransactionStatus fromState);

    boolean existsByTransactionId(String transactionId);
}
