package com.pesco.wallet_service.controller;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.models.WalletSettings;
import com.pesco.wallet_service.payloads.CreateTransferPinRequest;
import com.pesco.wallet_service.payloads.MaintenanceDebitRequest;
import com.pesco.wallet_service.payloads.WalletRefundRequest;
import com.pesco.wallet_service.repository.WalletSettingsRepository;
import com.pesco.wallet_service.services.WalletCacheService;
import com.pesco.wallet_service.services.WalletService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import pesco.wallet_service.grpc.WalletServiceGrpc;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletSettingsRepository walletSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final WalletCacheService walletCacheService;

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletGrpcClient;

    public WalletController(WalletSettingsRepository walletSettingsRepository, PasswordEncoder passwordEncoder, WalletService walletService, WalletCacheService walletCacheService) {
        this.walletSettingsRepository = walletSettingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.walletCacheService = walletCacheService;
    }
   

    @GetMapping("/{walletId}")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<?> getWalletById(@PathVariable Long walletId) {
        try {
            // Call the gRPC service
            Optional<WalletSettings> walletSettings = walletSettingsRepository.findByWalletId(walletId);
        
            if (walletSettings.isPresent()) {
                return ResponseEntity.ok(walletSettings);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wallet setting not updated yet");

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create wallet: " + ex.getMessage());
        }
    }

    @GetMapping("/userId/{userId}")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<?> getWalletByUserId(@PathVariable Long userId) {
            // Validate userId
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body("Invalid user ID provided. Please provide a valid user ID.");
            }
            return walletService.getWalletByUserId(userId);
       
    }

    @GetMapping("/{userId}/{currency}")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<?> getByCurrency(@PathVariable Long userId,@PathVariable String currency) {
        return walletService.getWalletByUserIdAndCurrencyType(userId, currency);
    }

    @PostMapping("/create/pin")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<?> userSetTransferPin(@RequestBody CreateTransferPinRequest request,
            Authentication authentication) {
        String providedPin = request.getTransferPin();
        String requestUsername = authentication.getName();

         Map<String, Object> error = new HashMap<>();

        if (providedPin == null || providedPin.isEmpty()) {
            error.put("status", "error");
            error.put("message", "Your withdrawal/transfer pin is required");
            error.put("details", "Please provide your withdrawal/transfer pin and please don't share it with anyone for security reasons.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!request.getUsername().equals(requestUsername)) {
            error.put("status", "error");
            error.put("details", "One more attempt from you again, you will be reported to the Economic and Financial Crimes Commission (EFCC).");
            error.put("message", "Fraudulent action is taken here, You are not the authorized user to operate this wallet.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        }

        if (providedPin.length() != 4 || !providedPin.matches("\\d{4}")) {
            error.put("status", "error");
            error.put("message", "Invalid input. Please provide exactly 4 digits.");
            error.put("details", "Your pin must be exactly 4 numeric digits.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
         }

        Optional<WalletSettings> wallet = walletSettingsRepository.findByWalletId(request.getWalletId());
        if (wallet.isPresent()) {
            WalletSettings update  = wallet.get();
         
            wallet.get().setPassword(passwordEncoder.encode(providedPin));
            wallet.get().setIsSecure(true);
            walletSettingsRepository.save(update);

            error.put("status", "success");
            error.put("message", "Withdrawal password successfully set.");
            error.put("details", "Withdrawal/transfer password successfully set, you can now make withdrawals or transfers");
            return ResponseEntity.status(HttpStatus.CREATED).body(error);
        }
        return ResponseEntity.badRequest().body("Sorry, system can not update your wallet right now.");
    }

    @PostMapping("/verify/pin")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
    public ResponseEntity<?> verifyPin(@RequestBody CreateTransferPinRequest request,
            Authentication authentication) {
        String providedPin = request.getTransferPin();
        String requestUsername = authentication.getName();

        Map<String, Object> response = new HashMap<>();

        if (providedPin == null || providedPin.isEmpty()) {
            response.put("status", "error");
            response.put("message", "PIN is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!request.getUsername().equals(requestUsername)) {
            response.put("status", "error");
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (providedPin.length() != 4 || !providedPin.matches("\\d{4}")) {
            response.put("status", "error");
            response.put("message", "PIN must be exactly 4 digits");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<WalletSettings> wallet = walletSettingsRepository.findByWalletId(request.getWalletId());
        if (wallet.isPresent()) {
            WalletSettings walletSettings = wallet.get();
            
            // Verify the provided PIN against the stored encoded PIN
            if (passwordEncoder.matches(providedPin, walletSettings.getPassword())) {
                response.put("status", "success");
                response.put("message", "PIN verified successfully");
                response.put("verified", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Pin does not match active pin.");
                response.put("verified", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        }
        
        response.put("status", "error");
        response.put("message", "Wallet not found");
        response.put("verified", false);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping("/cache/{userId}")
    @PreAuthorize("hasRole('USER', 'ADMIN')")
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
            // Validate request
            if (request.getUserId() == null || request.getWalletId() == null || 
                request.getCurrencyType() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            // Process maintenance fee deduction
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
                    .body("Failed to process maintenance fee: " + ex.getMessage());
        }
    }
}

