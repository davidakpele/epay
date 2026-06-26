package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.BankStatus;
import com.example.admin_api_service.enums.BankType;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedBank;

@Repository
public interface SupportedBankRepository extends JpaRepository<SupportedBank, String> {
    Optional<SupportedBank> findByBankCode(String bankCode);
    boolean existsByBankCode(String bankCode);
    List<SupportedBank> findAllByStatus(BankStatus status);
    List<SupportedBank> findAllByBankType(BankType bankType);
    List<SupportedBank> findAllByCountryCodeAndStatus(String countryCode, BankStatus status);
}