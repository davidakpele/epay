package com.epay.bank.repository;

import com.epay.domain.bank.entity.UserBankList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBankRepository extends JpaRepository<UserBankList, Long> {

    Optional<UserBankList> findByAccountNumber(String accountNumber);

    List<UserBankList> findByUserId(Long userId);

    boolean existsByAccountNumberAndBankCode(String accountNumber, String bankCode);

    boolean existsByAccountNumberAndBankName(String accountNumber, String bankName);

    Optional<UserBankList> findByAccountNumberAndBankCode(String accountNumber, String bankCode);
}