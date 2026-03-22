package com.example.admin_api_service.models.complianceAndRisk;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.SanctionScreeningResult;
import com.example.admin_api_service.enums.SanctionScreeningTrigger;

@Entity
@Table(name = "sanction_screening_logs")
public class SanctionScreeningLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    // Transaction that triggered this screening (null for onboarding/periodic checks)
    @Column(name = "transaction_id", length = 36)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger", length = 30, nullable = false)
    private SanctionScreeningTrigger trigger;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20, nullable = false)
    private SanctionScreeningResult result;

    // Name searched against the sanction lists
    @Column(name = "screened_name", length = 200, nullable = false)
    private String screenedName;

    // Match confidence score (0.00 – 100.00)
    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    // ID of the SanctionList entry matched (null if no match)
    @Column(name = "matched_sanction_id", length = 36)
    private String matchedSanctionId;

    // Screening provider used (internal or third-party e.g. Dow Jones, Refinitiv)
    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    // Full provider response payload stored as JSON
    @Column(name = "provider_response", columnDefinition = "json")
    private String providerResponse;

    // Whether a human reviewed this result
    @Column(name = "manually_reviewed", nullable = false)
    private boolean manuallyReviewed = false;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    // Linked AML case if a match triggered case creation
    @Column(name = "aml_case_id", length = 36)
    private String amlCaseId;

    @Column(name = "screened_at", nullable = false)
    private LocalDateTime screenedAt = LocalDateTime.now();

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_sanction_id", insertable = false, updatable = false)
    private SanctionList matchedSanction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aml_case_id", insertable = false, updatable = false)
    private AmlCase amlCase;

    public SanctionScreeningLog() {
    }

    public SanctionScreeningLog(String id, Long userId, Long walletId, String transactionId, SanctionScreeningTrigger trigger, SanctionScreeningResult result, String screenedName, BigDecimal matchScore, String matchedSanctionId, String providerName, String providerReference, String providerResponse, boolean manuallyReviewed, String reviewedBy, LocalDateTime reviewedAt, String reviewNote, String amlCaseId, LocalDateTime screenedAt, LocalDateTime createdOn, SanctionList matchedSanction, AmlCase amlCase) {
        this.id = id;
        this.userId = userId;
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.trigger = trigger;
        this.result = result;
        this.screenedName = screenedName;
        this.matchScore = matchScore;
        this.matchedSanctionId = matchedSanctionId;
        this.providerName = providerName;
        this.providerReference = providerReference;
        this.providerResponse = providerResponse;
        this.manuallyReviewed = manuallyReviewed;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNote = reviewNote;
        this.amlCaseId = amlCaseId;
        this.screenedAt = screenedAt;
        this.createdOn = createdOn;
        this.matchedSanction = matchedSanction;
        this.amlCase = amlCase;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public SanctionScreeningTrigger getTrigger() { return trigger; }
    public void setTrigger(SanctionScreeningTrigger trigger) { this.trigger = trigger; }

    public SanctionScreeningResult getResult() { return result; }
    public void setResult(SanctionScreeningResult result) { this.result = result; }

    public String getScreenedName() { return screenedName; }
    public void setScreenedName(String screenedName) { this.screenedName = screenedName; }

    public BigDecimal getMatchScore() { return matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }

    public String getMatchedSanctionId() { return matchedSanctionId; }
    public void setMatchedSanctionId(String matchedSanctionId) { this.matchedSanctionId = matchedSanctionId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public boolean isManuallyReviewed() { return manuallyReviewed; }
    public void setManuallyReviewed(boolean manuallyReviewed) { this.manuallyReviewed = manuallyReviewed; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public String getAmlCaseId() { return amlCaseId; }
    public void setAmlCaseId(String amlCaseId) { this.amlCaseId = amlCaseId; }

    public LocalDateTime getScreenedAt() { return screenedAt; }
    public void setScreenedAt(LocalDateTime screenedAt) { this.screenedAt = screenedAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public SanctionList getMatchedSanction() { return matchedSanction; }
    public void setMatchedSanction(SanctionList matchedSanction) { this.matchedSanction = matchedSanction; }

    public AmlCase getAmlCase() { return amlCase; }
    public void setAmlCase(AmlCase amlCase) { this.amlCase = amlCase; }
}
