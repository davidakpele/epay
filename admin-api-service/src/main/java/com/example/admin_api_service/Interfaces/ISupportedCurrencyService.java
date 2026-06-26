package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedCurrency;

public interface ISupportedCurrencyService {
    SupportedCurrency addCurrency(SupportedCurrency currency, String createdBy);
 
    SupportedCurrency updateCurrency(String code, SupportedCurrency updated, String updatedBy);
 
    SupportedCurrency getCurrencyByCode(String code);
 
    SupportedCurrency getCurrencyById(String currencyId);
 
    Page<SupportedCurrency> getAllCurrencies(Pageable pageable);
 
    List<SupportedCurrency> getActiveCurrencies();
 
    SupportedCurrency getBaseCurrency();
 
    void activateCurrency(String code, String updatedBy);
 
    void suspendCurrency(String code, String updatedBy);
 
    void setDepositEnabled(String code, boolean enabled, String updatedBy);
 
    void setWithdrawalEnabled(String code, boolean enabled, String updatedBy);
 
    void setFxEnabled(String code, boolean enabled, String updatedBy);
 
    boolean isCurrencyActive(String code);
 
    boolean isDepositEnabled(String code);
 
    boolean isWithdrawalEnabled(String code);
}
