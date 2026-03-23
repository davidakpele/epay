package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ISupportedBankService;
import com.example.admin_api_service.enums.BankStatus;
import com.example.admin_api_service.enums.BankType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.SupportedBank;
import com.example.admin_api_service.repository.SupportedBankRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SupportedBankServiceImpl implements ISupportedBankService {

    private final SupportedBankRepository bankRepository;

    public SupportedBankServiceImpl(SupportedBankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @Override
    public SupportedBank addBank(SupportedBank bank, String createdBy) {
        if (bankRepository.existsByBankCode(bank.getBankCode())) {
            throw new ConflictException("Bank with code '" + bank.getBankCode() + "' already exists");
        }
        bank.setUpdatedBy(createdBy);
        bank.setStatus(BankStatus.ACTIVE);
        return bankRepository.save(bank);
    }

    @Override
    public SupportedBank updateBank(String bankCode, SupportedBank updated, String updatedBy) {
        SupportedBank existing = getBankByCode(bankCode);
        existing.setName(updated.getName());
        existing.setShortName(updated.getShortName());
        existing.setBankType(updated.getBankType());
        existing.setCountryCode(updated.getCountryCode());
        existing.setNibssBankCode(updated.getNibssBankCode());
        existing.setSwiftCode(updated.getSwiftCode());
        existing.setRoutingNumber(updated.getRoutingNumber());
        existing.setLogoUrl(updated.getLogoUrl());
        existing.setNipEnabled(updated.isNipEnabled());
        existing.setNameEnquiryEnabled(updated.isNameEnquiryEnabled());
        existing.setDirectDebitEnabled(updated.isDirectDebitEnabled());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return bankRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportedBank getBankByCode(String bankCode) {
        return bankRepository.findByBankCode(bankCode)
                .orElseThrow(() -> new ResourceNotFoundException("SupportedBank", "bankCode", bankCode));
    }

    @Override
    @Transactional(readOnly = true)
    public SupportedBank getBankById(String bankId) {
        return bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportedBank", "id", bankId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportedBank> getAllBanks(Pageable pageable) {
        return bankRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportedBank> getActiveBanks() {
        return bankRepository.findAllByStatus(BankStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportedBank> getBanksByType(BankType bankType) {
        return bankRepository.findAllByBankType(bankType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportedBank> getBanksByCountry(String countryCode) {
        return bankRepository.findAllByCountryCodeAndStatus(countryCode, BankStatus.ACTIVE);
    }

    @Override
    public void activateBank(String bankCode, String updatedBy) {
        SupportedBank bank = getBankByCode(bankCode);
        bank.setStatus(BankStatus.ACTIVE);
        bank.setTransferEnabled(true);
        bank.setDisabledReason(null);
        bank.setUpdatedBy(updatedBy);
        bank.setUpdatedOn(LocalDateTime.now());
        bankRepository.save(bank);
    }

    @Override
    public void disableTransfers(String bankCode, String updatedBy, String reason) {
        SupportedBank bank = getBankByCode(bankCode);
        bank.setStatus(BankStatus.TRANSFER_DISABLED);
        bank.setTransferEnabled(false);
        bank.setDisabledReason(reason);
        bank.setUpdatedBy(updatedBy);
        bank.setUpdatedOn(LocalDateTime.now());
        bankRepository.save(bank);
    }

    @Override
    public void setDegraded(String bankCode, String updatedBy) {
        SupportedBank bank = getBankByCode(bankCode);
        bank.setStatus(BankStatus.DEGRADED);
        bank.setUpdatedBy(updatedBy);
        bank.setUpdatedOn(LocalDateTime.now());
        bankRepository.save(bank);
    }

    @Override
    public void suspendBank(String bankCode, String updatedBy) {
        SupportedBank bank = getBankByCode(bankCode);
        bank.setStatus(BankStatus.SUSPENDED);
        bank.setTransferEnabled(false);
        bank.setUpdatedBy(updatedBy);
        bank.setUpdatedOn(LocalDateTime.now());
        bankRepository.save(bank);
    }

    @Override
    public void updatePerformanceMetrics(String bankCode, BigDecimal successRate,
                                          Long avgResponseTimeMs) {
        bankRepository.findByBankCode(bankCode).ifPresent(bank -> {
            bank.setSuccessRate(successRate);
            bank.setAvgResponseTimeMs(avgResponseTimeMs);
            bank.setUpdatedOn(LocalDateTime.now());
            bankRepository.save(bank);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTransferEnabled(String bankCode) {
        return bankRepository.findByBankCode(bankCode)
                .map(b -> b.isTransferEnabled() && b.getStatus() == BankStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNipEnabled(String bankCode) {
        return bankRepository.findByBankCode(bankCode)
                .map(b -> b.isNipEnabled() && b.getStatus() == BankStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBankCode(String bankCode) {
        return bankRepository.existsByBankCode(bankCode);
    }
}
