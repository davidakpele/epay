package com.example.admin_api_service.models.settlementsAndReconciliation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ChargebackCaseStatus;
import com.example.admin_api_service.enums.ChargebackOutcome;
import com.example.admin_api_service.enums.ChargebackReason;
import com.example.admin_api_service.models.userAndWalletManagement.WalletFreeze;

@Entity
@Table(name = "chargeback_cases")
public class ChargebackCase {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Human-readable reference e.g. CHB-2024-00078
    @Column(name = "case_reference", length = 50, nullable = false, unique = true)
    private String caseReference;

    // Original transaction being disputed
    @Column(name = "transaction_id", length = 36, nullable = false)
    private String transactionId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ChargebackCaseStatus status = ChargebackCaseStatus.RAISED;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 50, nullable = false)
    private ChargebackReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    private ChargebackOutcome outcome;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "disputed_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal disputedAmount;

    // Amount actually recovered/reversed after resolution
    @Column(name = "recovered_amount", precision = 18, scale = 4)
    private BigDecimal recoveredAmount;

    @Column(name = "chargeback_fee", precision = 18, scale = 4)
    private BigDecimal chargebackFee = BigDecimal.ZERO;

    // Reference from card network or acquiring bank (Visa/Mastercard case number)
    @Column(name = "network_reference", length = 100)
    private String networkReference;

    // Reference from our acquiring bank
    @Column(name = "acquirer_reference", length = 100)
    private String acquirerReference;

    @Column(name = "description", length = 1000)
    private String description;

    // Evidence documents stored as JSON array of file URLs
    @Column(name = "evidence_urls", columnDefinition = "json")
    private String evidenceUrls;

    // Deadline by which we must respond to the chargeback
    @Column(name = "response_deadline")
    private LocalDate responseDeadline;

    // Whether a pre-arbitration was filed
    @Column(name = "pre_arbitration_filed", nullable = false)
    private boolean preArbitrationFiled = false;

    @Column(name = "pre_arbitration_date")
    private LocalDate preArbitrationDate;

    // Whether the case went to arbitration
    @Column(name = "arbitration_filed", nullable = false)
    private boolean arbitrationFiled = false;

    @Column(name = "arbitration_date")
    private LocalDate arbitrationDate;

    // Linked wallet freeze triggered by this chargeback
    @Column(name = "triggered_freeze_id", length = 36)
    private String triggeredFreezeId;

    // Linked manual adjustment created to reverse funds
    @Column(name = "reversal_adjustment_id", length = 36)
    private String reversalAdjustmentId;

    // Linked reconciliation exception if raised from one
    @Column(name = "reconciliation_exception_id", length = 36)
    private String reconciliationExceptionId;

    @Column(name = "raised_by", length = 36)
    private String raisedBy;

    @Column(name = "raised_at", nullable = false)
    private LocalDateTime raisedAt = LocalDateTime.now();

    @Column(name = "assigned_to", length = 36)
    private String assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_freeze_id", insertable = false, updatable = false)
    private WalletFreeze triggeredFreeze;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public ChargebackCase() {
    }

    public ChargebackCase(String id, String caseReference, String transactionId, Long walletId, Long userId, ChargebackCaseStatus status, ChargebackReason reason, ChargebackOutcome outcome, String currency, BigDecimal disputedAmount, BigDecimal recoveredAmount, BigDecimal chargebackFee, String networkReference, String acquirerReference, String description, String evidenceUrls, LocalDate responseDeadline, boolean preArbitrationFiled, LocalDate preArbitrationDate, boolean arbitrationFiled, LocalDate arbitrationDate, String triggeredFreezeId, String reversalAdjustmentId, String reconciliationExceptionId, String raisedBy, LocalDateTime raisedAt, String assignedTo, LocalDateTime assignedAt, String resolvedBy, LocalDateTime resolvedAt, String resolutionNote, LocalDateTime createdOn, LocalDateTime updatedOn, WalletFreeze triggeredFreeze) {
        this.id = id;
        this.caseReference = caseReference;
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.userId = userId;
        this.status = status;
        this.reason = reason;
        this.outcome = outcome;
        this.currency = currency;
        this.disputedAmount = disputedAmount;
        this.recoveredAmount = recoveredAmount;
        this.chargebackFee = chargebackFee;
        this.networkReference = networkReference;
        this.acquirerReference = acquirerReference;
        this.description = description;
        this.evidenceUrls = evidenceUrls;
        this.responseDeadline = responseDeadline;
        this.preArbitrationFiled = preArbitrationFiled;
        this.preArbitrationDate = preArbitrationDate;
        this.arbitrationFiled = arbitrationFiled;
        this.arbitrationDate = arbitrationDate;
        this.triggeredFreezeId = triggeredFreezeId;
        this.reversalAdjustmentId = reversalAdjustmentId;
        this.reconciliationExceptionId = reconciliationExceptionId;
        this.raisedBy = raisedBy;
        this.raisedAt = raisedAt;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.resolutionNote = resolutionNote;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.triggeredFreeze = triggeredFreeze;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCaseReference() { return caseReference; }
    public void setCaseReference(String caseReference) { this.caseReference = caseReference; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public ChargebackCaseStatus getStatus() { return status; }
    public void setStatus(ChargebackCaseStatus status) { this.status = status; }

    public ChargebackReason getReason() { return reason; }
    public void setReason(ChargebackReason reason) { this.reason = reason; }

    public ChargebackOutcome getOutcome() { return outcome; }
    public void setOutcome(ChargebackOutcome outcome) { this.outcome = outcome; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getDisputedAmount() { return disputedAmount; }
    public void setDisputedAmount(BigDecimal disputedAmount) { this.disputedAmount = disputedAmount; }

    public BigDecimal getRecoveredAmount() { return recoveredAmount; }
    public void setRecoveredAmount(BigDecimal recoveredAmount) { this.recoveredAmount = recoveredAmount; }

    public BigDecimal getChargebackFee() { return chargebackFee; }
    public void setChargebackFee(BigDecimal chargebackFee) { this.chargebackFee = chargebackFee; }

    public String getNetworkReference() { return networkReference; }
    public void setNetworkReference(String networkReference) { this.networkReference = networkReference; }

    public String getAcquirerReference() { return acquirerReference; }
    public void setAcquirerReference(String acquirerReference) { this.acquirerReference = acquirerReference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvidenceUrls() { return evidenceUrls; }
    public void setEvidenceUrls(String evidenceUrls) { this.evidenceUrls = evidenceUrls; }

    public LocalDate getResponseDeadline() { return responseDeadline; }
    public void setResponseDeadline(LocalDate responseDeadline) { this.responseDeadline = responseDeadline; }

    public boolean isPreArbitrationFiled() { return preArbitrationFiled; }
    public void setPreArbitrationFiled(boolean preArbitrationFiled) { this.preArbitrationFiled = preArbitrationFiled; }

    public LocalDate getPreArbitrationDate() { return preArbitrationDate; }
    public void setPreArbitrationDate(LocalDate preArbitrationDate) { this.preArbitrationDate = preArbitrationDate; }

    public boolean isArbitrationFiled() { return arbitrationFiled; }
    public void setArbitrationFiled(boolean arbitrationFiled) { this.arbitrationFiled = arbitrationFiled; }

    public LocalDate getArbitrationDate() { return arbitrationDate; }
    public void setArbitrationDate(LocalDate arbitrationDate) { this.arbitrationDate = arbitrationDate; }

    public String getTriggeredFreezeId() { return triggeredFreezeId; }
    public void setTriggeredFreezeId(String triggeredFreezeId) { this.triggeredFreezeId = triggeredFreezeId; }

    public String getReversalAdjustmentId() { return reversalAdjustmentId; }
    public void setReversalAdjustmentId(String reversalAdjustmentId) { this.reversalAdjustmentId = reversalAdjustmentId; }

    public String getReconciliationExceptionId() { return reconciliationExceptionId; }
    public void setReconciliationExceptionId(String reconciliationExceptionId) { this.reconciliationExceptionId = reconciliationExceptionId; }

    public String getRaisedBy() { return raisedBy; }
    public void setRaisedBy(String raisedBy) { this.raisedBy = raisedBy; }

    public LocalDateTime getRaisedAt() { return raisedAt; }
    public void setRaisedAt(LocalDateTime raisedAt) { this.raisedAt = raisedAt; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public WalletFreeze getTriggeredFreeze() { return triggeredFreeze; }
    public void setTriggeredFreeze(WalletFreeze triggeredFreeze) { this.triggeredFreeze = triggeredFreeze; }
}
