package com.epay.maintenance.service;

import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.maintenance.dto.MaintenanceFeeConfigDto;
import com.epay.domain.maintenance.entity.MaintenanceFeeConfig;
import com.epay.domain.maintenance.enums.FeeType;
import com.epay.domain.maintenance.input.CreateFeeConfigRequest;
import com.epay.maintenance.repository.MaintenanceFeeConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceFeeConfigService {

    private final MaintenanceFeeConfigRepository configRepository;

    public List<MaintenanceFeeConfigDto> getAllActive() {
        return configRepository.findAllActive()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<MaintenanceFeeConfigDto> getAllForCurrency(String currencyCode) {
        return configRepository.findAllByCurrencyCode(currencyCode.toUpperCase())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public MaintenanceFeeConfigDto getActiveForCurrency(String currencyCode) {
        return configRepository.findActiveByCurrencyCode(currencyCode.toUpperCase())
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active maintenance fee config for currency: " + currencyCode));
    }

    public MaintenanceFeeConfigDto getById(Long id) {
        return configRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance fee config not found: " + id));
    }

    @Transactional
    public MaintenanceFeeConfigDto createOrReplace(CreateFeeConfigRequest request, Long adminId) {
        validate(request);

        String code = request.getCurrencyCode().trim().toUpperCase();

        configRepository.deactivateAllByCurrencyCode(code);

        MaintenanceFeeConfig config = MaintenanceFeeConfig.builder()
                .currencyCode(code)
                .feeType(request.getFeeType())
                .feeAmount(request.getFeeAmount())
                .feePercentage(request.getFeePercentage())
                .minimumFee(request.getMinimumFee())
                .maximumFee(request.getMaximumFee())
                .active(true)
                .createdByAdminId(adminId)
                .updatedByAdminId(adminId)
                .build();

        configRepository.save(config);
        log.info("[FeeConfig] Created/replaced config for {} by adminId={}", code, adminId);
        return toDto(config);
    }

    @Transactional
    public void deactivate(Long configId, Long adminId) {
        MaintenanceFeeConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance fee config not found: " + configId));
        config.setActive(false);
        config.setUpdatedByAdminId(adminId);
        configRepository.save(config);
        log.info("[FeeConfig] Deactivated config id={} by adminId={}", configId, adminId);
    }

    private void validate(CreateFeeConfigRequest r) {
        if (r.getFeeType() == FeeType.FIXED) {
            if (r.getFeeAmount() == null || r.getFeeAmount().signum() <= 0)
                throw new BadRequestException(
                        "FIXED fee type requires a positive feeAmount", ErrorCode.INVALID_INPUT);
        } else if (r.getFeeType() == FeeType.PERCENTAGE) {
            if (r.getFeePercentage() == null || r.getFeePercentage().signum() <= 0)
                throw new BadRequestException(
                        "PERCENTAGE fee type requires a positive feePercentage", ErrorCode.INVALID_INPUT);
            if (r.getMaximumFee() != null && r.getMinimumFee() != null
                    && r.getMaximumFee().compareTo(r.getMinimumFee()) < 0)
                throw new BadRequestException(
                        "maximumFee must be >= minimumFee", ErrorCode.INVALID_INPUT);
        }
    }

    public MaintenanceFeeConfigDto toDto(MaintenanceFeeConfig c) {
        return MaintenanceFeeConfigDto.builder()
                .id(c.getId())
                .currencyCode(c.getCurrencyCode())
                .feeType(c.getFeeType())
                .feeAmount(c.getFeeAmount())
                .feePercentage(c.getFeePercentage())
                .minimumFee(c.getMinimumFee())
                .maximumFee(c.getMaximumFee())
                .active(c.isActive())
                .createdByAdminId(c.getCreatedByAdminId())
                .updatedByAdminId(c.getUpdatedByAdminId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
