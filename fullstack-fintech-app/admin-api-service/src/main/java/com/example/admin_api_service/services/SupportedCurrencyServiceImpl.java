package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ISupportedCurrencyService;
import com.example.admin_api_service.enums.CurrencyStatus;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedCurrency;
import com.example.admin_api_service.repository.SupportedCurrencyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SupportedCurrencyServiceImpl implements ISupportedCurrencyService {

    private final SupportedCurrencyRepository currencyRepository;

    public SupportedCurrencyServiceImpl(SupportedCurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public SupportedCurrency addCurrency(SupportedCurrency currency, String createdBy) {
        if (currencyRepository.existsByCode(currency.getCode())) {
            throw new ConflictException("Currency with code '" + currency.getCode() + "' already exists");
        }
        currency.setUpdatedBy(createdBy);
        currency.setStatus(CurrencyStatus.ACTIVE);
        return currencyRepository.save(currency);
    }

    @Override
    public SupportedCurrency updateCurrency(String code, SupportedCurrency updated, String updatedBy) {
        SupportedCurrency existing = getCurrencyByCode(code);
        existing.setName(updated.getName());
        existing.setSymbol(updated.getSymbol());
        existing.setCurrencyType(updated.getCurrencyType());
        existing.setDecimalPlaces(updated.getDecimalPlaces());
        existing.setMinorUnitName(updated.getMinorUnitName());
        existing.setMinimumBalance(updated.getMinimumBalance());
        existing.setCountryCode(updated.getCountryCode());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return currencyRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportedCurrency getCurrencyByCode(String code) {
        return currencyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("SupportedCurrency", "code", code));
    }

    @Override
    @Transactional(readOnly = true)
    public SupportedCurrency getCurrencyById(String currencyId) {
        return currencyRepository.findById(currencyId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportedCurrency", "id", currencyId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportedCurrency> getAllCurrencies(Pageable pageable) {
        return currencyRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportedCurrency> getActiveCurrencies() {
        return currencyRepository.findAllByStatus(CurrencyStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportedCurrency getBaseCurrency() {
        return currencyRepository.findByIsBaseCurrencyTrue()
                .orElseThrow(() -> new ResourceNotFoundException("SupportedCurrency", "isBaseCurrency", "true"));
    }

    @Override
    public void activateCurrency(String code, String updatedBy) {
        SupportedCurrency currency = getCurrencyByCode(code);
        currency.setStatus(CurrencyStatus.ACTIVE);
        currency.setUpdatedBy(updatedBy);
        currency.setUpdatedOn(LocalDateTime.now());
        currencyRepository.save(currency);
    }

    @Override
    public void suspendCurrency(String code, String updatedBy) {
        SupportedCurrency currency = getCurrencyByCode(code);
        if (currency.isBaseCurrency()) {
            throw new BadRequestException("The base currency cannot be suspended");
        }
        currency.setStatus(CurrencyStatus.SUSPENDED);
        currency.setUpdatedBy(updatedBy);
        currency.setUpdatedOn(LocalDateTime.now());
        currencyRepository.save(currency);
    }

    @Override
    public void setDepositEnabled(String code, boolean enabled, String updatedBy) {
        SupportedCurrency currency = getCurrencyByCode(code);
        currency.setDepositEnabled(enabled);
        currency.setUpdatedBy(updatedBy);
        currency.setUpdatedOn(LocalDateTime.now());
        currencyRepository.save(currency);
    }

    @Override
    public void setWithdrawalEnabled(String code, boolean enabled, String updatedBy) {
        SupportedCurrency currency = getCurrencyByCode(code);
        currency.setWithdrawalEnabled(enabled);
        currency.setUpdatedBy(updatedBy);
        currency.setUpdatedOn(LocalDateTime.now());
        currencyRepository.save(currency);
    }

    @Override
    public void setFxEnabled(String code, boolean enabled, String updatedBy) {
        SupportedCurrency currency = getCurrencyByCode(code);
        currency.setFxEnabled(enabled);
        currency.setUpdatedBy(updatedBy);
        currency.setUpdatedOn(LocalDateTime.now());
        currencyRepository.save(currency);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCurrencyActive(String code) {
        return currencyRepository.findByCode(code)
                .map(c -> c.getStatus() == CurrencyStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDepositEnabled(String code) {
        return currencyRepository.findByCode(code)
                .map(c -> c.getStatus() == CurrencyStatus.ACTIVE && c.isDepositEnabled())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWithdrawalEnabled(String code) {
        return currencyRepository.findByCode(code)
                .map(c -> c.getStatus() == CurrencyStatus.ACTIVE && c.isWithdrawalEnabled())
                .orElse(false);
    }
}
