package com.epay.wallet.interfaces;

import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import com.epay.domain.wallet.input.InvestmentCreditRequest;
import com.epay.domain.wallet.input.InvestmentDebitRequest;
import com.epay.domain.wallet.input.MaintenanceDebitRequest;
import com.epay.domain.wallet.input.SavingsCreditRequest;
import com.epay.domain.wallet.input.SavingsDebitRequest;
import com.epay.domain.wallet.input.WalletRefundRequest;

public interface IWalletService {
    ResponseEntity<?> getWalletByUserId(Long userId);
    ResponseEntity<?> getWalletByUserIdAndCurrencyType(Long userId, String type);
    ResponseEntity<?> updateBalance(String currency, BigDecimal amount, Long userId, Long walletId);
    ResponseEntity<?> processMaintenanceFee(MaintenanceDebitRequest request);
    ResponseEntity<?> refundWallet(WalletRefundRequest request);
    ResponseEntity<?> processInvestmentDebit(InvestmentDebitRequest request);
    ResponseEntity<?> processInvestmentCredit(InvestmentCreditRequest request);
    ResponseEntity<?> processSavingsDebit(SavingsDebitRequest request);
    ResponseEntity<?> processSavingsCredit(SavingsCreditRequest request);
}
