package com.epay.investment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.epay.common.interfaces.InvestmentServices;
import com.epay.domain.history.dto.StatusTimeline;
import com.epay.domain.history.entity.Transaction;
import com.epay.domain.history.entity.TransactionHistory;
import com.epay.domain.investment.dto.InvestmentReturnResponse;
import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.enums.InvestmentDuration;
import com.epay.domain.investment.enums.InvestmentStatus;
import com.epay.domain.investment.enums.TransactionType;
import com.epay.domain.investment.input.CreateInvestmentRequest;
import com.epay.domain.investment.repository.InvestmentRepository;
import com.epay.history.repository.TransactionRepository;
import com.epay.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    public Investment createInvestment(CreateInvestmentRequest request) {

        InvestmentPlan plan = getPlan(request.getDuration());

        BigDecimal profit = calculateProfit(
                request.getPrincipal(),
                plan.annualRate(),
                plan.days());

        BigDecimal total = request.getPrincipal().add(profit);

        String referenceId =
                "INV-" + request.getUserId() + "-" + Instant.now().getEpochSecond();

        boolean debited = walletRepository.debitForInvestment(
                request.getUserId(),
                request.getWalletId(),
                request.getCurrencyCode(),
                request.getPrincipal(),
                referenceId
        );

        if (!debited) {
            throw new IllegalStateException(
                    "Insufficient wallet balance or wallet service unavailable.");
        }

        Investment investment = Investment.builder()
                .userId(request.getUserId())
                .walletId(request.getWalletId())
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

        Investment created = investmentRepository.save(investment);

        try {

            historyService.createDeposit(
                    Transaction.builder()
                            .userId(request.getUserId())
                            .walletId(request.getWalletId())
                            .currency(request.getCurrencyCode())
                            .transactionType(TransactionType.DEBITED.toString())
                            .grossAmount(request.getPrincipal())
                            .netAmount(request.getPrincipal())
                            .description(
                                    String.format(
                                            "Investment locked — %s plan at %s%% p.a. (ref: %s)",
                                            request.getDuration(),
                                            plan.annualRate(),
                                            referenceId))
                            .statusTimeline(StatusTimeline)
                            .reference(referenceId)
                            .transactionId(generateId("TXN"))
                            .sessionId(generateId("SESS"))
                            .timestamp(LocalDateTime.now())
                            .build());

        } catch (Exception ex) {

            log.warn(
                    "History creation failed for investment {}",
                    created.getId(),
                    ex);

        }

        return created;
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

    // -------------------------------------------------------------------------
    // Process Matured Investments
    // -------------------------------------------------------------------------

    @Override
    public void processMaturedInvestments() {

        List<Investment> matured =
                investmentRepository.findMaturedUnpaid(Instant.now());

        log.info("Processing {} matured investments", matured.size());

        for (Investment investment : matured) {

            try {

                String reference =
                        "INV-PAYOUT-" +
                                investment.getUserId() +
                                "-" +
                                Instant.now().getEpochSecond();

                boolean credited =
                        walletRepository.creditInvestmentPayout(
                                investment.getUserId(),
                                investment.getWalletId(),
                                investment.getCurrencyCode(),
                                investment.getTotalPayout(),
                                reference
                        );

                if (!credited) {

                    investment.setStatus(InvestmentStatus.FAILED);

                    investmentRepository.save(investment);

                    log.error("Payout failed for investment {}", investment.getId());

                    continue;
                }

                investment.setStatus(InvestmentStatus.PAID_OUT);
                investment.setPaidOutAt(LocalDateTime.now());

                investmentRepository.save(investment);

                try {

                    historyService.createCredit(

                            Transaction.builder()
                                    .userId(investment.getUserId())
                                    .walletId(investment.getWalletId())
                                    .currency(investment.getCurrencyCode())
                                    .transactionType(TransactionType.CREDITED.toString())
                                    .grossAmount(investment.getTotalPayout())
                                    .netAmount(investment.getTotalPayout())
                                    .description(String.format(
                                            "Investment matured — principal %s + profit %s (ref: %s)",
                                            investment.getPrincipal(),
                                            investment.getExpectedProfit(),
                                            reference))
                                    .statusTimeline("SUCCESS")
                                    .referenceId(reference)
                                    .transactionId(generateId("TXN"))
                                    .sessionId(generateId("SESS"))
                                    .timestamp(Instant.now())
                                    .build());

                } catch (Exception ex) {

                    log.warn(
                            "Failed to write payout history for investment {}",
                            investment.getId(),
                            ex);

                }

                log.info(
                        "Payout processed for investment {}, user {}, amount {}",
                        investment.getId(),
                        investment.getUserId(),
                        investment.getTotalPayout());

            } catch (Exception ex) {

                log.error(
                        "Unexpected error processing investment {}",
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

    private record InvestmentPlan(
            BigDecimal annualRate,
            int days
    ) {
    }
}
