package com.epay.bills.service;

import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IWithdrawWalletPort;
import com.epay.domain.bills.dto.BillPaymentResponse;
import com.epay.domain.bills.dto.BillPaymentResponse.BillData;
import com.epay.domain.bills.enums.BillService;
import com.epay.domain.bills.enums.BillStatus;
import com.epay.domain.bills.input.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillsService {

    private static final String TX_PREFIX   = "BX";
    private static final String REF_PREFIX  = "BILL";

    private final IWithdrawWalletPort walletPort;
    private final IHistoryPort        historyPort;

    @Transactional
    public ResponseEntity<BillPaymentResponse> payAirtime(AirtimeRequest req) {
        String currency   = req.getCurrency().toUpperCase();
        String recipient  = req.getPhoneNumber();
        String provider   = req.getNetwork() != null ? req.getNetwork().name() : "UNKNOWN";
        String description = "Airtime recharge — " + provider + " " + recipient;

        return process(BillService.AIRTIME, req.getUserId(), req.getAmount(),
                currency, recipient, provider, description,
                "Airtime purchased successfully.");
    }

    @Transactional
    public ResponseEntity<BillPaymentResponse> payData(DataRequest req) {
        String currency   = req.getCurrency().toUpperCase();
        String recipient  = req.getPhoneNumber();
        String provider   = req.getNetwork() != null ? req.getNetwork().name() : "UNKNOWN";
        String description = "Data bundle — " + provider + " " + recipient;

        return process(BillService.DATA, req.getUserId(), req.getAmount(),
                currency, recipient, provider, description,
                "Data bundle purchased successfully.");
    }

    @Transactional
    public ResponseEntity<BillPaymentResponse> payCableTv(CableTvRequest req) {
        String currency   = req.getCurrency().toUpperCase();
        String recipient  = req.getSmartCardNumber();
        String provider   = req.getProvider() != null ? req.getProvider().name() : "UNKNOWN";
        String description = "Cable TV subscription — " + provider
                + " " + req.getPlanName() + " (" + recipient + ")";

        return process(BillService.CABLETV, req.getUserId(), req.getAmount(),
                currency, recipient, provider, description,
                "Cable TV subscription activated successfully.");
    }

    @Transactional
    public ResponseEntity<BillPaymentResponse> payElectricity(ElectricityRequest req) {
        String currency   = req.getCurrency().toUpperCase();
        String recipient  = req.getMeterNumber();
        String provider   = req.getProvider() != null ? req.getProvider().name() : "UNKNOWN";
        String meterType  = req.getMeterType() != null ? req.getMeterType().name() : "";
        String description = "Electricity payment — " + provider
                + " " + meterType + " meter " + recipient;

        return process(BillService.ELECTRICITY, req.getUserId(), req.getAmount(),
                currency, recipient, provider, description,
                "Electricity token generated successfully.");
    }

    @Transactional
    public ResponseEntity<BillPaymentResponse> payBetting(BettingRequest req) {
        String currency   = req.getCurrency().toUpperCase();
        String recipient  = req.getBettingAccountId();
        String provider   = req.getProvider() != null ? req.getProvider().name() : "UNKNOWN";
        String description = "Betting wallet top-up — " + provider + " account " + recipient;

        return process(BillService.BETTING, req.getUserId(), req.getAmount(),
                currency, recipient, provider, description,
                "Betting wallet funded successfully.");
    }

    @Transactional
    public ResponseEntity<BillPaymentResponse> payShopping(ShoppingRequest req) {
        String currency   = req.getCurrency().toUpperCase();
        String recipient  = req.getOrderId();
        String provider   = req.getProvider() != null ? req.getProvider().name() : "MARKETPLACE";
        String description = "Shopping payment — " + provider + " order " + recipient;

        return process(BillService.SHOPPING, req.getUserId(), req.getAmount(),
                currency, recipient, provider, description,
                "Shopping payment processed successfully.");
    }

    private ResponseEntity<BillPaymentResponse> process(
            BillService service,
            Long        userId,
            BigDecimal  amount,
            String      currency,
            String      recipient,
            String      provider,
            String      description,
            String      successMessage) {

        if (!walletPort.walletExists(userId)) {
            return error("No wallet found for this account. Create a wallet first.");
        }
        if (!walletPort.isCurrencySupported(currency)) {
            return error("Unsupported currency: " + currency);
        }

        BigDecimal balance = walletPort.getBalance(userId, currency);
        if (balance.compareTo(amount) < 0) {
            return error(String.format(
                    "Insufficient balance. Available: %s %.2f  Required: %s %.2f",
                    currency, balance, currency, amount));
        }

        String transactionId = generateTxnId();
        String reference     = generateReference(service, userId);
        String currencySymbol = walletPort.getCurrencySymbol(currency);

        BigDecimal previousBalance = balance;
        try {
            walletPort.debitWallet(userId, currency, amount, reference);
        } catch (Exception e) {
            log.error("[Bills] Wallet debit failed userId={} ref={}: {}", userId, reference, e.getMessage());
            return error("Failed to process payment. Please try again.");
        }
        BigDecimal newBalance = walletPort.getBalance(userId, currency);

        try {
            historyPort.record(
                    userId,
                    walletPort.getWalletId(userId),
                    transactionId,
                    reference,
                    "BILL_PAYMENT",
                    "DEBIT",
                    service.name(),
                    "DELIVERED",
                    amount,
                    BigDecimal.ZERO,
                    amount,
                    previousBalance,
                    newBalance,
                    currency,
                    currencySymbol,
                    "Account Holder",
                    description,
                    provider,
                    null, null, null, null, null,
                    null,
                    LocalDateTime.now());
        } catch (Exception e) {
            log.warn("[Bills] History record failed txn={}: {}", transactionId, e.getMessage());
        }

        log.info("[Bills] {} userId={} ref={} amount={} {} status=COMPLETED",
                service, userId, reference, amount, currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                BillPaymentResponse.builder()
                        .status("success")
                        .message(successMessage)
                        .data(BillData.builder()
                                .transactionId(transactionId)
                                .referenceId(reference)
                                .service(service)
                                .provider(provider)
                                .amount(amount)
                                .currency(currency)
                                .recipient(recipient)
                                .status(BillStatus.COMPLETED)
                                .processedAt(Instant.now())
                                .build())
                        .build());
    }

    private ResponseEntity<BillPaymentResponse> error(String message) {
        return ResponseEntity.badRequest().body(
                BillPaymentResponse.builder()
                        .status("failed")
                        .message(message)
                        .build());
    }

    private String generateTxnId() {
        long hash = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return TX_PREFIX + String.valueOf(hash).substring(0, 9);
    }

    private String generateReference(BillService service, Long userId) {
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        return REF_PREFIX + "_" + service.name().substring(0, 3) + "_" + userId + "_" + suffix;
    }
}
