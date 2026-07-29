package com.epay.bank.service;

import com.epay.bank.repository.UserBankRepository;
import com.epay.common.interfaces.IUserBankService;
import com.epay.domain.bank.dto.PaystackDtos.PayStackBankList;
import com.epay.domain.bank.dto.PaystackDtos.PaystackAccountData;
import com.epay.domain.bank.dto.PaystackDtos.PaystackBankResponse;
import com.epay.domain.bank.dto.PaystackDtos.PaystackAccountResponse;
import com.epay.domain.bank.entity.UserBankList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserBankService implements IUserBankService {

    private final UserBankRepository                repository;
    @Qualifier("paystackWebClient")
    private final WebClient                         paystackWebClient;

    @Override
    public UserBankList createBank(UserBankList bank) {
        return repository.save(bank);
    }

    @Override
    public Optional<UserBankList> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<UserBankList> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber);
    }

    @Override
    public List<UserBankList> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        repository.deleteAllById(ids);
    }

    @Override
    public boolean findByAccountNumberAndBankName(String accountNumber, String bankName) {
        return repository.existsByAccountNumberAndBankName(accountNumber, bankName);
    }

    @Override
    public Optional<UserBankList> findInternal(String accountNumber, String bankCode) {
        return repository.findByAccountNumberAndBankCode(accountNumber, bankCode);
    }

    @Override
    public List<PayStackBankList> fetchAllBanks() {
        try {
            PaystackBankResponse result = paystackWebClient.get()
                    .uri("/bank")
                    .retrieve()
                    .bodyToMono(PaystackBankResponse.class)
                    .block();

            if (result == null || !result.isStatus()) {
                String msg = result != null ? result.getMessage() : "No response from Paystack";
                log.error("[Bank] fetchAllBanks failed: {}", msg);
                throw new RuntimeException("Failed to fetch bank list: " + msg);
            }

            log.info("[Bank] Fetched {} banks from Paystack", result.getData().size());
            return result.getData();

        } catch (WebClientResponseException e) {
            log.error("[Bank] Paystack HTTP error fetching banks: {} {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Paystack error: " + e.getMessage(), e);
        }
    }

    @Override
    public PaystackAccountData verifyExternal(String accountNumber, String bankCode) {
        try {
            PaystackAccountResponse result = paystackWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bank/resolve")
                            .queryParam("account_number", accountNumber)
                            .queryParam("bank_code", bankCode)
                            .build())
                    .retrieve()
                    .bodyToMono(PaystackAccountResponse.class)
                    .block();

            if (result == null || !result.isStatus()) {
                String msg = result != null ? result.getMessage() : "No response from Paystack";
                log.error("[Bank] verifyExternal failed: account={} bankCode={} reason={}",
                        accountNumber, bankCode, msg);
                throw new RuntimeException("Account verification failed: " + msg);
            }

            log.info("[Bank] Verified account={} bankCode={} name={}",
                    accountNumber, bankCode, result.getData().getAccountName());
            return result.getData();

        } catch (WebClientResponseException e) {
            log.error("[Bank] Paystack HTTP error verifying account: {} {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Paystack error: " + e.getMessage(), e);
        }
    }
}
