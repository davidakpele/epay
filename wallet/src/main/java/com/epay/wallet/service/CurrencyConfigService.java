package com.epay.wallet.service;

import com.epay.common.exception.ConflictException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.wallet.dto.SupportedCurrencyDTO;
import com.epay.domain.wallet.entity.SupportedCurrency;
import com.epay.domain.wallet.input.CreateCurrencyRequest;
import com.epay.domain.wallet.input.UpdateCurrencyRequest;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConfigService {

    private final SupportedCurrencyRepository currencyRepository;

    @Transactional
    public SupportedCurrencyDTO create(CreateCurrencyRequest request, Long adminId) {
        String code = request.getCode().trim().toUpperCase();

        if (currencyRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Currency " + code + " already exists",
                    ErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        SupportedCurrency currency = SupportedCurrency.builder()
                .code(code)
                .name(request.getName().trim())
                .symbol(request.getSymbol().trim())
                .exchangeRate(request.getExchangeRate())
                .decimalPlaces(request.getDecimalPlaces())
                .minDeposit(request.getMinDeposit())
                .minWithdrawal(request.getMinWithdrawal())
                .active(true)
                .defaultEligible(request.isDefaultEligible())
                .countryCode(request.getCountryCode() != null
                        ? request.getCountryCode().toUpperCase() : null)
                .lastUpdatedBy(adminId)
                .build();

        currencyRepository.save(currency);
        log.info("Currency created: code={} by adminId={}", code, adminId);
        return toDTO(currency);
    }

    @Transactional
    public SupportedCurrencyDTO update(Long currencyId, UpdateCurrencyRequest request, Long adminId) {
        SupportedCurrency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found"));

        if (request.getName() != null)           currency.setName(request.getName().trim());
        if (request.getSymbol() != null)          currency.setSymbol(request.getSymbol().trim());
        if (request.getExchangeRate() != null)    currency.setExchangeRate(request.getExchangeRate());
        if (request.getDecimalPlaces() != null)   currency.setDecimalPlaces(request.getDecimalPlaces());
        if (request.getMinDeposit() != null)      currency.setMinDeposit(request.getMinDeposit());
        if (request.getMinWithdrawal() != null)   currency.setMinWithdrawal(request.getMinWithdrawal());
        if (request.getActive() != null)          currency.setActive(request.getActive());
        if (request.getDefaultEligible() != null) currency.setDefaultEligible(request.getDefaultEligible());
        if (request.getCountryCode() != null)     currency.setCountryCode(request.getCountryCode().toUpperCase());

        currency.setLastUpdatedBy(adminId);
        currencyRepository.save(currency);

        log.info("Currency updated: id={} by adminId={}", currencyId, adminId);
        return toDTO(currency);
    }

    @Transactional
    public void setActive(Long currencyId, boolean active, Long adminId) {
        if (!currencyRepository.existsById(currencyId)) {
            throw new ResourceNotFoundException("Currency not found");
        }
        currencyRepository.setActive(currencyId, active, adminId);
        log.info("Currency {} {}: id={} by adminId={}",
                active ? "enabled" : "disabled", currencyId, currencyId, adminId);
    }

    public List<SupportedCurrencyDTO> getAllActive() {
        return currencyRepository.findByActiveTrue()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<SupportedCurrencyDTO> getAll() {
        return currencyRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public SupportedCurrencyDTO getByCode(String code) {
        return currencyRepository.findByCodeIgnoreCase(code.toUpperCase())
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + code));
    }

    private SupportedCurrencyDTO toDTO(SupportedCurrency c) {
        return SupportedCurrencyDTO.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .symbol(c.getSymbol())
                .exchangeRate(c.getExchangeRate())
                .decimalPlaces(c.getDecimalPlaces())
                .minDeposit(c.getMinDeposit())
                .minWithdrawal(c.getMinWithdrawal())
                .active(c.isActive())
                .defaultEligible(c.isDefaultEligible())
                .countryCode(c.getCountryCode())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}