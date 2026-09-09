package com.epay.wallet.service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ConflictException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.exception.WalletException;
import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.IMaintenanceUsagePort;
import com.epay.common.interfaces.IDebtRecoveryPort;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.wallet.dto.WalletBalanceDTO;
import com.epay.domain.wallet.dto.WalletSection;
import com.epay.domain.wallet.entity.CurrencyBalance;
import com.epay.domain.wallet.entity.SupportedCurrency;
import com.epay.domain.wallet.entity.Wallet;
import com.epay.domain.wallet.entity.WalletSettings;
import com.epay.domain.wallet.input.AddCurrencyRequest;
import com.epay.domain.wallet.input.ChangePinRequest;
import com.epay.domain.wallet.input.CreateWalletRequest;
import com.epay.domain.wallet.input.InvestmentCreditRequest;
import com.epay.domain.wallet.input.InvestmentDebitRequest;
import com.epay.domain.wallet.input.MaintenanceDebitRequest;
import com.epay.domain.wallet.input.SavingsCreditRequest;
import com.epay.domain.wallet.input.SavingsDebitRequest;
import com.epay.domain.wallet.input.SetPinRequest;
import com.epay.domain.wallet.input.SwapRequest;
import com.epay.domain.wallet.input.TransferRequest;
import com.epay.domain.wallet.input.WalletRefundRequest;
import com.epay.wallet.cache.WalletCacheService;
import com.epay.wallet.interfaces.IWalletService;
import com.epay.wallet.repository.CurrencyBalanceRepository;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import com.epay.wallet.repository.WalletRepository;
import com.epay.wallet.repository.WalletSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService implements IWalletService {

    private final WalletRepository             walletRepository;
    private final CurrencyBalanceRepository    currencyBalanceRepository;
    private final WalletSettingsRepository     walletSettingsRepository;
    private final SupportedCurrencyRepository  supportedCurrencyRepository;
    private final WalletCacheService           walletCacheService;
    private final UserLookupPort               userLookupPort;
    private final PasswordEncoder              passwordEncoder;
    private final IWalletNotificationPublisher notificationPublisher;
    private final IHistoryPort                 historyPort;
    private final IMaintenanceUsagePort        maintenanceUsagePort;
    private final IDebtRecoveryPort            debtRecoveryPort;

    private static final DateTimeFormatter EVT_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");

    private String formatNow() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(EVT_FMT);
    }

    @Override
    public ResponseEntity<?> getWalletByUserId(Long userId) {
        validateUserId(userId);
        requireActiveUser(userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        List<WalletBalanceDTO> balanceDTOs = wallet.getBalances().stream()
                .map(this::toBalanceDTO).collect(Collectors.toList());

        boolean pinSet = walletSettingsRepository.findByWalletId(wallet.getId())
                .map(WalletSettings::isIsSecure).orElse(false);

        WalletSection section = new WalletSection();
        section.setWalletId(wallet.getId());
        section.setUserId(wallet.getUserId());
        section.setHasTransferPin(pinSet);
        section.setWallet_balances(balanceDTOs);
        return ResponseEntity.ok(section);
    }

    @Override
    public ResponseEntity<?> getWalletByUserIdAndCurrencyType(Long userId, String type) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (type == null || type.isBlank())
            throw new BadRequestException("Currency type is required", ErrorCode.INVALID_INPUT);

        String code = type.trim().toUpperCase();
        requireActiveCurrency(code);

        Wallet wallet = walletRepository.findByUserIdAndCurrencyCode(userId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        return ResponseEntity.ok(toBalanceDTO(balance));
    }


    @Transactional
    public ResponseEntity<?> createWallet(CreateWalletRequest request) {
        validateUserId(request.getUserId());

        if (walletRepository.existsByUserId(request.getUserId()))
            throw new ConflictException("Wallet already exists for this user",
                    ErrorCode.WALLET_ALREADY_EXISTS);

        String defaultCode = request.getDefaultCurrency() != null
                ? request.getDefaultCurrency().trim().toUpperCase()
                : "NGN";

        List<SupportedCurrency> activeCurrencies = supportedCurrencyRepository.findByActiveTrue();

        if (activeCurrencies.isEmpty())
            throw new BadRequestException("No supported currencies configured",
                    ErrorCode.CURRENCY_NOT_SUPPORTED);

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .active(true)
                .build();

        walletRepository.save(wallet);

        for (SupportedCurrency currency : activeCurrencies) {
            boolean isDefault = currency.getCode().equalsIgnoreCase(defaultCode);
            CurrencyBalance cb = CurrencyBalance.builder()
                    .currencyCode(currency.getCode())
                    .currencySymbol(currency.getSymbol())
                    .balance(BigDecimal.ZERO)
                    .isDefault(isDefault)
                    .wallet(wallet)
                    .build();
            currencyBalanceRepository.save(cb);
            wallet.getBalances().add(cb);
        }

        @SuppressWarnings("null")
        List<String> codes = activeCurrencies.stream()
                .map(SupportedCurrency::getCode)
                .toList();
        walletCacheService.warmFromWallet(request.getUserId(), wallet, null, defaultCode, codes);

        log.info("Wallet created: userId={} currencies={}", request.getUserId(), codes);
        return ResponseEntity.status(201).build();
    }

    @Transactional
    public ResponseEntity<?> addCurrency(Long userId, AddCurrencyRequest request) {
        validateUserId(userId);
        requireActiveUser(userId);

        String code = request.getCurrencyCode().trim().toUpperCase();
        SupportedCurrency currency = requireActiveCurrency(code);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.hasCurrency(code))
            throw new ConflictException("Wallet already has currency: " + code,
                    ErrorCode.RESOURCE_ALREADY_EXISTS);

        CurrencyBalance newBalance = CurrencyBalance.builder()
                .currencyCode(code)
                .currencySymbol(currency.getSymbol())
                .balance(BigDecimal.ZERO)
                .isDefault(false)
                .wallet(wallet)
                .build();
        currencyBalanceRepository.save(newBalance);

        walletCacheService.updateBalance(userId, code, BigDecimal.ZERO, BigDecimal.ZERO, newTxnId());
        log.info("Currency added: userId={} currency={}", userId, code);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> setDefaultCurrency(Long userId, String currencyCode) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (currencyCode == null || currencyCode.isBlank())
            throw new BadRequestException("Currency code is required", ErrorCode.INVALID_INPUT);

        String code = currencyCode.trim().toUpperCase();
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.hasCurrency(code))
            throw new BadRequestException(code + " not in wallet — add it first",
                    ErrorCode.INVALID_CURRENCY);

        CurrencyBalance newDefault = currencyBalanceRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        currencyBalanceRepository.clearDefaultForWallet(wallet.getId());
        currencyBalanceRepository.setDefault(newDefault.getId());

        walletCacheService.evict(userId);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> setPin(Long userId, SetPinRequest request, Authentication authentication) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (!request.getPin().equals(request.getConfirmPin()))
            throw new BadRequestException("PINs do not match", ErrorCode.INVALID_INPUT);

        Optional<WalletSettings> settingsOpt = walletSettingsRepository.findByWalletId(request.getWalletId());
        final boolean isUpdate = settingsOpt.isPresent() && settingsOpt.get().isIsSecure();

        WalletSettings settings = settingsOpt.orElseGet(() -> {
            Wallet w = walletRepository.findById(request.getWalletId())
                    .orElseThrow(() -> new WalletException("Wallet not found.", ErrorCode.RESOURCE_NOT_FOUND));
            WalletSettings s = new WalletSettings();
            s.setWallet(w);
            return s;
        });

        settings.setPassword(passwordEncoder.encode(request.getPin()));
        settings.setIsSecure(true);
        walletSettingsRepository.save(settings);

        final String action     = isUpdate ? "UPDATED" : "CREATED";
        final String actionTime = formatNow();
        final String username   = authentication != null ? authentication.getName() : "";

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishWalletPinAlert(null, username, action, actionTime, null, null);
            } catch (Exception ex) {
                log.warn("[WalletPinAlert] Failed to publish: {}", ex.getMessage());
            }
        });

        walletCacheService.evict(userId);
        log.info("PIN set: userId={}", userId);
        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(
                "Wallet PIN set successfully",
                HttpStatus.CREATED.value()
        ));
    }

    @Transactional
    public ResponseEntity<?> changePin(Long userId, ChangePinRequest request) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (!request.getNewPin().equals(request.getConfirmNewPin()))
            throw new BadRequestException("New PINs do not match", ErrorCode.INVALID_INPUT);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Optional<WalletSettings> settings = walletSettingsRepository.findByWalletId(wallet.getId());

        if (!settings.get().getIsSecure())
            throw new BadRequestException("No PIN set — use set PIN first",
                    ErrorCode.OPERATION_NOT_ALLOWED);

        if (!passwordEncoder.matches(request.getCurrentPin(), settings.get().getPassword()))
            throw new WalletException("Current PIN is incorrect", ErrorCode.INVALID_PIN);

        WalletSettings updateWalletSettings = settings.get();

        updateWalletSettings.setPassword(passwordEncoder.encode(request.getNewPin()));
        walletSettingsRepository.save(updateWalletSettings);
        walletCacheService.evict(userId);
        log.info("PIN changed: userId={}", userId);
        return ResponseEntity.ok().build();
    }


    @Transactional
    public ResponseEntity<?> setWalletActive(Long userId, boolean active, Long adminId) {
        validateUserId(userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        wallet.setActive(active);
        walletRepository.save(wallet);
        walletCacheService.evict(userId);
        log.info("Wallet {} by adminId={} for userId={}", active ? "unfrozen" : "frozen", adminId, userId);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> transfer(TransferRequest request, Long senderUserId) {
        validateAmount(request.getAmount());
        if (request.getCurrency() == null || request.getCurrency().isBlank())
            throw new BadRequestException("Currency is required", ErrorCode.INVALID_CURRENCY);
        if (request.getRecipientUsername() == null || request.getRecipientUsername().isBlank())
            throw new BadRequestException("Recipient username is required", ErrorCode.INVALID_INPUT);
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank())
            throw new BadRequestException("Idempotency key is required", ErrorCode.INVALID_INPUT);

        String code = request.getCurrency().trim().toUpperCase();
        requireActiveCurrency(code);
        requireActiveUser(senderUserId);

        Long recipientUserId = userLookupPort.findUserIdByUsername(request.getRecipientUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recipient user does not exist in our system."));

        if (senderUserId.equals(recipientUserId))
            throw new BadRequestException("Cannot transfer to yourself", ErrorCode.INVALID_INPUT);

        Wallet senderWallet = walletRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found"));

        if (!senderWallet.isActive())
            throw new WalletException("Sender wallet is locked", ErrorCode.WALLET_LOCKED);

        @SuppressWarnings("null")
        boolean pinSet = walletSettingsRepository.findByWalletId(senderWallet.getId())
            .map(WalletSettings::isIsSecure).orElse(false);

        if (!pinSet)
            throw new WalletException("Transaction PIN not set", ErrorCode.INVALID_PIN);

        Optional<WalletSettings> walletSettings = walletSettingsRepository.findByWalletId(senderWallet.getId());

        if (!passwordEncoder.matches(request.getTransactionPin(), walletSettings.get().getPassword()))
            throw new WalletException("Invalid transaction PIN", ErrorCode.INVALID_PIN);

        CurrencyBalance senderBalance = senderWallet.getBalance(code)
                .orElseThrow(() -> new WalletException(
                        "You don't have a " + code + " wallet", ErrorCode.WALLET_NOT_FOUND));

        if (senderBalance.getBalance().compareTo(request.getAmount()) < 0)
            throw new WalletException(
                    String.format("Insufficient %s balance. Available: %.2f",
                            code, senderBalance.getBalance()), ErrorCode.INSUFFICIENT_BALANCE);

        Wallet recipientWallet = walletRepository.findByUserId(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient wallet not found"));

        if (!recipientWallet.isActive())
            throw new WalletException("Recipient wallet is not available", ErrorCode.WALLET_SUSPENDED);

        BigDecimal newSenderBal = senderBalance.getBalance().subtract(request.getAmount());
        senderBalance.setBalance(newSenderBal);

        SupportedCurrency currency = supportedCurrencyRepository.findByCodeIgnoreCase(code).get();
        CurrencyBalance recipientBalance = recipientWallet.getBalance(code).orElseGet(() -> {
            CurrencyBalance nb = CurrencyBalance.builder()
                    .currencyCode(code).currencySymbol(currency.getSymbol())
                    .balance(BigDecimal.ZERO).isDefault(false).build();
            recipientWallet.addCurrency(nb);
            return nb;
        });
        BigDecimal newRecipientBal = recipientBalance.getBalance().add(request.getAmount());
        recipientBalance.setBalance(newRecipientBal);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        String txnId = newTxnId();
        walletCacheService.updateBalance(senderUserId, code, newSenderBal, newSenderBal, txnId);
        walletCacheService.updateBalance(recipientUserId, code, newRecipientBal, newRecipientBal, txnId);

        // Track monthly activity for maintenance fee calculation (fire-and-forget)
        CompletableFuture.runAsync(() -> {
            try {
                maintenanceUsagePort.recordActivity(senderUserId, code, request.getAmount(), "TRANSFER_DEBIT");
                maintenanceUsagePort.recordActivity(recipientUserId, code, request.getAmount(), "TRANSFER_CREDIT");
            } catch (Exception ex) {
                log.warn("[MaintenanceUsage] Transfer tracking failed: {}", ex.getMessage());
            }
        });

        final BigDecimal prevSenderBal = senderBalance.getBalance().add(request.getAmount());
        final BigDecimal prevRecipientBal = recipientBalance.getBalance().subtract(request.getAmount());
        final String senderFullName   = userLookupPort.findFullNameByUserId(senderUserId).orElse("Sender");
        final String recipientFullName = userLookupPort.findFullNameByUserId(recipientUserId).orElse("Recipient");
        final String symbol = currency.getSymbol();
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(senderUserId, senderWallet.getId(), txnId, request.getIdempotencyKey(),
                        "TRANSFER_DEBIT", "DEBIT", "INTERNAL", "DELIVERED",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        prevSenderBal, newSenderBal, code, symbol,
                        senderFullName, "TRANSFER TO " + recipientFullName,
                        recipientFullName, recipientUserId, recipientWallet.getId(),
                        null, null, null, null, java.time.LocalDateTime.now());
                historyPort.record(recipientUserId, recipientWallet.getId(), txnId + "_CR", request.getIdempotencyKey() + "_CR",
                        "TRANSFER_CREDIT", "CREDIT", "INTERNAL", "DELIVERED",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        prevRecipientBal, newRecipientBal, code, symbol,
                        recipientFullName, "TRANSFER FROM " + senderFullName,
                        senderFullName, senderUserId, senderWallet.getId(),
                        null, null, null, null, java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[Transfer] History failed txn={}: {}", txnId, ex.getMessage());
            }
        });

        final String finalCode  = code;
        final BigDecimal amount = request.getAmount();
        final String recipient  = request.getRecipientUsername();
        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishDebitNotification(
                        null, BigDecimal.ZERO, amount, "You", recipient,
                        newSenderBal, finalCode, txnId, newSenderBal.add(amount));
                notificationPublisher.publishCreditNotification(
                        null, amount, "Sender", recipient,
                        newRecipientBal, finalCode, txnId, newRecipientBal.subtract(amount));
            } catch (Exception ex) {
                log.warn("[Transfer] Notification failed txn={}: {}", txnId, ex.getMessage());
            }
        });

        log.info("Transfer: sender={} recipient={} currency={} amount={} ref={}",
                senderUserId, recipientUserId, code, amount, request.getIdempotencyKey());
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateBalance(String currency, BigDecimal amount,
                                        Long userId, Long walletId) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (walletId == null || walletId <= 0)
            throw new BadRequestException("Valid wallet ID is required", ErrorCode.INVALID_INPUT);
        if (currency == null || currency.isBlank())
            throw new BadRequestException("Currency is required", ErrorCode.INVALID_CURRENCY);
        validateDelta(amount);

        String code = currency.trim().toUpperCase();
        SupportedCurrency supported = requireActiveCurrency(code);

        Wallet wallet = resolveWallet(userId, walletId);

        CurrencyBalance balance = currencyBalanceRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), code)
                .orElseGet(() -> {
                    CurrencyBalance nb = CurrencyBalance.builder()
                            .currencyCode(code)
                            .currencySymbol(supported.getSymbol())
                            .balance(BigDecimal.ZERO)
                            .isDefault(false)
                            .wallet(wallet)
                            .build();
                    return currencyBalanceRepository.save(nb);
                });

        BigDecimal newBalance = balance.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0)
            throw new WalletException("Insufficient balance", ErrorCode.INSUFFICIENT_BALANCE);

        // ── Debt recovery intercept ────────────────────────────────────────────
        // If this is an incoming credit (amount > 0) and the user has an outstanding
        // maintenance fee debt on this currency, deduct the debt first and only credit
        // the net remainder to the wallet.  Both operations are in the same transaction.
        BigDecimal creditAmount = amount;
        if (amount.compareTo(BigDecimal.ZERO) > 0 && maintenanceUsagePort.hasActiveDebt(userId, code)) {
            creditAmount = debtRecoveryPort.recoverOnCredit(userId, code, amount, walletId);
        }
        BigDecimal finalBalance = balance.getBalance().add(creditAmount);
        // ──────────────────────────────────────────────────────────────────────

        currencyBalanceRepository.updateBalance(balance.getId(), finalBalance);

        walletCacheService.updateBalance(userId, code, finalBalance, finalBalance, newTxnId());
        log.info("Balance updated: userId={} walletId={} currency={} delta={} new={}",
                userId, walletId, code, creditAmount, finalBalance);

        // Track usage for maintenance fee (any balance touch counts)
        CompletableFuture.runAsync(() -> {
            try {
                maintenanceUsagePort.recordActivity(userId, code, amount.abs(), "BALANCE_UPDATE");
            } catch (Exception ex) {
                log.warn("[MaintenanceUsage] updateBalance tracking failed: {}", ex.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    private void validateDelta(BigDecimal amount) {
        if (amount == null)
            throw new BadRequestException("Amount is required", ErrorCode.INVALID_AMOUNT);
        if (amount.compareTo(BigDecimal.ZERO) == 0)
            throw new BadRequestException("Amount must not be zero", ErrorCode.INVALID_AMOUNT);
    }

    @Override
    @Transactional
    public ResponseEntity<?> refundWallet(WalletRefundRequest request) {
        if (request == null)
            throw new BadRequestException("Request is required", ErrorCode.INVALID_INPUT);
        validateUserId(request.getSenderId());
        requireActiveUser(request.getSenderId());
        if (request.getCurrencyCode() == null || request.getCurrencyCode().isBlank())
            throw new BadRequestException("Currency code is required", ErrorCode.INVALID_CURRENCY);
        validateAmount(request.getAmount());

        String code = request.getCurrencyCode().trim().toUpperCase();
        requireActiveCurrency(code);

        Wallet wallet = walletRepository.findByUserId(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        if (!wallet.isActive())
            throw new WalletException("Wallet is locked", ErrorCode.WALLET_LOCKED);

        CurrencyBalance balance = currencyBalanceRepository
                .findByWalletIdAndCurrencyCode(wallet.getId(), code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        currencyBalanceRepository.updateBalance(balance.getId(), newBalance);

        walletCacheService.updateBalance(request.getSenderId(), code, newBalance, newBalance, newTxnId());
        log.info("Refund: userId={} currency={} amount={}", request.getSenderId(), code, request.getAmount());
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> processMaintenanceFee(MaintenanceDebitRequest request) {
        validateTxRequest(request.getUserId(), request.getWalletId(),
                request.getCurrencyType(), request.getAmount(), request.getReferenceNo());
        requireActiveUser(request.getUserId());

        String code = request.getCurrencyType().trim().toUpperCase();
        Wallet wallet = resolveWallet(request.getUserId(), request.getWalletId());
        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        if (balance.getBalance().compareTo(request.getAmount()) < 0)
            throw new WalletException(
                    String.format("Insufficient balance. Required: %s %.2f, Available: %s %.2f",
                            code, request.getAmount(), code, balance.getBalance()),
                    ErrorCode.INSUFFICIENT_BALANCE);

        BigDecimal prevMaint = balance.getBalance().add(request.getAmount());
        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);
        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, newTxnId());

        String maintTxnId = newTxnId();
        SupportedCurrency maintCurrency = supportedCurrencyRepository.findByCodeIgnoreCase(code).orElse(null);
        String symbol = maintCurrency != null ? maintCurrency.getSymbol() : code;
        String holder = userLookupPort.findFullNameByUserId(request.getUserId()).orElse("Account Holder");
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(request.getUserId(), request.getWalletId(),
                        maintTxnId, request.getReferenceNo(),
                        "FEE", "DEBIT", "SYSTEM", "SUCCESS",
                        request.getAmount(), request.getAmount(), request.getAmount(),
                        prevMaint, newBalance, code, symbol, holder,
                        "MAINTENANCE FEE DEDUCTION",
                        null, null, null, null, null, null,
                        request.getDescription(), java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[Maintenance] History failed: {}", ex.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> processInvestmentDebit(InvestmentDebitRequest request) {
        validateTxRequest(request.getUserId(), request.getWalletId(),
                request.getCurrencyType(), request.getAmount(), request.getReferenceNo());
        requireActiveUser(request.getUserId());

        String code = request.getCurrencyType().trim().toUpperCase();
        Wallet wallet = resolveWallet(request.getUserId(), request.getWalletId());
        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        if (balance.getBalance().compareTo(request.getAmount()) < 0)
            throw new WalletException("Insufficient balance for investment debit",
                    ErrorCode.INSUFFICIENT_BALANCE);

        BigDecimal prevInvDebit = balance.getBalance();
        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);
        String invDebitTxnId = newTxnId();
        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, invDebitTxnId);
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(request.getUserId(), request.getWalletId(),
                        invDebitTxnId, request.getReferenceNo(),
                        "INVESTMENT_DEBIT", "DEBIT", "SYSTEM", "SUCCESS",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        prevInvDebit, newBalance, code, code, null,
                        "INVESTMENT DEBIT", null, null, null, null, null, null,
                        request.getDescription(), java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[InvDebit] History failed: {}", ex.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> processInvestmentCredit(InvestmentCreditRequest request) {
        validateTxRequest(request.getUserId(), request.getWalletId(),
                request.getCurrencyType(), request.getAmount(), request.getReferenceNo());
        requireActiveUser(request.getUserId());

        String code = request.getCurrencyType().trim().toUpperCase();
        Wallet wallet = resolveWallet(request.getUserId(), request.getWalletId());
        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        BigDecimal prevInvCredit = balance.getBalance();
        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);
        String invCreditTxnId = newTxnId();
        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, invCreditTxnId);
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(request.getUserId(), request.getWalletId(),
                        invCreditTxnId, request.getReferenceNo(),
                        "INVESTMENT_CREDIT", "CREDIT", "SYSTEM", "SUCCESS",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        prevInvCredit, newBalance, code, code, null,
                        "INVESTMENT CREDIT", null, null, null, null, null, null,
                        request.getDescription(), java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[InvCredit] History failed: {}", ex.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> processSavingsDebit(SavingsDebitRequest request) {
        validateTxRequest(request.getUserId(), request.getWalletId(),
                request.getCurrencyType(), request.getAmount(), request.getReferenceNo());
        requireActiveUser(request.getUserId());

        String code = request.getCurrencyType().trim().toUpperCase();
        Wallet wallet = resolveWallet(request.getUserId(), request.getWalletId());
        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        if (balance.getBalance().compareTo(request.getAmount()) < 0)
            throw new WalletException("Insufficient balance for savings debit",
                    ErrorCode.INSUFFICIENT_BALANCE);

        BigDecimal prevSavDebit = balance.getBalance();
        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);
        String savDebitTxnId = newTxnId();
        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, savDebitTxnId);
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(request.getUserId(), request.getWalletId(),
                        savDebitTxnId, request.getReferenceNo(),
                        "SAVINGS_DEBIT", "DEBIT", "SYSTEM", "SUCCESS",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        prevSavDebit, newBalance, code, code, null,
                        "SAVINGS DEBIT", null, null, null, null, null, null,
                        request.getDescription(), java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[SavingsDebit] History failed: {}", ex.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> processSavingsCredit(SavingsCreditRequest request) {
        validateTxRequest(request.getUserId(), request.getWalletId(),
                request.getCurrencyType(), request.getAmount(), request.getReferenceNo());
        requireActiveUser(request.getUserId());

        String code = request.getCurrencyType().trim().toUpperCase();
        Wallet wallet = resolveWallet(request.getUserId(), request.getWalletId());
        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));

        BigDecimal prevSavCredit = balance.getBalance();
        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);
        String savCreditTxnId = newTxnId();
        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, savCreditTxnId);
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(request.getUserId(), request.getWalletId(),
                        savCreditTxnId, request.getReferenceNo(),
                        "SAVINGS_CREDIT", "CREDIT", "SYSTEM", "SUCCESS",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        prevSavCredit, newBalance, code, code, null,
                        "SAVINGS CREDIT", null, null, null, null, null, null,
                        request.getDescription(), java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[SavingsCredit] History failed: {}", ex.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> swapCurrency(Long userId, SwapRequest request) {
        validateUserId(userId);
        requireActiveUser(userId);

        if (!Boolean.TRUE.equals(request.getAcceptRate()))
            throw new BadRequestException(
                    "You must accept the exchange rate to proceed", ErrorCode.INVALID_INPUT);

        String fromCode = request.getCurrency().trim().toUpperCase();
        String toCode   = request.getTargetWallet().trim().toUpperCase();

        if (fromCode.equals(toCode))
            throw new BadRequestException(
                    "Source and target currencies must be different", ErrorCode.INVALID_INPUT);

        validateAmount(request.getAmount());

        BigDecimal exchangeRate;
        try {
            exchangeRate = getExchangeRate(fromCode, toCode);
        } catch (RuntimeException e) {
            throw new BadRequestException(e.getMessage(), ErrorCode.INVALID_CURRENCY);
        }

        Map<String, Object> swapAmounts = calculateSwapAmounts(request.getAmount(), exchangeRate);
        BigDecimal convertedAmount = (BigDecimal) swapAmounts.get("finalAmount");
        BigDecimal feePercentage   = (BigDecimal) swapAmounts.get("feePercentage");

        BigDecimal sourceFee  = request.getAmount().multiply(feePercentage)
                .setScale(8, java.math.RoundingMode.HALF_UP);
        BigDecimal totalDebit = request.getAmount().add(sourceFee);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.isActive())
            throw new WalletException("Wallet is locked", ErrorCode.WALLET_LOCKED);

        Optional<WalletSettings> settingsOpt = walletSettingsRepository.findByWalletId(wallet.getId());
        if (settingsOpt.isEmpty() || !settingsOpt.get().getIsSecure())
            throw new WalletException("Transaction PIN not set. Please set a PIN first.",
                    ErrorCode.INVALID_PIN);

        if (!passwordEncoder.matches(request.getTransactionPin(), settingsOpt.get().getPassword()))
            throw new WalletException("Invalid transaction PIN", ErrorCode.INVALID_PIN);

        CurrencyBalance fromBalance = wallet.getBalance(fromCode)
                .orElseThrow(() -> new WalletException(
                        "You don't have a " + fromCode + " balance", ErrorCode.WALLET_NOT_FOUND));

        if (fromBalance.getBalance().compareTo(totalDebit) < 0)
            throw new WalletException(
                    String.format("Insufficient %s balance. Available: %.4f, Required: %.4f (inc. %.0f%% fee)",
                            fromCode, fromBalance.getBalance(), totalDebit,
                            feePercentage.multiply(new BigDecimal("100")).doubleValue()),
                    ErrorCode.INSUFFICIENT_BALANCE);

        @SuppressWarnings("null")
        String toSymbol = supportedCurrencyRepository.findByCodeIgnoreCase(toCode)
                .map(SupportedCurrency::getSymbol)
                .orElse(toCode);
        @SuppressWarnings("null")
        String fromSymbol = supportedCurrencyRepository.findByCodeIgnoreCase(fromCode)
                .map(SupportedCurrency::getSymbol)
                .orElse(fromCode);

        BigDecimal prevFromBalance = fromBalance.getBalance();
        BigDecimal newFromBalance  = prevFromBalance.subtract(totalDebit);
        fromBalance.setBalance(newFromBalance);

        CurrencyBalance toBalance = wallet.getBalance(toCode).orElseGet(() -> {
            CurrencyBalance nb = CurrencyBalance.builder()
                    .currencyCode(toCode)
                    .currencySymbol(toSymbol)
                    .balance(BigDecimal.ZERO)
                    .isDefault(false)
                    .build();
            wallet.addCurrency(nb);
            return nb;
        });

        BigDecimal prevToBalance = toBalance.getBalance();
        BigDecimal newToBalance  = prevToBalance.add(convertedAmount);
        toBalance.setBalance(newToBalance);

        walletRepository.save(wallet);

        String txnId = newTxnId();
        walletCacheService.updateBalance(userId, fromCode, newFromBalance, newFromBalance, txnId);
        walletCacheService.updateBalance(userId, toCode,   newToBalance,  newToBalance,  txnId);

        // Track both currencies as active this month (fire-and-forget)
        CompletableFuture.runAsync(() -> {
            try {
                maintenanceUsagePort.recordActivity(userId, fromCode, request.getAmount(), "SWAP");
                maintenanceUsagePort.recordActivity(userId, toCode,   convertedAmount,     "SWAP");
            } catch (Exception ex) {
                log.warn("[MaintenanceUsage] Swap tracking failed: {}", ex.getMessage());
            }
        });

        final String fullName  = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        final String email     = userLookupPort.findEmailByUserId(userId).orElse(null);
        final String rateLabel = String.format("1 %s = %.6f %s (incl. %.0f%% spread)",
                fromCode, exchangeRate.doubleValue(), toCode,
                feePercentage.multiply(new BigDecimal("100")).doubleValue());

        final BigDecimal finalConverted      = convertedAmount;
        final BigDecimal finalNewToBalance   = newToBalance;
        final BigDecimal finalPrevToBalance  = prevToBalance;
        final BigDecimal finalSourceFee      = sourceFee;

        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        userId, wallet.getId(),
                        txnId, request.getIdempotencyKey(),
                        "SWAP", "DEBIT", "INTERNAL", "DELIVERED",  
                        request.getAmount(), finalSourceFee, request.getAmount(),
                        prevFromBalance, newFromBalance, fromCode, fromSymbol,
                        fullName,
                        "SWAP " + fromCode + " → " + toCode + " @ " + rateLabel,
                        null, null, null, null, null, null, null,
                        java.time.LocalDateTime.now());

                historyPort.record(
                        userId, wallet.getId(),
                        txnId + "_CR", request.getIdempotencyKey() + "_CR",
                        "SWAP", "CREDIT", "INTERNAL", "DELIVERED",
                        finalConverted, BigDecimal.ZERO, finalConverted,
                        finalPrevToBalance, finalNewToBalance, toCode, toSymbol,
                        fullName,
                        "SWAP CREDIT " + fromCode + " → " + toCode + " @ " + rateLabel,
                        null, null, null, null, null, null, null,
                        java.time.LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[Swap] History failed txn={}: {}", txnId, ex.getMessage());
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishSwapNotification(
                        email, finalConverted, fullName,
                        finalNewToBalance, finalPrevToBalance,
                        toSymbol, rateLabel);
            } catch (Exception ex) {
                log.warn("[Swap] Notification failed txn={}: {}", txnId, ex.getMessage());
            }
        });

        log.info("[Swap] userId={} {} {} → {} {} rate={} fee={} txn={}",
                userId, request.getAmount(), fromCode, convertedAmount, toCode, exchangeRate, sourceFee, txnId);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("success",             true);
        response.put("transactionId",       txnId);
        response.put("fromCurrency",        fromCode);
        response.put("toCurrency",          toCode);
        response.put("grossAmount",         request.getAmount());
        response.put("fee",                 sourceFee);
        response.put("feePercentage",       feePercentage.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%");
        response.put("convertedAmount",     convertedAmount);
        response.put("exchangeRate",        rateLabel);
        response.put("previousFromBalance", prevFromBalance);
        response.put("newFromBalance",      newFromBalance);
        response.put("previousToBalance",   prevToBalance);
        response.put("newToBalance",        newToBalance);
        response.put("timestamp",           java.time.Instant.now().toString());
        return ResponseEntity.status(201).body(response);
    }

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        SupportedCurrency from = supportedCurrencyRepository
                .findByCodeIgnoreCase(fromCurrency)
                .filter(SupportedCurrency::isActive)
                .orElseThrow(() -> new RuntimeException(
                        "Currency not supported: " + fromCurrency));

        SupportedCurrency to = supportedCurrencyRepository
                .findByCodeIgnoreCase(toCurrency)
                .filter(SupportedCurrency::isActive)
                .orElseThrow(() -> new RuntimeException(
                        "Currency not supported: " + toCurrency));

        BigDecimal fromRate = from.getExchangeRate();
        BigDecimal toRate   = to.getExchangeRate();

        if (fromRate == null || fromRate.compareTo(BigDecimal.ZERO) == 0)
            throw new RuntimeException(
                    "Exchange rate not configured for " + fromCurrency + ". Contact support.");
        if (toRate == null || toRate.compareTo(BigDecimal.ZERO) == 0)
            throw new RuntimeException(
                    "Exchange rate not configured for " + toCurrency + ". Contact support.");

        BigDecimal crossRate = toRate.divide(fromRate, 10, java.math.RoundingMode.HALF_UP);

        BigDecimal margin = new BigDecimal("0.005");
        return crossRate.multiply(BigDecimal.ONE.subtract(margin))
                        .setScale(8, java.math.RoundingMode.HALF_UP);
    }

    private java.util.Map<String, Object> calculateSwapAmounts(BigDecimal amount,
                                                                BigDecimal exchangeRate) {
        BigDecimal baseConvertedAmount = amount.multiply(exchangeRate);
        BigDecimal feePercentage       = calculateSwapFee(amount);
        BigDecimal feeAmount           = baseConvertedAmount.multiply(feePercentage);
        BigDecimal finalAmount         = baseConvertedAmount.subtract(feeAmount);

        return java.util.Map.of(
                "baseConvertedAmount", baseConvertedAmount,
                "feePercentage",       feePercentage,
                "feeAmount",           feeAmount,
                "finalAmount",         finalAmount,
                "exchangeRate",        exchangeRate);
    }

    private BigDecimal calculateSwapFee(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("1000")) > 0) return new BigDecimal("0.005");
        if (amount.compareTo(new BigDecimal("100"))  > 0) return new BigDecimal("0.01");
        return new BigDecimal("0.015");
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0)
            throw new BadRequestException("Valid user ID is required", ErrorCode.INVALID_INPUT);
    }

    private void requireActiveUser(Long userId) {
        if (!userLookupPort.existsActiveUser(userId))
            throw new ResourceNotFoundException("User not found or account is inactive");
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null)
            throw new BadRequestException("Amount is required", ErrorCode.INVALID_AMOUNT);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Amount must be greater than zero", ErrorCode.INVALID_AMOUNT);
    }

    private SupportedCurrency requireActiveCurrency(String code) {
        SupportedCurrency c = supportedCurrencyRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BadRequestException(
                        "Unsupported currency: " + code, ErrorCode.CURRENCY_NOT_SUPPORTED));
        if (!c.isActive())
            throw new WalletException("Currency " + code + " is currently disabled",
                    ErrorCode.CURRENCY_NOT_SUPPORTED);
        return c;
    }

    private void validateTxRequest(Long userId, Long walletId, String currency,
                                    BigDecimal amount, String referenceNo) {
        validateUserId(userId);
        if (walletId == null || walletId <= 0)
            throw new BadRequestException("Valid wallet ID is required", ErrorCode.INVALID_INPUT);
        if (currency == null || currency.isBlank())
            throw new BadRequestException("Currency type is required", ErrorCode.INVALID_CURRENCY);
        validateAmount(amount);
        if (referenceNo == null || referenceNo.isBlank())
            throw new BadRequestException("Reference number is required", ErrorCode.INVALID_INPUT);
        requireActiveCurrency(currency.trim().toUpperCase());
    }

    private Wallet resolveWallet(Long userId, Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        if (!wallet.getUserId().equals(userId))
            throw new WalletException("Wallet does not belong to this user", ErrorCode.WALLET_NOT_FOUND);
        if (!wallet.isActive())
            throw new WalletException("Wallet is locked", ErrorCode.WALLET_LOCKED);
        return wallet;
    }

    private String newTxnId() {
        return UUID.randomUUID().toString();
    }

    private WalletBalanceDTO toBalanceDTO(CurrencyBalance b) {
        WalletBalanceDTO dto = new WalletBalanceDTO();
        dto.setCurrency_code(b.getCurrencyCode());
        dto.setSymbol(b.getCurrencySymbol());
        dto.setBalance(b.getBalance().toPlainString());
        return dto;
    }
}
