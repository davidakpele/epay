package com.example.escrow_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.escrow_service.models.Ledger;

@Repository
public interface LegderRepository extends JpaRepository<Ledger, String> {

    List<Ledger> findByStatus(String string);

}
