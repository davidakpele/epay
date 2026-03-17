package com.example.admin_api_service.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.admin_api_service.components.ReferenceGenerator;
import com.example.admin_api_service.components.WalletFundedEvent;
import com.example.admin_api_service.enums.Currency;
import com.example.admin_api_service.enums.LiquidityTransactionType;
import com.example.admin_api_service.enums.TransactionStatus;
import com.example.admin_api_service.enums.WalletStatus;
import com.example.admin_api_service.exceptions.DuplicateTransactionException;
import com.example.admin_api_service.exceptions.InsufficientLiquidityException;
import com.example.admin_api_service.exceptions.WalletNotFoundException;
import com.example.admin_api_service.models.LiquidityTransaction;
import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.payloads.FundWalletRequest;
import com.example.admin_api_service.payloads.RebalanceRequest;
import com.example.admin_api_service.payloads.UpdateThresholdRequest;
import com.example.admin_api_service.payloads.WithdrawWalletRequest;
import com.example.admin_api_service.repository.LiquidityThresholdAlertRepository;
import com.example.admin_api_service.repository.LiquidityTransactionRepository;
import com.example.admin_api_service.repository.SystemWalletRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemWalletService {

    private final SystemWalletRepository systemWalletRepository;
    private final LiquidityTransactionRepository transactionRepository;
    private final LiquidityThresholdAlertRepository alertRepository;
    private final LiquidityAlertService alertService;
    private final ReferenceGenerator referenceGenerator;
    private final ApplicationEventPublisher eventPublisher;

    // ── Wallet provisioning ──────────────────────────────────────

    public SystemWallet provisionWallet(Currency currency, BigDecimal minimumThreshold) {
        if (systemWalletRepository.existsByCurrency(currency)) {
            throw new IllegalStateException("Wallet already exists for: " + currency);
        }
        SystemWallet wallet = SystemWallet.builder()
                .currency(currency)
                .minimumThreshold(minimumThreshold)
                .walletReference(referenceGenerator.generateLiquidityRef())
                .build();
        return systemWalletRepository.save(wallet);
    }

    // ── Fund wallet ──────────────────────────────────────────────

    public LiquidityTransaction fundWallet(FundWalletRequest request, Long adminId) {
        SystemWallet wallet = getWalletByCurrency(request.currency());

        if (transactionRepository.findByReference(request.externalReference()).isPresent()) {
            throw new DuplicateTransactionException(request.externalReference());
        }

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.add(request.amount()));
        wallet.setLastFundedAt(LocalDateTime.now());
        wallet.setLastFundedByAdminId(adminId);
        systemWalletRepository.save(wallet);

        LiquidityTransaction txn = buildTransaction(wallet, LiquidityTransactionType.FUND,
                request.amount(), before, wallet.getBalance(),
                request.externalReference(), request.description(), adminId);
        LiquidityTransaction saved = transactionRepository.save(txn);

        log.info("Wallet funded: currency={}, amount={}, adminId={}", request.currency(), request.amount(), adminId);

        // Re-evaluate threshold — funding might resolve an active alert
        alertService.evaluateAndResolveAlerts(wallet, adminId);

        eventPublisher.publishEvent(new WalletFundedEvent(wallet, request.amount(), adminId));
        return saved;
    }

    // ── Withdraw from wallet ─────────────────────────────────────

    public LiquidityTransaction withdrawFromWallet(WithdrawWalletRequest request, Long adminId) {
        SystemWallet wallet = getWalletByCurrency(request.currency());

        if (wallet.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientLiquidityException(
                String.format("Insufficient available balance. Available: %s, Requested: %s",
                    wallet.getAvailableBalance(), request.amount()));
        }

        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.subtract(request.amount()));
        systemWalletRepository.save(wallet);

        LiquidityTransaction txn = buildTransaction(wallet, LiquidityTransactionType.WITHDRAWAL,
                request.amount(), before, wallet.getBalance(),
                request.externalReference(), request.description(), adminId);
        LiquidityTransaction saved = transactionRepository.save(txn);

        log.info("Wallet withdrawal: currency={}, amount={}, adminId={}", request.currency(), request.amount(), adminId);

        alertService.evaluateThreshold(wallet);
        return saved;
    }

    // ── Rebalance between currencies ─────────────────────────────

    public void rebalance(RebalanceRequest request, Long adminId) {
        SystemWallet from = getWalletByCurrency(request.fromCurrency());
        SystemWallet to   = getWalletByCurrency(request.toCurrency());

        if (from.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientLiquidityException("Insufficient balance in source wallet for rebalance.");
        }

        String sharedRef = referenceGenerator.generateLiquidityRef();
        String desc = request.description() != null ? request.description()
                : String.format("Rebalance from %s to %s", request.fromCurrency(), request.toCurrency());

        // Debit source
        BigDecimal fromBefore = from.getBalance();
        from.setBalance(fromBefore.subtract(request.amount()));
        systemWalletRepository.save(from);
        transactionRepository.save(buildTransaction(from, LiquidityTransactionType.REBALANCE_OUT,
                request.amount(), fromBefore, from.getBalance(), sharedRef, desc, adminId));

        // Credit destination
        BigDecimal toBefore = to.getBalance();
        to.setBalance(toBefore.add(request.amount()));
        systemWalletRepository.save(to);
        transactionRepository.save(buildTransaction(to, LiquidityTransactionType.REBALANCE_IN,
                request.amount(), toBefore, to.getBalance(), sharedRef, desc, adminId));

        alertService.evaluateThreshold(from);
        alertService.evaluateAndResolveAlerts(to, adminId);

        log.info("Rebalance complete: {} -> {}, amount={}", request.fromCurrency(), request.toCurrency(), request.amount());
    }

    // ── Reserve management (called by user transaction engine) ───

    public void reserveBalance(Currency currency, BigDecimal amount, String txnRef) {
        SystemWallet wallet = getWalletByCurrency(currency);
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientLiquidityException("Cannot reserve: insufficient liquidity for " + currency);
        }
        BigDecimal before = wallet.getBalance();
        wallet.setReservedBalance(wallet.getReservedBalance().add(amount));
        systemWalletRepository.save(wallet);

        transactionRepository.save(buildTransaction(wallet, LiquidityTransactionType.RESERVE,
                amount, before, wallet.getBalance(), txnRef, "Reserve for txn: " + txnRef, null));
    }

    public void releaseReserve(Currency currency, BigDecimal amount, String txnRef) {
        SystemWallet wallet = getWalletByCurrency(currency);
        BigDecimal before = wallet.getBalance();
        wallet.setReservedBalance(wallet.getReservedBalance().subtract(amount).max(BigDecimal.ZERO));
        systemWalletRepository.save(wallet);

        transactionRepository.save(buildTransaction(wallet, LiquidityTransactionType.RELEASE,
                amount, before, wallet.getBalance(), txnRef, "Release reserve: " + txnRef, null));
    }

    public void settleReserve(Currency currency, BigDecimal amount, String txnRef) {
        SystemWallet wallet = getWalletByCurrency(currency);
        BigDecimal before = wallet.getBalance();
        wallet.setBalance(before.subtract(amount));
        wallet.setReservedBalance(wallet.getReservedBalance().subtract(amount).max(BigDecimal.ZERO));
        systemWalletRepository.save(wallet);

        transactionRepository.save(buildTransaction(wallet, LiquidityTransactionType.SETTLEMENT,
                amount, before, wallet.getBalance(), txnRef, "Settlement for txn: " + txnRef, null));

        alertService.evaluateThreshold(wallet);
    }

    // ── Threshold update ─────────────────────────────────────────

    public SystemWallet updateThreshold(UpdateThresholdRequest request, Long adminId) {
        SystemWallet wallet = getWalletByCurrency(request.currency());
        wallet.setMinimumThreshold(request.minimumThreshold());
        SystemWallet saved = systemWalletRepository.save(wallet);
        alertService.evaluateThreshold(wallet);
        log.info("Threshold updated: currency={}, threshold={}, adminId={}", request.currency(), request.minimumThreshold(), adminId);
        return saved;
    }

    // ── Wallet status toggle ─────────────────────────────────────

    public SystemWallet setWalletStatus(Currency currency, WalletStatus newStatus, Long adminId) {
        SystemWallet wallet = getWalletByCurrency(currency);
        wallet.setStatus(newStatus);
        log.info("Wallet status changed: currency={}, status={}, adminId={}", currency, newStatus, adminId);
        return systemWalletRepository.save(wallet);
    }

    // ── Liability sync (called when user wallet balances change) ─

    public void syncUserLiabilities(Currency currency, BigDecimal totalLiabilities) {
        SystemWallet wallet = getWalletByCurrency(currency);
        wallet.setTotalUserLiabilities(totalLiabilities);
        systemWalletRepository.save(wallet);
        alertService.evaluateThreshold(wallet);
    }

    // ── Queries ──────────────────────────────────────────────────

    @Transactional
    public SystemWallet getWalletByCurrency(Currency currency) {
        return systemWalletRepository.findByCurrency(currency)
                .orElseThrow(() -> new WalletNotFoundException(currency));
    }

    @Transactional
    public List<SystemWallet> getAllWallets() {
        return systemWalletRepository.findAll();
    }

    @Transactional
    public List<LiquidityTransaction> getTransactionHistory(Currency currency) {
        SystemWallet wallet = getWalletByCurrency(currency);
        return transactionRepository.findBySystemWallet(wallet);
    }


    private LiquidityTransaction buildTransaction(SystemWallet wallet,
            LiquidityTransactionType type, BigDecimal amount,
            BigDecimal before, BigDecimal after,
            String reference, String description, Long adminId) {
        return LiquidityTransaction.builder()
                .systemWallet(wallet)
                .type(type)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .reference(reference != null ? reference : referenceGenerator.generateLiquidityRef())
                .externalReference(reference)
                .description(description)
                .performedByAdminId(adminId)
                .initiatedByAdminId(adminId)
                .reservedBalanceAfter(wallet.getReservedBalance())
                .status(TransactionStatus.SUCCESS)
                .build();
    }
}
