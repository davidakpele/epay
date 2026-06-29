package pesco.notification_service.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import pesco.notification_service.messageProducer.AuthenticationMessageProducer;
import pesco.notification_service.messageProducer.WalletMessageProducer;
import pesco.notification_service.payloads.AccountSecurityNotification;
import pesco.notification_service.payloads.AccountVerificationRequest;
import pesco.notification_service.payloads.BlockUserWallet;
import pesco.notification_service.payloads.CreditWalletNotification;
import pesco.notification_service.payloads.DebitWalletNotification;
import pesco.notification_service.payloads.DepositWalletNotification;
import pesco.notification_service.payloads.LoginAlertNotification;
import pesco.notification_service.payloads.MaintenanceDeductionNotification;
import pesco.notification_service.payloads.PasswordResetRequest;
import pesco.notification_service.payloads.RegistrationOtpMessage;
import pesco.notification_service.payloads.SwapCurrencyPayload;
import pesco.notification_service.payloads.UserOTPMessage;
import pesco.notification_service.payloads.WalletPinNotification;
import pesco.notification_service.payloads.WelcomeMessagePayload;


@RestController
@Validated
public class MessageController {

    private final AuthenticationMessageProducer authenticationMessageProducer;
    private final WalletMessageProducer walletMessageProducer;

    public MessageController(AuthenticationMessageProducer authenticationMessageProducer, WalletMessageProducer walletMessageProducer) {
        this.authenticationMessageProducer = authenticationMessageProducer;
        this.walletMessageProducer = walletMessageProducer;
    }
  
    @PostMapping("/send/verification-message")
    public ResponseEntity<?> verificationAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody AccountVerificationRequest request) {
        try {
            authenticationMessageProducer.sendVerificationEmail(request.getEmail(), request.getmessage(), request.getLink(), request.getUsername());
            return ResponseEntity.ok().body("Verification email has been successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Verification email.");
        }
    }

    @PostMapping("/send/otp-message")
    public ResponseEntity<?> sendUserOtpAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody UserOTPMessage request) {
        try {
            authenticationMessageProducer.sendOptEmail(request.getEmail(), request.getOtp(), request.getRestPassword(), request.getConfigTwoFactorAuth(), request.getConfigTwoFactorAuthRecovery());
            return ResponseEntity.ok().body("OTP successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP.");
        }
    }

    @PostMapping("/send/password-reset-message")
    public ResponseEntity<?> sendUserResetPasswordAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            authenticationMessageProducer.sendPasswordResetEmail(request.getEmail(), request.getUsername(),
                    request.getmessage(), request.getUrl());
            return ResponseEntity.ok().body("Password reset link successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Password reset.");
        }
    }

    @PostMapping("/send/credit-wallet-message")
    public ResponseEntity<Map<String, Object>> sendCreditWalletAlert(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreditWalletNotification request) {
        try {
            walletMessageProducer.sendCreditWalletNotification(
                    request.getRecipientEmail(),
                    request.getTransferAmount(),
                    request.getSenderFullName(),
                    request.getReceiverFullName(),
                    request.getRecipientTotalBalance(),
                    request.getCurrency(),
                    request.getTransactionId(),
                    request.getPreviousBalance());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Credit message successfully sent!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send credit message.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/send/deposit-wallet-message")
    public ResponseEntity<Map<String, Object>> sendDepositWalletAlert(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DepositWalletNotification request) {
        try {
            walletMessageProducer.sendDepositWalletNotification(
                    request.getRecipientEmail(),
                    request.getRecipientName(),
                    request.getDepositAmount(),
                    request.getAmount(),
                    request.getAccountHolder(),
                    request.getAvailableBalance(),
                    request.getPreviousBalance(),
                    request.getTerminalNumber(),
                    request.getCurrencySymbol());

            Map<String, Object> response = new HashMap<>();
            response.put("status", request);
            response.put("message", "Deposit message successfully sent!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send deposit message.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/send/debit-wallet-message")
    public ResponseEntity<Map<String, Object>> sendDebitWalletAlert(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DebitWalletNotification request) {
        try {
            walletMessageProducer.sendDebitWalletNotification(
                    request.getSenderEmail(),
                    request.getFeeAmount(),
                    request.getTransferAmount(),
                    request.getSenderFullName(),
                    request.getReceiverFullName(),
                    request.getBalance(),
                    request.getCurrency(),
                    request.getTransactionId(),
                    request.getPreviousBalance());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Debit message successfully sent!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send debit message.");
            return ResponseEntity.internalServerError().body(error);
        }
    }


    @PostMapping(value = "/send/bank-statement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendStatementOfAccount(
            @RequestPart String email,
            @RequestPart String username,
            @RequestPart String subject,
            @RequestPart MultipartFile pdfFile,
            @RequestPart String filename,
            @RequestPart String period) {
        try {
            // Get bytes BEFORE transferring the file
            byte[] pdfBytes = pdfFile.getBytes();
            
            // Use absolute path for the upload directory
            String uploadDir = System.getProperty("user.dir") + "/received-files/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            final String filePath = uploadDir + pdfFile.getOriginalFilename();
            final File savedFile = new File(filePath);
            
            // Save the file
            pdfFile.transferTo(savedFile);
         
            // Verify file exists and has content
            if (savedFile.exists()) {
                System.out.println("File verification - Exists: true, Size: " + savedFile.length() + " bytes");
            } else {
                System.out.println("File verification - ERROR: File was not saved properly");
                return ResponseEntity.internalServerError().body("File was not saved properly");
            }
            // Send to RabbitMQ
            walletMessageProducer.sendAccountStatement(email, period, username, pdfBytes);
      
            // Schedule file deletion after 10 seconds
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                try {
                    if (savedFile.exists()) {
                        boolean deleted = savedFile.delete();
                        if (deleted) {
                            System.out.println("Temporary file deleted: " + savedFile.getAbsolutePath());
                        } else {
                            System.err.println("Failed to delete temporary file: " + savedFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting temporary file: " + e.getMessage());
                }
            }, 10, TimeUnit.SECONDS);
            scheduler.shutdown();
            
            return ResponseEntity.ok().body("Bank statement message successfully sent!");
            
        } catch (IOException | IllegalStateException e) {
            System.err.println("Error sending to RabbitMQ: " + e.getMessage());

            return ResponseEntity.internalServerError().body("Failed to send bank statement message.");
        }
    }

    @PostMapping("/send/swap-wallet-message")
    public ResponseEntity<Map<String, Object>> createSwapAlert(
            HttpServletRequest httpRequest,
            @Valid @RequestBody SwapCurrencyPayload request) {
               
        try {
            walletMessageProducer.sendSwapNotification(
                    request.getEmail(),
                    request.getAmount(),
                    request.getAccountHolder(),
                    request.getAvailableBalance(),
                    request.getPreviousBalance(),
                    request.getCurrencySymbol(),
                    request.getCurrencyExchange()
                    );

            Map<String, Object> response = new HashMap<>();
            response.put("status", request);
            response.put("message", "Swap message successfully sent!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send Swap message.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/send/welcome-message")
    public ResponseEntity<Map<String, Object>> createWelcomeMessage(HttpServletRequest httpRequest, @Valid @RequestBody WelcomeMessagePayload request) {
               
        try {
            authenticationMessageProducer.sendWelcomeNotification(
                    request.getEmail(),
                    request.getUsername(),
                    request.getMessage()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", request);
            response.put("message", "Welcome message successfully sent!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send welcome message.");
            return ResponseEntity.internalServerError().body(error);
        }
    }


    @PostMapping("/notifications/maintenance-fee")
    public ResponseEntity<Map<String, Object>> createMaintenanceAlert(
            HttpServletRequest httpRequest,
            @Valid @RequestBody MaintenanceDeductionNotification request) {

        try {
            walletMessageProducer.sendMaintenanceNotification(
                    request.getActionType(),
                    request.getAvailableBalance(),
                    request.getCurrency(),
                    request.getFeeAmount(),
                    request.getPreviousBalance(),
                    request.getReason(),
                    request.getSuccess(),
                    request.getTimestamp(),
                    request.getTotalAmountSpent(),
                    request.getUserEmail(),
                    request.getUserFirstName(),
                    request.getUserId(),
                    request.getUserLastName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Maintenance notification successfully sent!");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send maintenance notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }

    }


    @PostMapping("/notification/block-wallet-notification")
    public ResponseEntity<Map<String, Object>> createBlockUserWalletAlert(
            HttpServletRequest httpRequest,
            @RequestBody BlockUserWallet request) {

        try {
            walletMessageProducer.sendBlockUserWalletMessage(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getMessage()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Block user wallet notification successfully sent!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send maintenance notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/send/registration-otp-message")
    public ResponseEntity<Map<String, Object>> createRegistrationOtpMessage(
            HttpServletRequest httpRequest,
            @RequestBody RegistrationOtpMessage request) {
            System.out.println("Received request to send registration OTP to: " + request.getEmail());
            System.out.println("OTP Message Content: " + request.getMessage());
        try {
            authenticationMessageProducer.sendRegistrationOtpMessage( 
                    request.getEmail(),
                    request.getMessage()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Registration OTP message successfully sent!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send registration OTP message: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ── Login Alert ────────────────────────────────────────────────────────────

    @PostMapping("/send/login-alert")
    public ResponseEntity<Map<String, Object>> sendLoginAlert(
            HttpServletRequest httpRequest,
            @RequestBody LoginAlertNotification request) {
        try {
            authenticationMessageProducer.sendLoginAlert(request);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Login alert sent successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send login alert: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ── Wallet PIN Alert ───────────────────────────────────────────────────────

    @PostMapping("/send/wallet-pin-alert")
    public ResponseEntity<Map<String, Object>> sendWalletPinAlert(
            HttpServletRequest httpRequest,
            @RequestBody WalletPinNotification request) {
        try {
            walletMessageProducer.sendWalletPinAlert(request);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Wallet PIN alert sent successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send wallet PIN alert: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ── Account Security Alert (password, 2FA, lock, block, deactivate) ───────

    @PostMapping("/send/account-security-alert")
    public ResponseEntity<Map<String, Object>> sendAccountSecurityAlert(
            HttpServletRequest httpRequest,
            @RequestBody AccountSecurityNotification request) {
        try {
            authenticationMessageProducer.sendAccountSecurityAlert(request);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Account security alert sent successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to send account security alert: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
