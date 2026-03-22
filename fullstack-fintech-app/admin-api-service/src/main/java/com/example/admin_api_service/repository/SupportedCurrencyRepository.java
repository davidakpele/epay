package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedCurrency;

@Repository
public interface SupportedCurrencyRepository extends JpaRepository<SupportedCurrency, String> {

}