package com.example.escrow_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.escrow_service.models.Escrow;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Long> {
    @Query("SELECT e FROM Escrow e JOIN e.balances b WHERE b.currencyCode = :currencyCode")
    List<Escrow> findByCurrencyCode(@Param("currencyCode") String currencyCode);

    @Query("SELECT e FROM Escrow e JOIN e.balances b WHERE e.id = :escrowId AND b.currencyCode = :currencyCode")
    Optional<Escrow> findByIdAndCurrencyCode(@Param("escrowId") Long escrowId, 
                                           @Param("currencyCode") String currencyCode);
}
