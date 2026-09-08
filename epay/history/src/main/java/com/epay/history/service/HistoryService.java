package com.epay.history.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epay.domain.history.dto.StateEntry;
import com.epay.domain.history.dto.StateMachine;
import com.epay.domain.history.dto.StateTransition;
import com.epay.domain.history.dto.StatusHistoryEntry;
import com.epay.domain.history.dto.StatusTimeline;
import com.epay.domain.history.dto.TransactionDTO;
import com.epay.domain.history.entity.Transaction;
import com.epay.domain.history.entity.TransactionAllowedTransition;
import com.epay.domain.history.entity.TransactionAuditLog;
import com.epay.domain.history.entity.TransactionStateEntry;
import com.epay.domain.history.entity.TransactionStatusHistory;
import com.epay.domain.history.enums.TransactionStatus;
import com.epay.history.repository.AuditLogRepository;
import com.epay.history.repository.TransactionAllowedTransitionRepository;
import com.epay.history.repository.TransactionRepository;
import com.epay.history.repository.TransactionStateEntryRepository;
import com.epay.history.repository.TransactionStatusHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.epay.domain.history.enums.TransactionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final TransactionRepository                transactionRepository;
    private final AuditLogRepository                   auditLogRepository;
    private final TransactionStateEntryRepository      stateEntryRepository;
    private final TransactionStatusHistoryRepository   statusHistoryRepository;
    private final TransactionAllowedTransitionRepository allowedTransitionRepository;

    @Transactional
    public Transaction record(RecordRequest req) {
        if (req.transactionId() != null
                && transactionRepository.existsByTransactionId(req.transactionId())) {
            log.warn("[History] Duplicate transactionId={} — skipping insert", req.transactionId());
            return transactionRepository.findByTransactionId(req.transactionId()).orElse(null);
        }

        String  txnId      = txnOrGenerate(req.transactionId());
        String  actor      = resolveActor(req.channel());
        boolean isInternal = "INTERNAL".equalsIgnoreCase(req.channel());

        TransactionStatus finalStatus = parseStatus(req.status());
        StatusTimeline    timeline    = buildTimeline(req, finalStatus, actor, isInternal);

        Transaction txn = Transaction.builder()
                .transactionId(txnId)
                .reference(req.reference())
                .idempotencyKey(req.idempotencyKey())
                .userId(req.userId())
                .walletId(req.walletId())
                .accountHolder(upper(req.accountHolder()))
                .counterpartyUserId(req.counterpartyUserId())
                .counterpartyWalletId(req.counterpartyWalletId())
                .counterpartyAccountHolder(upper(req.counterpartyAccountHolder()))
                .transactionType(req.transactionType())
                .debitCredit(req.debitCredit())
                .channel(req.channel())
                .currentStatus(finalStatus)
                .statusTimeline(timeline)
                .grossAmount(safe(req.grossAmount()))
                .feeAmount(safe(req.feeAmount()))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(safe(req.netAmount()))
                .previousBalance(req.previousBalance())
                .availableBalance(req.newBalance())
                .runningBalance(req.newBalance())
                .currency(req.currency())
                .currencySymbol(req.currencySymbol())
                .exchangeRate(BigDecimal.ONE)
                .description(req.description())
                .failureReason(req.failureReason())
                .ipAddress(req.ipAddress())
                .deviceId(req.deviceId())
                .userAgent(req.userAgent())
                .adminNote(req.adminNote())
                .completedAt(isTerminal(finalStatus) ? LocalDateTime.now() : req.completedAt())
                .build();

        transactionRepository.save(txn);

        persistStateEntries(txnId, timeline.getStateMachine());
        persistStatusHistory(txnId, timeline.getStatusHistory());
        persistAllowedTransitions(txnId, timeline.getStateMachine().getAllowedTransitions());

        audit(txnId, "CREATE", "SYSTEM", null, finalStatus,
                req.ipAddress(), req.deviceId(), null);

        log.info("[History] Recorded txn={} type={} status={} userId={}",
                txnId, req.transactionType(), finalStatus, req.userId());
        return txn;
    }

    @Transactional
    public Transaction advanceStatus(String transactionId, TransactionStatus newStatus,
                                      String actor, String message,
                                      String ipAddress, String deviceId, String reason) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + transactionId));

        TransactionStatus previous = txn.getCurrentStatus();
        txn.advanceStatus(newStatus, actor, message);

        StatusTimeline tl = txn.getStatusTimeline();
        if (tl != null && tl.getStateMachine() != null) {
            tl.getStateMachine().setCurrentState(newStatus);
        }

        transactionRepository.save(txn);

        Instant now = Instant.now();

        int updated = stateEntryRepository.visitState(transactionId, newStatus, now, actor, message);
        if (updated == 0) {
            long count = stateEntryRepository.findByTransactionId(transactionId).size();
            stateEntryRepository.save(TransactionStateEntry.builder()
                    .transactionId(transactionId)
                    .stateName(newStatus)
                    .visitedAt(now)
                    .actor(actor)
                    .message(message)
                    .onFailure(false)
                    .sortOrder((int) count)
                    .build());
        }

        long historyCount = statusHistoryRepository.countByTransactionId(transactionId);
        statusHistoryRepository.save(TransactionStatusHistory.builder()
                .transactionId(transactionId)
                .status(newStatus)
                .visitedAt(now)
                .actor(actor)
                .message(message)
                .sortOrder((int) historyCount)
                .build());
        long transitionCount = allowedTransitionRepository.findByTransactionId(transactionId).size();
        allowedTransitionRepository.save(TransactionAllowedTransition.builder()
                .transactionId(transactionId)
                .fromState(previous)
                .toState(newStatus)
                .sortOrder((int) transitionCount)
                .build());

        audit(transactionId, "STATUS_CHANGE", actor, previous, newStatus,
                ipAddress, deviceId, reason);

        log.info("[History] Status {} → {} for txn={}", previous, newStatus, transactionId);
        return txn;
    }

    private StatusTimeline buildTimeline(RecordRequest req,
                                          TransactionStatus finalStatus,
                                          String actor,
                                          boolean isInternal) {
        StatusTimeline timeline = new StatusTimeline();
        StateMachine   sm       = timeline.getStateMachine();
        String         type     = req.transactionType();
        String         rawFail  = req.failureReason();

        boolean isTransfer = type != null &&
                (type.equalsIgnoreCase("TRANSFER_DEBIT") || type.equalsIgnoreCase("TRANSFER_CREDIT"));

        if (isInternal && !isTransfer) {
            buildInternalGraph(sm);
        } else {
            buildExternalGraph(sm);
        }
        sm.setCurrentState(finalStatus);

        timeline.recordVisit(INITIATED, "SYSTEM", "Transaction created");
        if (finalStatus == INITIATED) return timeline;

        if (finalStatus == FAILED && isEarlyFailure(req.status())) {
            timeline.recordFailedVisit(INITIATED, "SYSTEM", "Transaction failed at initiation");
            sm.addTransition(INITIATED, FAILED);
            timeline.recordVisit(FAILED, "SYSTEM",
                    rawFail != null ? rawFail : type + " failed");
            return timeline;
        }

        sm.addTransition(INITIATED, PROCESSING);
        timeline.recordVisit(PROCESSING, "SYSTEM", processingMessage(type));
        if (finalStatus == PROCESSING) return timeline;


        if (finalStatus == FAILED && isFailedAt(req.status(), "PROCESSING")) {
            sm.visitStateFailed(PROCESSING, timeline.getStatusHistory()
                    .stream().filter(e -> e.getStatus() == PROCESSING)
                    .findFirst().map(StatusHistoryEntry::getTimestamp).orElse(Instant.now()),
                    "SYSTEM", processingMessage(type));
            sm.addTransition(PROCESSING, FAILED);
            timeline.recordVisit(FAILED, actor, failureMessage(type, rawFail));
            return timeline;
        }

        if (isInternal && !isTransfer) {
            if (finalStatus == SETTLED) {
                sm.addTransition(PROCESSING, SETTLED);
                timeline.recordVisit(SETTLED, "LEDGER",
                        "Ledger updated — " + lower(type) + " settled");
            } else if (finalStatus == FAILED) {
                sm.visitStateFailed(PROCESSING, timeline.getStatusHistory()
                        .stream().filter(e -> e.getStatus() == PROCESSING)
                        .findFirst().map(StatusHistoryEntry::getTimestamp).orElse(Instant.now()),
                        "SYSTEM", processingMessage(type));
                sm.addTransition(PROCESSING, FAILED);
                timeline.recordVisit(FAILED, actor, failureMessage(type, rawFail));
            } else if (finalStatus == CANCELLED) {
                sm.addTransition(PROCESSING, CANCELLED);
                timeline.recordVisit(CANCELLED, actor, "Transaction cancelled");
            } else {
                sm.addTransition(PROCESSING, DELIVERED);
                timeline.recordVisit(DELIVERED, "LEDGER", statusMessage(DELIVERED, type));
            }
        } else {
            sm.addTransition(PROCESSING, AUTHORIZED);
            timeline.recordVisit(AUTHORIZED, actor, "Payment authorized successfully");
            if (finalStatus == AUTHORIZED) return timeline;

            if (finalStatus == FAILED && isFailedAt(req.status(), "AUTHORIZED")) {
                sm.visitStateFailed(AUTHORIZED, timeline.getStatusHistory()
                        .stream().filter(e -> e.getStatus() == AUTHORIZED)
                        .findFirst().map(StatusHistoryEntry::getTimestamp).orElse(Instant.now()),
                        actor, "Payment authorization failed");
                sm.addTransition(AUTHORIZED, FAILED);
                timeline.recordVisit(FAILED, actor, failureMessage(type, rawFail));
                return timeline;
            }

            sm.addTransition(AUTHORIZED, SETTLEMENT_PENDING);
            timeline.recordVisit(SETTLEMENT_PENDING, "SYSTEM",
                    "Payment authorized and awaiting settlement");
            if (finalStatus == SETTLEMENT_PENDING) return timeline;

            if (finalStatus == FAILED && isFailedAt(req.status(), "SETTLEMENT")) {
                sm.visitStateFailed(SETTLEMENT_PENDING, timeline.getStatusHistory()
                        .stream().filter(e -> e.getStatus() == SETTLEMENT_PENDING)
                        .findFirst().map(StatusHistoryEntry::getTimestamp).orElse(Instant.now()),
                        actor, "Settlement failed");
                sm.addTransition(SETTLEMENT_PENDING, FAILED);
                timeline.recordVisit(FAILED, actor, failureMessage(type, rawFail));
                return timeline;
            }

            if (finalStatus == FAILED) {
                sm.addTransition(SETTLEMENT_PENDING, FAILED);
                timeline.recordVisit(FAILED, actor, failureMessage(type, rawFail));
            } else if (finalStatus == CANCELLED) {
                sm.addTransition(SETTLEMENT_PENDING, CANCELLED);
                timeline.recordVisit(CANCELLED, "SYSTEM", "Transaction cancelled");
            } else if (finalStatus == REVERSED) {
                sm.addTransition(SETTLEMENT_PENDING, REVERSED);
                timeline.recordVisit(REVERSED, actor, "Transaction reversed");
            } else if (finalStatus == SETTLED) {
                sm.addTransition(SETTLEMENT_PENDING, SETTLED);
                timeline.recordVisit(SETTLED, actor,
                        "Ledger updated — " + lower(type) + " settled");
            } else {
                sm.addTransition(SETTLEMENT_PENDING, DELIVERED);
                timeline.recordVisit(DELIVERED, "SYSTEM", statusMessage(DELIVERED, type));
            }
        }

        return timeline;
    }

    private void buildExternalGraph(StateMachine sm) {
        sm.declareState(INITIATED,          PROCESSING,         null);
        sm.declareState(PROCESSING,         AUTHORIZED,         FAILED);
        sm.declareState(AUTHORIZED,         SETTLEMENT_PENDING, null);
        sm.declareState(SETTLEMENT_PENDING, DELIVERED,          null);
        sm.declareState(DELIVERED,          null,               null);
        sm.declareState(FAILED,             null,               null);
    }

    private void buildInternalGraph(StateMachine sm) {
        sm.declareState(INITIATED,  PROCESSING, null);
        sm.declareState(PROCESSING, DELIVERED,  FAILED);
        sm.declareState(DELIVERED,  null,        null);
        sm.declareState(FAILED,     null,        null);
    }

    private void persistStateEntries(String txnId, StateMachine sm) {
        if (sm == null || sm.getStates() == null) return;
        List<TransactionStateEntry> rows = new ArrayList<>();
        int order = 0;
        for (var entry : sm.getStates().entrySet()) {
            TransactionStatus stateName = entry.getKey();
            StateEntry        se        = entry.getValue();
            rows.add(TransactionStateEntry.builder()
                    .transactionId(txnId)
                    .stateName(stateName)
                    .visitedAt(se != null ? se.getTimestamp() : null)
                    .actor(se != null ? se.getActor() : null)
                    .message(se != null ? se.getMessage() : null)
                    .nextState(se != null ? se.getNextState() : null)
                    .onFailure(se != null && se.isOnFailure())
                    .failureState(se != null ? se.getFailureState() : null)
                    .sortOrder(order++)
                    .build());
        }
        stateEntryRepository.saveAll(rows);
    }

    private void persistStatusHistory(String txnId, List<StatusHistoryEntry> entries) {
        if (entries == null || entries.isEmpty()) return;
        List<TransactionStatusHistory> rows = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            StatusHistoryEntry e = entries.get(i);
            rows.add(TransactionStatusHistory.builder()
                    .transactionId(txnId)
                    .status(e.getStatus())
                    .visitedAt(e.getTimestamp())
                    .actor(e.getActor())
                    .message(e.getMessage())
                    .sortOrder(i)
                    .build());
        }
        statusHistoryRepository.saveAll(rows);
    }

    private void persistAllowedTransitions(String txnId, List<StateTransition> transitions) {
        if (transitions == null || transitions.isEmpty()) return;
        List<TransactionAllowedTransition> rows = new ArrayList<>();
        for (int i = 0; i < transitions.size(); i++) {
            StateTransition t = transitions.get(i);
            rows.add(TransactionAllowedTransition.builder()
                    .transactionId(txnId)
                    .fromState(t.getFrom())
                    .toState(t.getTo())
                    .sortOrder(i)
                    .build());
        }
        allowedTransitionRepository.saveAll(rows);
    }

    public Page<TransactionDTO> getByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    public Page<TransactionDTO> getByUserIdAndType(Long userId, String type, Pageable pageable) {
        return transactionRepository.findByUserIdAndType(userId, type.toUpperCase(), pageable)
                .map(this::toDTO);
    }

    public Page<TransactionDTO> getByUserIdAndStatus(Long userId, TransactionStatus status,
                                                      Pageable pageable) {
        return transactionRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(this::toDTO);
    }

    public Page<TransactionDTO> getByWalletId(Long walletId, Pageable pageable) {
        return transactionRepository.findByWalletId(walletId, pageable).map(this::toDTO);
    }

    public Optional<TransactionDTO> getByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId).map(this::toDTO);
    }

    public Optional<TransactionDTO> getByReference(String reference) {
        return transactionRepository.findByReference(reference).map(this::toDTO);
    }

    public List<TransactionDTO> getByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByUserIdAndDateRange(userId, from, to)
                .stream().map(this::toDTO).toList();
    }

    public List<TransactionAuditLog> getAuditLog(String transactionId) {
        return auditLogRepository.findByTransactionId(transactionId);
    }

    public BigDecimal sumDeliveredByType(Long userId, String type) {
        BigDecimal result = transactionRepository
                .sumDeliveredByUserIdAndType(userId, type.toUpperCase());
        return result != null ? result : BigDecimal.ZERO;
    }

    public Page<TransactionDTO> filterByUser(Long userId,
                                              LocalDateTime fromDate,
                                              LocalDateTime toDate,
                                              String transactionType,
                                              String currency,
                                              TransactionStatus status,
                                              Pageable pageable) {
        String type = (transactionType == null || transactionType.isBlank())
                      ? "ALL" : transactionType.toUpperCase();
        String cur  = (currency == null || currency.isBlank())
                      ? null : currency.toUpperCase();
        LocalDateTime endOfDay = toDate.getHour() == 0 && toDate.getMinute() == 0
                ? toDate.withHour(23).withMinute(59).withSecond(59) : toDate;

        return transactionRepository
                .filterByUser(userId, fromDate, endOfDay, type, cur, status, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public void hideFromUser(String transactionId, Long userId) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + transactionId));

        if (!txn.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Transaction " + transactionId + " does not belong to user " + userId);
        }

        if (!txn.isPublishAccess()) {
            log.debug("[History] Transaction {} already hidden for userId={}", transactionId, userId);
            return;
        }

        int updated = transactionRepository.hideFromUser(transactionId, userId);
        if (updated > 0) {
            audit(transactionId, "USER_HIDE", String.valueOf(userId),
                    txn.getCurrentStatus(), txn.getCurrentStatus(), null, null,
                    "User requested removal from history view");
            log.info("[History] Transaction {} hidden from user={}", transactionId, userId);
        }
    }

    @Transactional
    public void restoreToUser(String transactionId) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + transactionId));

        if (txn.isPublishAccess()) {
            log.debug("[History] Transaction {} is already visible — restore is a no-op", transactionId);
            return;
        }

        int updated = transactionRepository.restoreToUser(transactionId);
        if (updated > 0) {
            audit(transactionId, "ADMIN_RESTORE", "ADMIN",
                    txn.getCurrentStatus(), txn.getCurrentStatus(), null, null,
                    "Admin restored transaction visibility");
            log.info("[History] Transaction {} restored to visible by admin", transactionId);
        }
    }

    public Page<TransactionDTO> getHiddenTransactions(Pageable pageable) {
        return transactionRepository.findAllHidden(pageable).map(t -> toDTO(t, true));
    }


    public Page<TransactionDTO> getHiddenByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findHiddenByUserId(userId, pageable).map(t -> toDTO(t, true));
    }


    public Page<TransactionDTO> getByUserIdAdmin(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdAdmin(userId, pageable).map(t -> toDTO(t, true));
    }


    private TransactionDTO toDTO(Transaction t) {
        return toDTO(t, false);
    }


    private TransactionDTO toDTO(Transaction t, boolean includeAdminFields) {
        String txnId = t.getTransactionId();

        StateMachine           dbSm         = buildStateMachineFromDb(txnId, t.getCurrentStatus());
        List<StatusHistoryEntry> dbHistory  = buildStatusHistoryFromDb(txnId);

        StatusTimeline tl = t.getStatusTimeline();
        StateMachine           sm      = (dbSm.getStates() != null && !dbSm.getStates().isEmpty())
                ? dbSm : (tl != null ? tl.getStateMachine() : null);
        List<StatusHistoryEntry> history = !dbHistory.isEmpty()
                ? dbHistory : (tl != null ? tl.getStatusHistory() : List.of());

        TransactionDTO.SettlementInfo settlement = null;
        if (t.getCurrentStatus() == DELIVERED || t.getCurrentStatus() == SETTLED) {
            Instant settledAt = t.getCompletedAt() != null
                    ? t.getCompletedAt().toInstant(java.time.ZoneOffset.UTC) : null;
            settlement = TransactionDTO.SettlementInfo.builder()
                    .status("SETTLED")
                    .settlementReference("SET_" + txnId.replaceAll("[^A-Za-z0-9]", ""))
                    .settledAt(settledAt)
                    .build();
        }

        TransactionDTO.FailureInfo failure = null;
        if (t.getCurrentStatus() == FAILED && t.getFailureReason() != null) {
            failure = TransactionDTO.FailureInfo.builder()
                    .reason(t.getFailureReason())
                    .failedAt(t.getCompletedAt() != null
                            ? t.getCompletedAt().toInstant(java.time.ZoneOffset.UTC) : null)
                    .build();
        }

        Instant createdAt   = t.getCreatedAt()   != null
                ? t.getCreatedAt().toInstant(java.time.ZoneOffset.UTC)   : null;
        Instant updatedAt   = t.getUpdatedAt()   != null
                ? t.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC)   : null;
        Instant completedAt = t.getCompletedAt() != null
                ? t.getCompletedAt().toInstant(java.time.ZoneOffset.UTC) : null;

        BigDecimal balanceChange = t.getNetAmount();
        if ("DEBIT".equalsIgnoreCase(t.getDebitCredit()) && balanceChange != null) {
            balanceChange = balanceChange.negate();
        }

        TransactionDTO.LedgerInfo ledger = TransactionDTO.LedgerInfo.builder()
                .ledgerEntryId("LED_" + txnId.replaceAll("[^A-Za-z0-9]", ""))
                .ledgerStatus(isTerminal(t.getCurrentStatus()) ? "POSTED" : "PENDING")
                .build();

        boolean isInternal = "INTERNAL".equalsIgnoreCase(t.getChannel());
        TransactionDTO.ProviderInfo provider = null;
        if (!isInternal) {
            provider = TransactionDTO.ProviderInfo.builder()
                    .name(resolveActor(t.getChannel()))
                    .providerReference(t.getReference())
                    .build();
        }

        return TransactionDTO.builder()
                .historyId(t.getId())
                .transactionId(txnId)
                .reference(t.getReference())
                .idempotencyKey(t.getIdempotencyKey())
                .publishAccess(includeAdminFields ? t.isPublishAccess() : null)
                .transactionType(t.getTransactionType())
                .transactionCategory(resolveCategory(t.getTransactionType()))
                .debitCredit(t.getDebitCredit())
                .channel(t.getChannel())
                .status(t.getCurrentStatus())
                .stateMachine(sm)
                .statusHistory(history)
                .amount(TransactionDTO.AmountInfo.builder()
                        .currency(t.getCurrency())
                        .symbol(t.getCurrencySymbol())
                        .gross(t.getGrossAmount())
                        .fee(t.getFeeAmount())
                        .tax(t.getTaxAmount())
                        .net(t.getNetAmount())
                        .build())
                .balance(TransactionDTO.BalanceInfo.builder()
                        .currency(t.getCurrency())
                        .previous(t.getPreviousBalance())
                        .change(balanceChange)
                        .running(t.getRunningBalance())
                        .available(t.getAvailableBalance())
                        .build())
                .provider(provider)
                .settlement(settlement)
                .failure(failure)
                .description(t.getDescription())
                .metadata(TransactionDTO.MetadataInfo.builder()
                        .walletId(t.getWalletId())
                        .accountType(t.getCurrency())
                        .paymentMethod(t.getChannel())
                        .build())
                .relatedTransactions(List.of())
                .ledger(ledger)
                .user(TransactionDTO.UserInfo.builder()
                        .id(t.getUserId())
                        .walletId(t.getWalletId())
                        .accountHolder(t.getAccountHolder())
                        .build())
                .recipient(t.getCounterpartyUserId() != null
                        ? TransactionDTO.UserInfo.builder()
                                .id(t.getCounterpartyUserId())
                                .walletId(t.getCounterpartyWalletId())
                                .accountHolder(t.getCounterpartyAccountHolder())
                                .build()
                        : null)
                .timestamps(TransactionDTO.TimestampsInfo.builder()
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .completedAt(completedAt)
                        .build())
                .build();
    }

    private StateMachine buildStateMachineFromDb(String txnId, TransactionStatus currentStatus) {
        StateMachine sm = new StateMachine();
        sm.setCurrentState(currentStatus);
        List<TransactionStateEntry> stateRows = stateEntryRepository.findByTransactionId(txnId);
        if (!stateRows.isEmpty()) {
            LinkedHashMap<TransactionStatus, StateEntry> states = new LinkedHashMap<>();
            for (TransactionStateEntry row : stateRows) {
                StateEntry se = StateEntry.builder()
                        .timestamp(row.getVisitedAt())
                        .actor(row.getActor())
                        .message(row.getMessage())
                        .nextState(row.getNextState())
                        .onFailure(row.isOnFailure())
                        .failureState(row.getFailureState())
                        .build();
                states.put(row.getStateName(), se);
            }
            sm.setStates(states);
        }

        List<TransactionAllowedTransition> transitionRows =
                allowedTransitionRepository.findByTransactionId(txnId);
        if (!transitionRows.isEmpty()) {
            List<StateTransition> transitions = new ArrayList<>();
            for (TransactionAllowedTransition row : transitionRows) {
                transitions.add(StateTransition.builder()
                        .from(row.getFromState())
                        .to(row.getToState())
                        .build());
            }
            sm.setAllowedTransitions(transitions);
        }

        return sm;
    }

    private List<StatusHistoryEntry> buildStatusHistoryFromDb(String txnId) {
        List<TransactionStatusHistory> rows = statusHistoryRepository.findByTransactionId(txnId);
        if (rows.isEmpty()) return List.of();

        List<StatusHistoryEntry> result = new ArrayList<>(rows.size());
        for (TransactionStatusHistory row : rows) {
            result.add(StatusHistoryEntry.builder()
                    .status(row.getStatus())
                    .timestamp(row.getVisitedAt())
                    .actor(row.getActor())
                    .message(row.getMessage())
                    .build());
        }
        return result;
    }

    private void audit(String txnId, String action, String performedBy,
                        TransactionStatus previous, TransactionStatus newStatus,
                        String ip, String deviceId, String reason) {
        try {
            auditLogRepository.save(TransactionAuditLog.builder()
                    .transactionId(txnId)
                    .action(action)
                    .performedBy(performedBy != null ? performedBy : "SYSTEM")
                    .previousStatus(previous)
                    .newStatus(newStatus)
                    .ipAddress(ip)
                    .deviceId(deviceId)
                    .reason(reason)
                    .build());
        } catch (Exception e) {
            log.warn("[Audit] Failed to write audit for txn={}: {}", txnId, e.getMessage());
        }
    }

    private String txnOrGenerate(String id) {
        if (id != null && !id.isBlank()) return id;
        return UUID.randomUUID().toString();
    }
    private TransactionStatus parseStatus(String status) {
        if (status == null) return DELIVERED;
        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED", "DELIVERED"      -> DELIVERED;
            case "SETTLED"                                -> SETTLED;
            case "SETTLEMENT_PENDING"                     -> SETTLEMENT_PENDING;
            case "AUTHORIZED"                             -> AUTHORIZED;
            case "PROCESSING"                             -> PROCESSING;
            case "PROCESSED"                              -> PROCESSING;
            case "FAILED",
                 "FAILED_AT_PROCESSING",
                 "FAILED_AT_PROCESSED",
                 "FAILED_AT_AUTHORIZED",
                 "FAILED_AT_SETTLEMENT",
                 "FAILED_AT_PENDING"      -> FAILED;
            case "CANCELLED"                              -> CANCELLED;
            case "REVERSED"                               -> REVERSED;
            default                                       -> DELIVERED;
        };
    }

    private boolean isEarlyFailure(String rawStatus) {
        return rawStatus != null && rawStatus.equalsIgnoreCase("FAILED");
    }

    private boolean isFailedAt(String rawStatus, String step) {
        if (rawStatus == null) return false;
        String s = rawStatus.toUpperCase();
        return s.equals("FAILED_AT_" + step.toUpperCase())
                || (step.equalsIgnoreCase("PROCESSING") && s.equals("FAILED_AT_PROCESSED"))
                || (step.equalsIgnoreCase("SETTLEMENT") && s.equals("FAILED_AT_PENDING"));
    }

    private String resolveActor(String channel) {
        if (channel == null) return "SYSTEM";
        return switch (channel.toUpperCase()) {
            case "PAYSTACK"              -> "PAYSTACK";
            case "FLUTTERWAVE"           -> "FLUTTERWAVE";
            case "CARD", "USSD"         -> "PAYSTACK";
            case "BANK", "BANK_TRANSFER" -> "BANK";
            case "INTERNAL"              -> "LEDGER";
            default                      -> "SYSTEM";
        };
    }

    private String resolveCategory(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "DEPOSIT"                              -> "WALLET_FUNDING";
            case "WITHDRAWAL", "WITHDRAWAL_DEBIT",
                 "WITHDRAWAL_CREDIT"                   -> "WALLET_WITHDRAWAL";
            case "TRANSFER_DEBIT", "TRANSFER_CREDIT"   -> "PEER_TRANSFER";
            case "SWAP"                                 -> "CURRENCY_EXCHANGE";
            case "BILL_PAYMENT"                         -> "BILL_PAYMENT";
            case "ESCROW_LOCK", "ESCROW_RELEASE"        -> "ESCROW";
            case "REVERSAL"                             -> "REVERSAL";
            default                                     -> type.toUpperCase();
        };
    }

    private String processingMessage(String type) {
        if (type == null) return "Transaction validated and processing started";
        return switch (type.toUpperCase()) {
            case "DEPOSIT"                              -> "Transaction validated and processing started";
            case "WITHDRAWAL", "WITHDRAWAL_DEBIT",
                 "WITHDRAWAL_CREDIT"                   -> "Withdrawal validated, funds reserved";
            case "TRANSFER_DEBIT"                       -> "Transfer validated, funds debited from sender";
            case "TRANSFER_CREDIT"                      -> "Transfer validated, funds credited to recipient";
            case "SWAP"                                 -> "Swap validated, exchange rate locked";
            default                                     -> "Transaction validated and processing started";
        };
    }

    private String statusMessage(TransactionStatus status, String type) {
        String t = type != null ? type.toUpperCase() : "TRANSACTION";
        return switch (status) {
            case DELIVERED -> switch (t) {
                case "TRANSFER_DEBIT"   -> "Funds transferred successfully";
                case "TRANSFER_CREDIT"  -> "Transfer received successfully";
                case "WITHDRAWAL"       -> "Withdrawal completed successfully";
                case "SWAP"             -> "Swap completed successfully";
                default                 -> "Transaction completed successfully";
            };
            case SETTLED            -> "Ledger updated — " + lower(t) + " settled";
            case FAILED             -> switch (t) {
                case "TRANSFER_DEBIT", "TRANSFER_CREDIT" -> "Transfer failed";
                case "WITHDRAWAL"                        -> "Withdrawal failed";
                default                                  -> "Transaction failed";
            };
            case CANCELLED          -> "Transaction cancelled";
            case REVERSED           -> "Transaction reversed";
            case AUTHORIZED         -> "Payment authorized successfully";
            case SETTLEMENT_PENDING -> "Payment authorized and awaiting settlement";
            case PROCESSING         -> processingMessage(type);
            default                 -> status.name();
        };
    }

    private String failureMessage(String type, String reason) {
        String t = type != null ? type : "Transaction";
        return reason != null && !reason.isBlank() ? reason : t + " failed";
    }

    private boolean isTerminal(TransactionStatus status) {
        return status == DELIVERED || status == SETTLED || status == FAILED
                || status == CANCELLED || status == REVERSED || status == EXPIRED;
    }

    private String upper(String v) { return v != null ? v.toUpperCase() : null; }
    private String lower(String v) { return v != null ? v.toLowerCase() : ""; }
    private BigDecimal safe(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    public record RecordRequest(
            Long          userId,
            Long          walletId,
            String        transactionId,
            String        reference,
            String        idempotencyKey,
            String        transactionType,
            String        debitCredit,
            String        channel,
            String        status,
            BigDecimal    grossAmount,
            BigDecimal    feeAmount,
            BigDecimal    netAmount,
            BigDecimal    previousBalance,
            BigDecimal    newBalance,
            String        currency,
            String        currencySymbol,
            String        accountHolder,
            String        description,
            String        failureReason,
            String        counterpartyAccountHolder,
            Long          counterpartyUserId,
            Long          counterpartyWalletId,
            String        ipAddress,
            String        deviceId,
            String        userAgent,
            String        adminNote,
            LocalDateTime completedAt
    ) {
        public static RecordRequest of(Long userId, Long walletId,
                                        String transactionId, String reference,
                                        String transactionType, String debitCredit,
                                        String channel, String status,
                                        BigDecimal grossAmount, BigDecimal feeAmount,
                                        BigDecimal netAmount, BigDecimal previousBalance,
                                        BigDecimal newBalance, String currency,
                                        String currencySymbol, String accountHolder,
                                        String description,
                                        String counterpartyAccountHolder,
                                        Long counterpartyUserId, Long counterpartyWalletId,
                                        String ipAddress, String deviceId, String userAgent,
                                        String adminNote, LocalDateTime completedAt) {
            return new RecordRequest(userId, walletId, transactionId, reference,
                    null,
                    transactionType, debitCredit, channel, status,
                    grossAmount, feeAmount, netAmount, previousBalance, newBalance,
                    currency, currencySymbol, accountHolder, description, null,
                    counterpartyAccountHolder, counterpartyUserId, counterpartyWalletId,
                    ipAddress, deviceId, userAgent, adminNote, completedAt);
        }
    }
}
