package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.BankType;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedBank;

public interface ISupportedBankService {
    SupportedBank addBank(SupportedBank bank, String createdBy);
 
    SupportedBank updateBank(String bankCode, SupportedBank updated, String updatedBy);
 
    SupportedBank getBankByCode(String bankCode);
 
    SupportedBank getBankById(String bankId);
 
    Page<SupportedBank> getAllBanks(Pageable pageable);
 
    List<SupportedBank> getActiveBanks();
 
    List<SupportedBank> getBanksByType(BankType bankType);
 
    List<SupportedBank> getBanksByCountry(String countryCode);
 
    void activateBank(String bankCode, String updatedBy);
 
    void disableTransfers(String bankCode, String updatedBy, String reason);
 
    void setDegraded(String bankCode, String updatedBy);
 
    void suspendBank(String bankCode, String updatedBy);
 
    // Update running success rate and avg response time (called by the transfer engine)
    void updatePerformanceMetrics(String bankCode, BigDecimal successRate, Long avgResponseTimeMs);
 
    boolean isTransferEnabled(String bankCode);
 
    boolean isNipEnabled(String bankCode);
 
    boolean existsByBankCode(String bankCode);
}
