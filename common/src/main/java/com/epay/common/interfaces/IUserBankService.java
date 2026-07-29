package com.epay.common.interfaces;

import java.util.List;
import java.util.Optional;
import com.epay.domain.bank.dto.PaystackDtos.PayStackBankList;
import com.epay.domain.bank.dto.PaystackDtos.PaystackAccountData;
import com.epay.domain.bank.entity.UserBankList;
 
public interface IUserBankService {
 
    UserBankList createBank(UserBankList bank);
 
    Optional<UserBankList> findById(Long id);
 
    Optional<UserBankList> findByAccountNumber(String accountNumber);
 
    List<UserBankList> findByUserId(Long userId);
 
    void deleteByIds(List<Long> ids);
 
    boolean findByAccountNumberAndBankName(String accountNumber, String bankName);
 
    Optional<UserBankList> findInternal(String accountNumber, String bankCode);
 
    List<PayStackBankList> fetchAllBanks();
 
    PaystackAccountData verifyExternal(String accountNumber, String bankCode);
}
 