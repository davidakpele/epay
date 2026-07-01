package com.pesco.wallet_service.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pesco.wallet_service.security.WalletRateLimited;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pesco.wallet_service.client.NotificationServiceClient;
import com.pesco.wallet_service.client.UserServiceClient;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.models.WalletSettings;
import com.pesco.wallet_service.payloads.CreateTransferPinRequest;
import com.pesco.wallet_service.payloads.MaintenanceDebitRequest;
import com.pesco.wallet_service.payloads.WalletRefundRequest;
import com.pesco.wallet_service.payloads.InvestmentDebitRequest;
import com.pesco.wallet_service.payloads.InvestmentCreditRequest;
import com.pesco.wallet_service.payloads.SavingsDebitRequest;
import com.pesco.wallet_service.payloads.SavingsCreditRequest;
import com.pesco.wallet_service.repository.WalletRepository;
import com.pesco.wallet_service.repository.WalletSettingsRepository;
import com.pesco.wallet_service.services.WalletCacheService;
import com.pesco.wallet_service.services.WalletService;
import com.pesco.wallet_service.handler.WalletHandler;
import com.pesco.wallet_service.util.NotificationProperties;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletSettingsRepository walletSettingsRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final WalletCacheService walletCacheService;
    private final WalletHandler walletHandler;
    private final NotificationServiceClient notificationServiceClient;
    private final NotificationProperties notificationProperties;
    private final UserServiceClient userServiceClient;

    private static final DateTimeFormatter EVT_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");

    private String formatNow() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(EVT_FMT);
    }

    public WalletController(WalletSettingsRepository walletSettingsRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            WalletService walletService,
            WalletCacheService walletCacheService,
            WalletHandler walletHandler,
            NotificationServiceClient notificationServiceClient,
            NotificationProperties notificationProperties,
            UserServiceClient userServiceClient) {
        this.walletSettingsRepository = walletSettingsRepository;
        this.walletRepository         = walletRepository;
        this.passwordEncoder          = passwordEncoder;
        this.walletService            = walletService;
        this.walletCacheService       = walletCacheService;
        this.walletHandler            = walletHandler;
        this.notificationServiceClient = notificationServiceClient;
        this.notificationProperties   = notificationProperties;
        this.userServiceClient        = userServiceClient;
    }

    @GetMapping("/{walletId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getWalletById(@PathVariable Long walletId) {
        try {
            Optional<WalletSettings> walletSettings = walletSettingsRepository.findByWalletId(walletId);
            if (walletSettings.isPresent()) {
                return ResponseEntity.ok(walletSettings);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wallet setting not updated yet");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch wallet: " + ex.getMessage());
        }
    }

    @GetMapping("/userId/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
    public ResponseEntity<?> getWalletByUserId(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body("Invalid user ID provided. Please provide a valid user ID.");
        }
        return walletService.getWalletByUserId(userId);
    }

    @GetMapping("/{userId}/{currency}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getByCurrency(@PathVariable Long userId,@PathVariable String currency) {
        return walletService.getWalletByUserIdAndCurrencyType(userId, currency);
    }

    @PostMapping("/create/pin")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @WalletRateLimited(
        keyPrefix      = "pin_setup",
        capacity       = 5,
        duration       = 10,
        timeUnit       = java.util.concurrent.TimeUnit.MINUTES,
        userIdentifier = "#authentication.name",
        coolDownSeconds = 30
    )
    public ResponseEntity<?> userSetTransferPin(@RequestBody CreateTransferPinRequest request,
                                                Authentication authentication) {
        String providedPin = request.getTransferPin();

        Map<String, Object> response = new HashMap<>();

        if (providedPin == null || providedPin.isEmpty()) {
            response.put("status",  "error");
            response.put("message", "Your withdrawal/transfer pin is required");
            response.put("details", "Please provide your pin and do not share it with anyone.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (providedPin.length() != 4 || !providedPin.matches("\\d{4}")) {
            response.put("status",  "error");
            response.put("message", "Invalid input. Please provide exactly 4 digits.");
            response.put("details", "Your pin must be exactly 4 numeric digits.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<WalletSettings> settingsOpt = walletSettingsRepository.findByWalletId(request.getWalletId());
        final boolean isUpdate = settingsOpt.isPresent() && settingsOpt.get().isIsSecure();

        WalletSettings settings;
        if (settingsOpt.isPresent()) {
            settings = settingsOpt.get();
        } else {
            Optional<Wallet> walletOpt = walletRepository.findById(request.getWalletId());
            if (walletOpt.isEmpty()) {
                response.put("status",  "error");
                response.put("message", "Wallet not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            settings = new WalletSettings();
            settings.setWallet(walletOpt.get());
        }

        settings.setPassword(passwordEncoder.encode(providedPin));
        settings.setIsSecure(true);
        walletSettingsRepository.save(settings);

        // ── Wallet PIN alert notification ─────────────────────────────────────
        final String actionTime = formatNow();
        final String action     = isUpdate ? "UPDATED" : "CREATED";
        final String username   = authentication != null ? authentication.getName()
                                                         : (request.getUsername() != null ? request.getUsername() : "");
        CompletableFuture.runAsync(() -> {
            try {
                com.pesco.wallet_service.dtos.UserDTO userDto =
                        userServiceClient.findByUsername(username, "");
                if (userDto != null) {
                    String email    = userDto.getEmail();
                    String fullName = username;
                    if (userDto.getRecords() != null && !userDto.getRecords().isEmpty()) {
                        var rec = userDto.getRecords().get(0);
                        fullName = rec.getFirstName() + " " + rec.getLastName();
                    }
                    notificationServiceClient.sendWalletPinAlert(
                        email, fullName, username,
                        action, actionTime,
                        "", "",
                        notificationProperties.getPhone(),
                        notificationProperties.getEmail()
                    );
                }
            } catch (Exception ex) {
                System.err.println("[WalletPinAlert] Failed to send: " + ex.getMessage());
            }
        });
        // ─────────────────────────────────────────────────────────────────────

        response.put("status",  "success");
        response.put("message", "Withdrawal/transfer pin successfully set.");
        response.put("details", "You can now make withdrawals or transfers.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify/pin")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @WalletRateLimited(
        keyPrefix      = "pin_verify",
        capacity       = 10,
        duration       = 5,
        timeUnit       = java.util.concurrent.TimeUnit.MINUTES,
        userIdentifier = "#authentication.name"
    )
    public ResponseEntity<?> verifyPin(@RequestBody CreateTransferPinRequest request,
                                       Authentication authentication) {
        String providedPin     = request.getTransferPin();
        String requestUsername = authentication.getName();
        Map<String, Object> response = new HashMap<>();

        if (providedPin == null || providedPin.isEmpty()) {
            response.put("status",  "error");
            response.put("message", "PIN is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!request.getUsername().equals(requestUsername)) {
            response.put("status",  "error");
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (providedPin.length() != 4 || !providedPin.matches("\\d{4}")) {
            response.put("status",  "error");
            response.put("message", "PIN must be exactly 4 digits");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<WalletSettings> walletOpt = walletSettingsRepository.findByWalletId(request.getWalletId());
        if (walletOpt.isPresent()) {
            WalletSettings walletSettings = walletOpt.get();
            if (passwordEncoder.matches(providedPin, walletSettings.getPassword())) {
                response.put("status",   "success");
                response.put("message",  "PIN verified successfully");
                response.put("verified", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("status",   "error");
                response.put("message",  "Pin does not match active pin.");
                response.put("verified", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        }

        response.put("status",   "error");
        response.put("message",  "Wallet not found");
        response.put("verified", false);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping("/cache/{userId}")
    public ResponseEntity<Map<String, Object>> getCacheWalletByUserId(@PathVariable Long userId) {
        Wallet wallet = walletCacheService.getCachedWalletByUserId(userId);
        if (wallet == null) {
            walletCacheService.cacheWalletById(userId);
            wallet = walletCacheService.getCachedWalletByUserId(userId);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("wallet", wallet);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/debit/maintenance")
    public ResponseEntity<?> debitMaintenanceFee(@RequestBody MaintenanceDebitRequest request) {
        try {
            if (request.getUserId() == null || request.getWalletId() == null ||
                request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            return walletService.processMaintenanceFee(request);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process maintenance fee: " + ex.getMessage());
        }
    }

    @PutMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody WalletRefundRequest request) {
        try {
            return walletService.refundWallet(request);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process refund: " + ex.getMessage());
        }
    }

    @PostMapping("/create-account/{userId}")
    public ResponseEntity<?> createWalletAccount(@PathVariable Long userId) {
        try {
            walletHandler.createAccount(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Wallet account created successfully");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create wallet account: " + ex.getMessage());
        }
    }

    // ── Investment debit: called by resiliences-service when user invests ─────

    @PostMapping("/internal/debit/investment")
    public ResponseEntity<?> debitInvestment(@RequestBody InvestmentDebitRequest request) {
        try {
            if (request.getUserId() == null || request.getWalletId() == null
                    || request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            return walletService.processInvestmentDebit(request);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process investment debit: " + ex.getMessage());
        }
    }

    // ── Investment credit: called by resiliences-service when investment matures

    @PostMapping("/internal/credit/investment")
    public ResponseEntity<?> creditInvestment(@RequestBody InvestmentCreditRequest request) {
        try {
            if (request.getUserId() == null || request.getWalletId() == null
                    || request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            return walletService.processInvestmentCredit(request);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process investment credit: " + ex.getMessage());
        }
    }

    // ── Savings debit: called by resiliences-service when user tops up a goal ─

    @PostMapping("/internal/debit/savings")
    public ResponseEntity<?> debitSavings(@RequestBody SavingsDebitRequest request) {
        try {
            if (request.getUserId() == null || request.getWalletId() == null
                    || request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            return walletService.processSavingsDebit(request);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process savings debit: " + ex.getMessage());
        }
    }

    // ── Savings credit: called by resiliences-service when user withdraws ─────

    @PostMapping("/internal/credit/savings")
    public ResponseEntity<?> creditSavings(@RequestBody SavingsCreditRequest request) {
        try {
            if (request.getUserId() == null || request.getWalletId() == null
                    || request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            return walletService.processSavingsCredit(request);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process savings credit: " + ex.getMessage());
        }
    }

    // ── Maintenance credit reversal ───────────────────────────────────────────

    @PostMapping("/internal/credit/maintenance")
    public ResponseEntity<?> creditMaintenanceReversal(@RequestBody MaintenanceDebitRequest request) {
        try {
            if (request.getUserId() == null || request.getWalletId() == null
                    || request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            // Reversal: credit back the amount (use updateBalance with positive amount)
            return walletService.updateBalance(
                request.getCurrencyType(), request.getAmount(),
                request.getUserId(), request.getWalletId());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process maintenance reversal: " + ex.getMessage());
        }
    }
}