package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.FeeConfigurationType;
import com.example.admin_api_service.models.feeAndLimits.FeeConfiguration;

public interface IFeeConfigurationService {

    FeeConfiguration createConfiguration(FeeConfiguration config, String createdBy);
 
    FeeConfiguration updateConfiguration(String configId, FeeConfiguration updated, String updatedBy);
 
    FeeConfiguration getConfigurationById(String configId);
 
    FeeConfiguration getConfigurationByCode(String code);
 
    Page<FeeConfiguration> getAllConfigurations(Pageable pageable);
 
    List<FeeConfiguration> getActiveConfigurations();
 
    List<FeeConfiguration> getConfigurationsByType(FeeConfigurationType type);
 
    // Resolve the active config for a given transaction context
    FeeConfiguration resolveConfiguration(FeeConfigurationType type, String channel,
                                          String currency, String kycTier);
 
    // Calculate the fee amount for a given transaction amount using the matched config
    BigDecimal calculateFee(FeeConfigurationType type, String channel,
                            String currency, String kycTier, BigDecimal transactionAmount);
 
    void activateConfiguration(String configId, String updatedBy);
 
    void deactivateConfiguration(String configId, String updatedBy);
 
    // Supersede old config and activate new one atomically
    FeeConfiguration supersede(String oldConfigId, FeeConfiguration newConfig, String updatedBy);
 
    void deleteConfiguration(String configId);
}
