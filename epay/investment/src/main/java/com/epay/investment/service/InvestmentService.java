package com.epay.investment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.exception.WalletException;
import com.epay.common.interfaces.InvestmentServices;
import com.epay.domain.history.entity.Transaction;
import com.epay.domain.history.enums.TransactionStatus;
import com.epay.domain.investment.dto.InvestmentReturnResponse;
import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.enums.InvestmentDuration;
import com.epay.domain.investment.enums.InvestmentStatus;
import com.epay.domain.investment.enums.TransactionType;
import com.epay.domain.investment.input.CreateInvestmentRequest;
import com.epay.domain.investment.repository.InvestmentRepository;
import com.epay.domain.wallet.entity.CurrencyBalance;
import com.epay.domain.wallet.entity.Wallet;
import com.epay.history.repository.TransactionRepository;
import com.epay.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService implements InvestmentServices {

        private static final Map<InvestmentDuration, InvestmentPlan> PLANS = new EnumMap<>(InvestmentDuration.class);

        static {
                PLANS.put(InvestmentDuration.WEEKLY,
                        new InvestmentPlan(BigDecimal.valueOf(8), 7));

                PLANS.put(InvestmentDuration.MONTHLY,
                        new InvestmentPlan(BigDecimal.valueOf(12), 30));

                PLANS.put(InvestmentDuration.QUARTERLY,
                        new InvestmentPlan(BigDecimal.valueOf(18), 90));

                PLANS.put(InvestmentDuration.YEARLY,
                        new InvestmentPlan(BigDecimal.valueOf(24), 365));
        }

        private final InvestmentRepository investmentRepository;
        private final WalletRepository walletRepository;
        private final TransactionRepository historyService;

        private InvestmentPlan getPlan(InvestmentDuration duration) {
                InvestmentPlan plan = PLANS.get(duration);

                if (plan == null) {
                throw new IllegalArgumentException(
                        "Unknown investment duration: " + duration);
                }

                return plan;
        }

        private BigDecimal calculateProfit(
                BigDecimal principal,
                BigDecimal annualRate,
                int days
        ) {

                BigDecimal years = BigDecimal.valueOf(days)
                        .divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP);

                return principal
                        .multiply(annualRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP))
                        .multiply(years)
                        .setScale(4, RoundingMode.HALF_UP);
        }


        @Override
        public InvestmentReturnResponse calculateReturns(
                BigDecimal principal,
                InvestmentDuration duration,
                String currencyCode
        ) {

                InvestmentPlan plan = getPlan(duration);

                BigDecimal profit = calculateProfit(
                        principal,
                        plan.annualRate(),
                        plan.days());

                BigDecimal total = principal.add(profit);

                return InvestmentReturnResponse.builder()
                        .principal(principal)
                        .duration(duration.name())
                        .durationDays(plan.days())
                        .annualRate(plan.annualRate())
                        .expectedProfit(profit)
                        .totalPayout(total)
                        .currencyCode(currencyCode)
                        .maturityDate(LocalDate.now().plusDays(plan.days()))
                        .build();
        }

        @Override
        @Transactional
        public Investment createInvestment(CreateInvestmentRequest request) {
                if (request.getUserId() == null){
                        throw new BadRequestException("User ID is required", ErrorCode.INVALID_INPUT);
                }

                if (request.getWalletId() == null){
                        throw new BadRequestException("Wallet ID is required", ErrorCode.INVALID_INPUT);
                }

                if (request.getCurrencyCode() == null || request.getCurrencyCode().isBlank()) {
                        throw new BadRequestException("Currency code is required", ErrorCode.INVALID_INPUT);
                }

                if (request.getPrincipal() == null || request.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BadRequestException("Investment amount must be greater than zero.", ErrorCode.INVALID_INPUT);
                }

                if (request.getDuration() == null) {
                throw new BadRequestException("Investment duration is required.", ErrorCode.INVALID_INPUT);  
                }

                Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found."));

                if (!wallet.getUserId().equals(request.getUserId())) {
                        throw new BadRequestException("Wallet does not belong to this user.", ErrorCode.WALLET_NOT_FOUND);                
                }

                CurrencyBalance balance = wallet.getBalance(request.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet does not support currency " + request.getCurrencyCode()));

                BigDecimal previousBalance = balance.getBalance();

                if (previousBalance.compareTo(request.getPrincipal()) < 0)
                throw new WalletException(
                        String.format("Insufficient %s balance. Available: %.2f",
                                request.getCurrencyCode(), balance.getBalance()), ErrorCode.INSUFFICIENT_BALANCE);

                
                InvestmentPlan plan = getPlan(request.getDuration());

                BigDecimal profit = calculateProfit(
                        request.getPrincipal(),
                        plan.annualRate(),
                        plan.days());

                BigDecimal total = request.getPrincipal().add(profit);

                String referenceId =
                        "INV-" + request.getUserId() + "-" + Instant.now().getEpochSecond();

                BigDecimal newBalance = previousBalance.subtract(request.getPrincipal());

                balance.setBalance(newBalance);

                walletRepository.save(wallet);
                
                Investment investment = Investment.builder()
                        .userId(request.getUserId())
                        .walletId(wallet.getId())
                        .currencyCode(request.getCurrencyCode())
                        .principal(request.getPrincipal())
                        .returnRate(plan.annualRate())
                        .expectedProfit(profit)
                        .totalPayout(total)
                        .duration(request.getDuration())
                        .durationDays(plan.days())
                        .startDate(LocalDateTime.now())
                        .maturityDate(LocalDateTime.now().plusDays(plan.days()))
                        .status(InvestmentStatus.ACTIVE)
                        .referenceId(referenceId)
                        .build();

                investment = investmentRepository.save(investment);
        try {
                Transaction transaction = Transaction.builder()
                        .transactionId(generateId("TXN"))
                        .reference(referenceId)
                        .userId(request.getUserId())
                        .walletId(wallet.getId())
                        .transactionType(TransactionType.DEBITED.name())
                        .debitCredit("DEBIT")
                        .currency(request.getCurrencyCode())
                        .grossAmount(request.getPrincipal())
                        .netAmount(request.getPrincipal())
                        .previousBalance(previousBalance)
                        .availableBalance(newBalance)
                        .runningBalance(newBalance)
                        .description(String.format(
                                "Investment locked (%s plan @ %s%% p.a.)",
                                request.getDuration(),
                                plan.annualRate()))
                        .build();
                        transaction.advanceStatus(TransactionStatus.INITIATED, "SYSTEM", "Transaction created");
                        transaction.advanceStatus(TransactionStatus.SETTLED, "SYSTEM", "Investment created successfully.");

                        historyService.save(transaction);
                } catch (Exception ex) {
                        log.warn("Failed to save investment transaction history for investment {}", investment.getId(), ex);
                }
                return investment;
        }

        @Override
        public Investment getById(Long id, Long userId) {
                return investmentRepository.findById(id)
                        .filter(i -> i.getUserId().equals(userId))
                        .orElse(null);

        }

        @Override
        public List<Investment> getByUserId(Long userId) {
                return investmentRepository.findByUserId(userId);
        }

        @Override
        @Transactional
        public void processMaturedInvestments() {

                List<Investment> matured = investmentRepository.findMaturedUnpaid(LocalDateTime.now());

                log.info("Processing {} matured investments", matured.size());

                for (Investment investment : matured) {

                        try {
                        String reference ="INV-PAYOUT-" + investment.getUserId() +"-" + Instant.now().getEpochSecond();

                        Wallet wallet = walletRepository.findById(investment.getWalletId())
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Wallet not found for investment " + investment.getId()));

                        CurrencyBalance balance = wallet.getBalance(investment.getCurrencyCode())
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Currency " + investment.getCurrencyCode() + " not found in wallet."));

                        BigDecimal previousBalance = balance.getBalance();

                        BigDecimal newBalance =
                                previousBalance.add(investment.getTotalPayout());

                        balance.setBalance(newBalance);

                        walletRepository.save(wallet);

                        investment.setStatus(InvestmentStatus.PAID_OUT);
                        investment.setPaidOutAt(LocalDateTime.now());

                        investmentRepository.save(investment);

                        try {
                                Transaction transaction = Transaction.builder()
                                        .transactionId(generateId("TXN"))
                                        .reference(reference)
                                        .userId(investment.getUserId())
                                        .walletId(investment.getWalletId())
                                        .transactionType(TransactionType.CREDITED.name())
                                        .debitCredit("CREDIT")
                                        .currency(investment.getCurrencyCode())
                                        .grossAmount(investment.getTotalPayout())
                                        .netAmount(investment.getTotalPayout())
                                        .previousBalance(previousBalance)
                                        .availableBalance(newBalance)
                                        .runningBalance(newBalance)
                                        .description(String.format(
                                                "Investment matured — principal %s + profit %s",
                                                investment.getPrincipal(),
                                                investment.getExpectedProfit()))
                                        .build();

                                transaction.advanceStatus(TransactionStatus.INITIATED,"SYSTEM","Transaction created");
                                transaction.advanceStatus(TransactionStatus.SETTLED,"SYSTEM","Investment payout completed.");

                                historyService.save(transaction);

                        } catch (Exception ex) {

                                log.warn(
                                        "Failed to write payout history for investment {}",
                                        investment.getId(),
                                        ex);

                        }

                        log.info(
                                "Investment {} paid successfully. User={}, Amount={}",
                                investment.getId(),
                                investment.getUserId(),
                                investment.getTotalPayout());

                        } catch (Exception ex) {

                        investment.setStatus(InvestmentStatus.FAILED);
                        investmentRepository.save(investment);

                        log.error(
                                "Failed to process matured investment {}",
                                investment.getId(),
                                ex);

                        }

                }
        }

        private String generateId(String prefix) {

                return (prefix + "_" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .toUpperCase())
                        .substring(0, 20);

        }

        private record InvestmentPlan(BigDecimal annualRate,int days) {
        }
}