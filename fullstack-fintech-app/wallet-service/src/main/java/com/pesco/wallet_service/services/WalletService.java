package com.pesco.wallet_service.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pesco.wallet_service.client.UserServiceClient;
import com.pesco.wallet_service.dtos.WalletBalanceDTO;
import com.pesco.wallet_service.dtos.WalletSection;
import com.pesco.wallet_service.enums.Currency;
import com.pesco.wallet_service.models.CurrencyBalanceMapStruct;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.models.WalletSettings;
import com.pesco.wallet_service.payloads.MaintenanceDebitRequest;
import com.pesco.wallet_service.payloads.WalletBalanceResponseDTO;
import com.pesco.wallet_service.payloads.WalletRefundRequest;
import com.pesco.wallet_service.repository.WalletRepository;
import com.pesco.wallet_service.repository.WalletSettingsRepository;
import com.pesco.wallet_service.util.AccountWrapper;
import com.pesco.wallet_service.util.HazelcastWallet;
import com.pesco.wallet_service.util.JwtTokenProvider;

import jakarta.transaction.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;
    private final WalletSettingsRepository walletSettingsRepository;
    private final HazelcastWallet redisWallet;
    private final JwtTokenProvider jwtprovider;
    
    public WalletService(WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            UserServiceClient userServiceClient,
            AccountWrapper accountWrapper,
            WalletSettingsRepository walletSettingsRepository, 
            HazelcastWallet redisWallet, JwtTokenProvider jwtprovider) {
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.userServiceClient = userServiceClient;
        this.walletSettingsRepository = walletSettingsRepository;
        this.redisWallet = redisWallet;
        this.jwtprovider = jwtprovider;
    }


    public ResponseEntity<?> getWalletByUserId(Long userId) {
        Map<String, Object> response = new LinkedHashMap<>();

        Optional<Wallet> walletOptional = walletRepository.findByUserId(userId);

        if (walletOptional.isEmpty()) {
            response.put("status", "error");
            response.put("type", "message");
            response.put("message", "User wallet not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Wallet wallet = walletOptional.get();

        // Convert balances to WalletBalanceDTO using setters
        List<WalletBalanceDTO> walletBalanceDTOs = wallet.getBalances().stream().map(balance -> {
            WalletBalanceDTO dto = new WalletBalanceDTO();
            dto.setCurrency_code(balance.getCurrencyCode());
            dto.setSymbol(balance.getCurrencySymbol());
            dto.setBalance(balance.getBalance().toString());
            return dto;
        }).collect(Collectors.toList());

        Optional<WalletSettings> walletSettingsOptional = walletSettingsRepository.findByWalletId(wallet.getId());
        boolean hasTransferPin = walletSettingsOptional
                .map(WalletSettings::isIsSecure)
                .orElse(false);

        // Create response DTO
        WalletSection walletSection = new WalletSection();
        walletSection.setWalletId(wallet.getId());
        walletSection.setUserId(wallet.getUserId());
        walletSection.setHasTransferPin(hasTransferPin);
        walletSection.setWallet_balances(walletBalanceDTOs);

        return ResponseEntity.ok(walletSection);
    }

    public ResponseEntity<?> getWalletByUserIdAndCurrencyType(Long userId, String type) {
        Map<String, Object> response = new LinkedHashMap<>();
        String currency = type.toUpperCase();

        if (!Arrays.stream(Currency.values())
                .anyMatch(ct -> ct.name().equalsIgnoreCase(currency))) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Invalid Currency provided.*");
            error.put("details",
                    "Please provide Currency type. Any of this list (USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY, INR)");
            return ResponseEntity.badRequest().body(error);
        }

        Optional<Wallet> walletOptional = walletRepository.findWalletByUserIdAndCurrencyCode(userId, currency);

        if (walletOptional.isEmpty()) {
            response.put("status", "error");
            response.put("type", "message");
            response.put("message", "Wallet or currency not found for user ID " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Wallet wallet = walletOptional.get();

        Optional<CurrencyBalanceMapStruct> currencyBalanceOpt = wallet.getBalances().stream()
                .filter(balance -> currency.equalsIgnoreCase(balance.getCurrencyCode()))
                .findFirst();

        if (currencyBalanceOpt.isEmpty()) {
            response.put("status", "error");
            response.put("type", "message");
            response.put("message", "Currency " + currency + " not found in wallet");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        CurrencyBalanceMapStruct balance = currencyBalanceOpt.get();

        WalletBalanceResponseDTO dto = new WalletBalanceResponseDTO();
        dto.setWalletId(wallet.getId());
        dto.setCurrencyCode(balance.getCurrencyCode());
        dto.setSymbol(balance.getCurrencySymbol());
        dto.setBalance(balance.getBalance().toPlainString());

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @Transactional
    public ResponseEntity<?> updateBalance(String currency, BigDecimal amount, Long userId, Long walletId) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            String currencyCode = currency.toUpperCase();

            Optional<Wallet> walletOptional = walletRepository.findById(walletId);

            if (walletOptional.isEmpty()) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Wallet not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Wallet wallet = walletOptional.get();

            if (!wallet.getUserId().equals(userId)) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Wallet does not belong to this user.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<CurrencyBalanceMapStruct> existing = wallet.getBalances().stream()
                    .filter(cb -> cb.getCurrencyCode().equalsIgnoreCase(currencyCode))
                    .findFirst();

            if (existing.isPresent()) {
                CurrencyBalanceMapStruct cb = existing.get();
                cb.setBalance(cb.getBalance().add(amount));
            } else {
                wallet.addBalance(new CurrencyBalanceMapStruct(
                    currencyCode,
                    getCurrencySymbol(currencyCode),
                    amount
                ));
            }

            walletRepository.save(wallet);

            CompletableFuture.runAsync(() ->
                redisWallet.updateHazelcastWalletBalance(
                    userId,
                    Currency.valueOf(currencyCode),
                    amount
                )
            ).exceptionally(ex -> null);

            response.put("status", "success");
            response.put("type", "wallet_update");
            response.put("message", "Wallet balance updated successfully.");
            response.put("userId", userId);
            response.put("walletId", walletId);
            response.put("currency", currencyCode);
            response.put("amount_added", amount);
            response.put("updated_balances", wallet.getBalances());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("type", "message");
            response.put("message", "Error updating wallet balance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String getCurrencySymbol(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "NGN" -> "₦";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "AUD" -> "A$";
            case "CAD" -> "C$";
            case "CHF" -> "CHF";
            case "CNY" -> "¥";
            case "INR" -> "₹";
            default -> "?";
        };
    }


    @Transactional
    public ResponseEntity<?> processMaintenanceFee(MaintenanceDebitRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Optional<Wallet> walletOptional = walletRepository.findById(request.getWalletId());

            if (walletOptional.isEmpty()) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Wallet not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Wallet wallet = walletOptional.get();

            if (!wallet.getUserId().equals(request.getUserId())) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Wallet does not belong to this user.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currencyCode = request.getCurrencyType().toUpperCase();

            CurrencyBalanceMapStruct cb = wallet.getBalances().stream()
                    .filter(b -> b.getCurrencyCode().equalsIgnoreCase(currencyCode))
                    .findFirst()
                    .orElse(null);

            if (cb == null) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Currency balance not found for: " + currencyCode);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            BigDecimal newBalance = cb.getBalance().subtract(request.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Insufficient balance for maintenance fee deduction.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            cb.setBalance(newBalance);
            walletRepository.save(wallet);

            CompletableFuture.runAsync(() ->
                redisWallet.updateHazelcastWalletBalance(
                    wallet.getUserId(),
                    Currency.valueOf(currencyCode),
                    request.getAmount().negate()
                )
            ).exceptionally(ex -> null);

            response.put("status", "success");
            response.put("type", "wallet_update");
            response.put("message", "Maintenance fee has been successfully deducted.");
            response.put("userId", request.getUserId());
            response.put("walletId", request.getWalletId());
            response.put("currency", currencyCode);
            response.put("amount_deducted", request.getAmount());
            response.put("new_balance", newBalance);
            response.put("updated_balances", wallet.getBalances());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("type", "message");
            response.put("message", "Error processing maintenance fee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Transactional
    public ResponseEntity<?> refundWallet(WalletRefundRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Wallet wallet = walletRepository.findByUserId(request.getSenderId())
                    .orElse(null);

            if (wallet == null) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Wallet not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String currencyCode = request.getCurrencyCode().toUpperCase();
            Optional<CurrencyBalanceMapStruct> currencyBalance = wallet.getBalances().stream()
                    .filter(cb -> cb.getCurrencyCode().equalsIgnoreCase(currencyCode))
                    .findFirst();

            if (currencyBalance.isEmpty()) {
                response.put("status", "error");
                response.put("type", "message");
                response.put("message", "Currency balance not found for: " + currencyCode);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            CurrencyBalanceMapStruct cb = currencyBalance.get();
            BigDecimal newBalance = cb.getBalance().add(request.getAmount());
            cb.setBalance(newBalance);

            walletRepository.save(wallet);
            
            CompletableFuture.runAsync(() ->
                redisWallet.updateHazelcastWalletBalance(
                    wallet.getUserId(),
                    Currency.valueOf(currencyCode),
                    request.getAmount()
                )
            ).exceptionally(ex -> null);

            response.put("status", "success");
            response.put("type", "wallet_update");
            response.put("message", "Refund has been successfully processed.");
            response.put("userId", request.getSenderId());
            response.put("currency", currencyCode);
            response.put("amount_refunded", request.getAmount());
            response.put("new_balance", newBalance);
            response.put("updated_balances", wallet.getBalances());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("type", "message");
            response.put("message", "Error processing refund: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
