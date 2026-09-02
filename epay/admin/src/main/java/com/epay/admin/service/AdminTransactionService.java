package com.epay.admin.service;

import com.epay.common.exception.*;
import com.epay.domain.admin.dto.AdminTransactionDTO;
import com.epay.domain.admin.input.AdminFlagTransactionRequest;
import com.epay.domain.admin.input.AdminTransactionNoteRequest;
import com.epay.domain.history.entity.TransactionHistory;
import com.epay.history.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    public Page<AdminTransactionDTO> searchTransactions(
            Long userId, String status, String type, String currency,
            BigDecimal minAmount, BigDecimal maxAmount,
            LocalDateTime from, LocalDateTime to,
            Boolean amlFlag, Pageable pageable) {

        return transactionHistoryRepository.adminSearch(
                userId, status != null ? status.toUpperCase() : null,
                type, currency != null ? currency.toUpperCase() : null,
                minAmount, maxAmount, from, to, amlFlag, pageable)
                .map(this::toDTO);
    }

    public AdminTransactionDTO getByTransactionId(String txnId) {
        return transactionHistoryRepository.findByTransactionId(txnId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + txnId));
    }

    public Page<AdminTransactionDTO> getByUserId(Long userId, Pageable pageable) {
        return transactionHistoryRepository.findByUserId(userId, pageable).map(this::toDTO);
    }


    @Transactional
    public AdminTransactionDTO addNote(String txnId, AdminTransactionNoteRequest request, String reviewerUsername) {
        TransactionHistory txn = requireTransaction(txnId);
        transactionHistoryRepository.updateAdminNote(txnId, request.getNote(), reviewerUsername);
        txn.setAdminNote(request.getNote());
        txn.setReviewedBy(reviewerUsername);
        log.info("[Admin] Transaction note added: txnId={} by={}", txnId, reviewerUsername);
        return toDTO(txn);
    }

    @Transactional
    public AdminTransactionDTO flagTransaction(String txnId, AdminFlagTransactionRequest request,
                                               String reviewerUsername) {
        TransactionHistory txn = requireTransaction(txnId);

        transactionHistoryRepository.updateAmlFlag(txnId, request.isAmlFlag(), request.getComplianceNote());

        if (request.getDisputeStatus() != null) {
            transactionHistoryRepository.updateDisputeStatus(
                    txnId, request.getDisputeStatus(), request.getDisputeReference());
        }

        txn.setAmlFlag(request.isAmlFlag());
        txn.setComplianceNote(request.getComplianceNote());
        if (request.getDisputeStatus() != null) {
            txn.setDisputeStatus(request.getDisputeStatus());
            txn.setDisputeReference(request.getDisputeReference());
        }
        txn.setReviewedBy(reviewerUsername);
        log.info("[Admin] Transaction flagged: txnId={} aml={} by={}", txnId, request.isAmlFlag(), reviewerUsername);
        return toDTO(txn);
    }

    public java.util.Map<String, Object> getTransactionStats(Long userId) {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalTransactions", transactionHistoryRepository.countByUserIdSince(userId,
                LocalDateTime.of(2000, 1, 1, 0, 0)));
        stats.put("totalDeposits",     transactionHistoryRepository.sumByUserIdAndType(userId, "DEPOSIT"));
        stats.put("totalWithdrawals",  transactionHistoryRepository.sumByUserIdAndType(userId, "WITHDRAWAL"));
        stats.put("totalTransfers",    transactionHistoryRepository.sumByUserIdAndType(userId, "TRANSFER_DEBIT"));
        return stats;
    }

    private TransactionHistory requireTransaction(String txnId) {
        return transactionHistoryRepository.findByTransactionId(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + txnId));
    }

    public AdminTransactionDTO toDTO(TransactionHistory h) {
        return AdminTransactionDTO.builder()
                .id(h.getId())
                .transactionId(h.getTransactionId())
                .reference(h.getReference())
                .userId(h.getUserId())
                .accountHolder(h.getAccountHolder())
                .transactionType(h.getTransactionType())
                .debitCredit(h.getDebitCredit())
                .channel(h.getChannel())
                .status(h.getStatus())
                .grossAmount(h.getGrossAmount())
                .feeAmount(h.getFeeAmount())
                .netAmount(h.getNetAmount())
                .currency(h.getCurrencyType())
                .currencySymbol(h.getCurrencySymbol())
                .previousBalance(h.getPreviousBalance())
                .runningBalance(h.getRunningBalance())
                .description(h.getDescription())
                .failureReason(h.getFailureReason())
                .adminNote(h.getAdminNote())
                .amlFlag(h.isAmlFlag())
                .complianceNote(h.getComplianceNote())
                .disputeStatus(h.getDisputeStatus())
                .disputeReference(h.getDisputeReference())
                .ipAddress(h.getIpAddress())
                .reviewedBy(h.getReviewedBy())
                .counterpartyUserId(h.getCounterpartyUserId())
                .counterpartyAccountHolder(h.getCounterpartyAccountHolder())
                .bankCode(h.getBankCode())
                .bankAccountNumber(h.getBankAccountNumber())
                .createdAt(h.getCreatedAt())
                .completedAt(h.getCompletedAt())
                .build();
    }
}
