package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.CurrencyStatus;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedCurrency;

@Repository
public interface SupportedCurrencyRepository extends JpaRepository<SupportedCurrency, String> {
    Optional<SupportedCurrency> findByCode(String code);
    Optional<SupportedCurrency> findByIsBaseCurrencyTrue();
    boolean existsByCode(String code);
    List<SupportedCurrency> findAllByStatus(CurrencyStatus status);
}