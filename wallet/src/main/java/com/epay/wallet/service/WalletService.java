package com.epay.wallet.service;

import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ConflictException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.exception.WalletException;
import com.epay.domain.wallet.dto.WalletBalanceDTO;
import com.epay.domain.wallet.dto.WalletSection;
import com.epay.domain.wallet.input.AddCurrencyRequest;
import com.epay.domain.wallet.input.ChangePinRequest;
import com.epay.domain.wallet.input.CreateWalletRequest;
import com.epay.domain.wallet.input.InvestmentCreditRequest;
import com.epay.domain.wallet.input.InvestmentDebitRequest;
import com.epay.domain.wallet.input.MaintenanceDebitRequest;
import com.epay.domain.wallet.input.SavingsCreditRequest;
import com.epay.domain.wallet.input.SavingsDebitRequest;
import com.epay.domain.wallet.input.SetPinRequest;
import com.epay.domain.wallet.input.TransferRequest;
import com.epay.domain.wallet.input.WalletRefundRequest;
import com.epay.wallet.cache.WalletCacheService;
import com.epay.wallet.domain.entity.CurrencyBalance;
import com.epay.wallet.domain.entity.SupportedCurrency;
import com.epay.wallet.domain.entity.Wallet;
import com.epay.wallet.domain.entity.WalletSettings;
import com.epay.wallet.interfaces.IWalletService;
import com.epay.wallet.port.UserLookupPort;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import com.epay.wallet.repository.WalletRepository;
import com.epay.wallet.repository.WalletSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService implements IWalletService {

    private final WalletRepository            walletRepository;
    private final WalletSettingsRepository    walletSettingsRepository;
    private final SupportedCurrencyRepository supportedCurrencyRepository;
    private final WalletCacheService          walletCacheService;
    private final UserLookupPort              userLookupPort;
    private final PasswordEncoder             passwordEncoder;

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
        requireActiveUser(request.getUserId());

        if (walletRepository.existsByUserId(request.getUserId()))
            throw new ConflictException("Wallet already exists for this user",
                    ErrorCode.WALLET_ALREADY_EXISTS);

        String code = request.getDefaultCurrency().trim().toUpperCase();
        SupportedCurrency currency = requireActiveCurrency(code);

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId()).active(true).pinSet(false).build();
        wallet.addCurrency(CurrencyBalance.builder()
                .currencyCode(code).currencySymbol(currency.getSymbol())
                .balance(BigDecimal.ZERO).isDefault(true).build());
        walletRepository.save(wallet);

        walletCacheService.warmFromWallet(request.getUserId(), wallet, null, code, List.of(code));
        log.info("Wallet created: userId={} currency={}", request.getUserId(), code);
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

        wallet.addCurrency(CurrencyBalance.builder()
                .currencyCode(code).currencySymbol(currency.getSymbol())
                .balance(BigDecimal.ZERO).isDefault(false).build());
        walletRepository.save(wallet);

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

        wallet.getBalances().forEach(b -> b.setDefault(false));
        wallet.getBalance(code).ifPresent(b -> b.setDefault(true));
        walletRepository.save(wallet);

        walletCacheService.evict(userId);
        log.info("Default currency set: userId={} currency={}", userId, code);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> setPin(Long userId, SetPinRequest request) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (!request.getPin().equals(request.getConfirmPin()))
            throw new BadRequestException("PINs do not match", ErrorCode.INVALID_INPUT);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.isPinSet())
            throw new BadRequestException("PIN already set — use change PIN",
                    ErrorCode.OPERATION_NOT_ALLOWED);

        wallet.setTransactionPin(passwordEncoder.encode(request.getPin()));
        wallet.setPinSet(true);
        walletRepository.save(wallet);

        WalletSettings settings = walletSettingsRepository.findByWalletId(wallet.getId())
                .orElse(new WalletSettings());
        settings.setWallet(wallet);
        settings.setIsSecure(true);
        walletSettingsRepository.save(settings);

        walletCacheService.evict(userId);
        log.info("PIN set: userId={}", userId);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> changePin(Long userId, ChangePinRequest request) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (!request.getNewPin().equals(request.getConfirmNewPin()))
            throw new BadRequestException("New PINs do not match", ErrorCode.INVALID_INPUT);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.isPinSet())
            throw new BadRequestException("No PIN set — use set PIN first",
                    ErrorCode.OPERATION_NOT_ALLOWED);

        if (!passwordEncoder.matches(request.getCurrentPin(), wallet.getTransactionPin()))
            throw new WalletException("Current PIN is incorrect", ErrorCode.INVALID_PIN);

        wallet.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        walletRepository.save(wallet);
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
        if (!senderWallet.isPinSet())
            throw new WalletException("Transaction PIN not set", ErrorCode.INVALID_PIN);
        if (!passwordEncoder.matches(request.getTransactionPin(), senderWallet.getTransactionPin()))
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
        CurrencyBalance recipientBalance = recipientWallet.getBalance(code)
                .orElseGet(() -> {
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

        log.info("Transfer: sender={} recipient={} currency={} amount={} ref={}",
                senderUserId, recipientUserId, code, request.getAmount(), request.getIdempotencyKey());
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateBalance(String currency, BigDecimal amount, Long userId, Long walletId) {
        validateUserId(userId);
        requireActiveUser(userId);
        if (walletId == null || walletId <= 0)
            throw new BadRequestException("Valid wallet ID is required", ErrorCode.INVALID_INPUT);
        if (currency == null || currency.isBlank())
            throw new BadRequestException("Currency is required", ErrorCode.INVALID_CURRENCY);
        validateAmount(amount);

        String code = currency.trim().toUpperCase();
        SupportedCurrency supported = requireActiveCurrency(code);

        Wallet wallet = resolveWallet(userId, walletId);
        CurrencyBalance balance = wallet.getBalance(code).orElseGet(() -> {
            CurrencyBalance nb = CurrencyBalance.builder()
                    .currencyCode(code).currencySymbol(supported.getSymbol())
                    .balance(BigDecimal.ZERO).isDefault(false).build();
            wallet.addCurrency(nb);
            return nb;
        });

        BigDecimal newBalance = balance.getBalance().add(amount);
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

        walletCacheService.updateBalance(userId, code, newBalance, newBalance, newTxnId());
        log.info("Balance updated: userId={} walletId={} currency={} delta={} new={}",
                userId, walletId, code, amount, newBalance);
        return ResponseEntity.ok().build();
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

        CurrencyBalance balance = wallet.getBalance(code)
                .orElseThrow(() -> new ResourceNotFoundException(code + " not found in wallet"));
        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

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

        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, newTxnId());
        log.info("Maintenance fee: walletId={} currency={} amount={}", request.getWalletId(), code, request.getAmount());
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

        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, newTxnId());
        log.info("Investment debit: walletId={} currency={} amount={}", request.getWalletId(), code, request.getAmount());
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

        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, newTxnId());
        log.info("Investment credit: walletId={} currency={} amount={}", request.getWalletId(), code, request.getAmount());
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

        BigDecimal newBalance = balance.getBalance().subtract(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, newTxnId());
        log.info("Savings debit: walletId={} currency={} amount={}", request.getWalletId(), code, request.getAmount());
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

        BigDecimal newBalance = balance.getBalance().add(request.getAmount());
        balance.setBalance(newBalance);
        walletRepository.save(wallet);

        walletCacheService.updateBalance(request.getUserId(), code, newBalance, newBalance, newTxnId());
        log.info("Savings credit: walletId={} currency={} amount={}", request.getWalletId(), code, request.getAmount());
        return ResponseEntity.ok().build();
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
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private WalletBalanceDTO toBalanceDTO(CurrencyBalance b) {
        WalletBalanceDTO dto = new WalletBalanceDTO();
        dto.setCurrency_code(b.getCurrencyCode());
        dto.setSymbol(b.getCurrencySymbol());
        dto.setBalance(b.getBalance().toPlainString());
        return dto;
    }
}
