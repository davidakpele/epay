package com.example.escrow_service.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.escrow_service.clients.WalletServiceClient;
import com.example.escrow_service.enums.Status;
import com.example.escrow_service.models.CurrencyBalance;
import com.example.escrow_service.models.Escrow;
import com.example.escrow_service.models.Ledger;
import com.example.escrow_service.payloads.CreateEscrowRequest;
import com.example.escrow_service.payloads.WalletRefundRequest;
import com.example.escrow_service.repositories.EscrowRepository;
import com.example.escrow_service.repositories.LegderRepository;
import jakarta.transaction.Transactional;

@Service
public class EscrowService {

    private final EscrowRepository escrowRepository;
    private final LegderRepository legderRepository;
    private final WalletServiceClient walletServiceClient;

    public EscrowService(EscrowRepository escrowRepository, LegderRepository legderRepository, WalletServiceClient walletServiceClient) {
        this.escrowRepository = escrowRepository;
        this.legderRepository = legderRepository;
        this.walletServiceClient = walletServiceClient;
    }

    public ResponseEntity<?> create(CreateEscrowRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (request.getAmount() == null) {
            response.put("status", "error");
            response.put("message", "Amount cannot be null");
            return ResponseEntity.badRequest().body(response);
        }
        List<Escrow> existingEscrows = escrowRepository.findAll();
        Escrow savedOrUpdatedEscrow;
        if (!existingEscrows.isEmpty()) {
            Escrow escrow = existingEscrows.get(0); 
            List<CurrencyBalance> updatedBalances = new ArrayList<>();
            boolean walletFound = false;
            
            for (CurrencyBalance balance : escrow.getBalances()) {
                if (balance.getCurrencyCode().equals(request.getCurrencyCode())) {
    
                    BigDecimal currentBalance = balance.getBalance();
                    if (currentBalance == null) {
                        currentBalance = BigDecimal.ZERO;
                    }
                    BigDecimal newBalance = currentBalance.add(request.getAmount());
                    CurrencyBalance updatedWallet = new CurrencyBalance(
                        balance.getCurrencyCode(), 
                        balance.getCurrencySymbol(), 
                        newBalance
                    );
                    updatedBalances.add(updatedWallet);
                    walletFound = true;
                } else {
                    BigDecimal currentBalance = balance.getBalance();
                    if (currentBalance == null) {
                        currentBalance = BigDecimal.ZERO;
                    }
                    CurrencyBalance safeBalance = new CurrencyBalance(
                        balance.getCurrencyCode(),
                        balance.getCurrencySymbol(),
                        currentBalance
                    );
                    updatedBalances.add(safeBalance);
                }
            }
            
            if (!walletFound) {
                CurrencyBalance newWallet = new CurrencyBalance(
                    request.getCurrencyCode(), 
                    getCurrencySymbol(request.getCurrencyCode()), 
                    request.getAmount()
                );
                updatedBalances.add(newWallet);
            }
            escrow.setBalances(updatedBalances);
            savedOrUpdatedEscrow = escrowRepository.save(escrow);
            response.put("message", "Escrow balance updated successfully");
            
        } else {
            Escrow newEscrow = new Escrow();
            CurrencyBalance newWallet = new CurrencyBalance(
                request.getCurrencyCode(),
                getCurrencySymbol(request.getCurrencyCode()),
                request.getAmount()
            );
            newEscrow.addBalance(newWallet);
            
            savedOrUpdatedEscrow = escrowRepository.save(newEscrow);
            response.put("message", "New escrow created successfully");
        }
        
        String uniqueId = generateUniqueId(); 
        Ledger ledger = new Ledger();
        ledger.setId(uniqueId);
        ledger.setAmount(request.getAmount());
        ledger.setCurrency(request.getCurrencyCode());
        ledger.setDescription(request.getDescription());
        ledger.setSenderId(request.getSenderId());
        ledger.setRecipientId(request.getRecipientId());
        ledger.setStatus(Status.PENDING.toString());
        legderRepository.save(ledger);
        response.put("status", "success");
        response.put("escrow", savedOrUpdatedEscrow);
        response.put("ledger", ledger);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> fetchLedgerById(String ledgerId) {
        Map<String, Object> response = new LinkedHashMap<>();
        Optional<Ledger> ledger = legderRepository.findById(ledgerId);
        if (ledger.isPresent()) {
            response.put("status", "success");
            response.put("message", "Ledger found successfully");
            response.put("ledger", ledger.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Ledger not found with ID: " + ledgerId);
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(response);
        }
    }

    public ResponseEntity<?> deleteLedgerById(String ledgerId) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (legderRepository.existsById(ledgerId)) {
            legderRepository.deleteById(ledgerId);
            response.put("status", "success");
            response.put("message", "Ledger deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Ledger not found with ID: " + ledgerId);
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(response);
        }
    }

    public ResponseEntity<?> updateLedgerStatusById(String ledgerId) {
        Map<String, Object> response = new LinkedHashMap<>();
        Optional<Ledger> optionalLedger = legderRepository.findById(ledgerId);
        if (optionalLedger.isPresent()) {
            Ledger ledger = optionalLedger.get();
            try {
                ledger.setStatus(Status.RELEASED.toString());
                Ledger updatedLedger = legderRepository.save(ledger);
                response.put("status", "success");
                response.put("message", "Ledger status updated successfully");
                response.put("ledger", updatedLedger);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                response.put("status", "error");
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            response.put("status", "error");
            response.put("message", "Ledger not found with ID: " + ledgerId);
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(response);
        }
    }

    @Scheduled(fixedRate = 3000) 
    @Transactional
    public void processPendingLedgers() {
        List<Ledger> pendingLedgers = legderRepository.findByStatus(Status.PENDING.toString());
        for (Ledger ledger : pendingLedgers) {
            try {
                processRefund(ledger);
            } catch (Exception e) {
                System.out.println("Failed to process refund for ledger: " + ledger.getId() + " - Error: " + e.getMessage());
            }
        }
    }

    private void processRefund(Ledger ledger) {
        boolean refundSuccess = callWalletService(ledger);
        if (refundSuccess) {
            boolean escrowUpdated = deductFromEscrow(ledger);
            if (escrowUpdated) {
                ledger.setStatus(Status.REFUNDED.toString());
                legderRepository.save(ledger);
            } 
        } 
    }

    private boolean callWalletService(Ledger ledger) {
        try {
            WalletRefundRequest refundRequest = new WalletRefundRequest();
            refundRequest.setAmount(ledger.getAmount());
            refundRequest.setCurrencyCode(ledger.getCurrency());
            refundRequest.setSenderId(ledger.getSenderId());
            boolean success = walletServiceClient.refundWallet(refundRequest);
            return success;
        } catch (Exception e) {
            System.out.println("Failed to call wallet-service for ledger: " + ledger.getId() + " - Error: " + e.getMessage());
            return false;
        }
    }


     private boolean deductFromEscrow(Ledger ledger) {
        List<Escrow> existingEscrows = escrowRepository.findAll();
        Escrow escrow = existingEscrows.get(0);
        List<CurrencyBalance> updatedBalances = new ArrayList<>();
        @SuppressWarnings("unused")
        boolean currencyFound = false;
        for (CurrencyBalance balance : escrow.getBalances()) {
            if (balance.getCurrencyCode().equals(ledger.getCurrency())) {
                BigDecimal currentBalance = balance.getBalance() != null ? balance.getBalance() : BigDecimal.ZERO;
                BigDecimal newBalance = currentBalance.subtract(ledger.getAmount());
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Insufficient balance in escrow for currency: " + ledger.getCurrency());
                    return false;
                }
                
                CurrencyBalance updatedWallet = new CurrencyBalance(
                    balance.getCurrencyCode(), 
                    balance.getCurrencySymbol(), 
                    newBalance
                );
                updatedBalances.add(updatedWallet);
                currencyFound = true;
            } else {
                updatedBalances.add(balance);
            }
        }
        escrow.setBalances(updatedBalances);
        escrowRepository.save(escrow);
        return true;
    }

    private String getCurrencySymbol(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "NGN" -> "₦";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "AUD" -> "A$";
            case "CAD" -> "C$";
            case "CHF" -> "CHF";
            case "CNY" -> "¥";
            case "INR" -> "₹";
            default -> currencyCode;
        };
    }

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public ResponseEntity<?> fetchAllLedgers() {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Ledger> ledgers = legderRepository.findAll();
        response.put("status", "success");
        response.put("message", "Ledgers fetched successfully");
        response.put("ledgers", ledgers);
        return ResponseEntity.ok(response);
    }
    
}