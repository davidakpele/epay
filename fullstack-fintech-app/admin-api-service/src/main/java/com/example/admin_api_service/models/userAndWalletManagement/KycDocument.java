package com.example.admin_api_service.models.userAndWalletManagement;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.KycDocumentStatus;
import com.example.admin_api_service.enums.KycDocumentType;

@Entity
@Table(name = "kyc_documents")
public class KycDocument {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_kyc_id", length = 36, nullable = false)
    private String userKycId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 50, nullable = false)
    private KycDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private KycDocumentStatus status = KycDocumentStatus.PENDING;

    // Document identity fields
    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "issuing_country", length = 50)
    private String issuingCountry;

    @Column(name = "issuing_authority", length = 100)
    private String issuingAuthority;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // Storage references (S3 keys, GCS paths, etc.)
    @Column(name = "front_file_url", length = 500)
    private String frontFileUrl;

    @Column(name = "back_file_url", length = 500)
    private String backFileUrl;

    @Column(name = "selfie_file_url", length = 500)
    private String selfieFileUrl;

    // Mime type of uploaded files
    @Column(name = "file_mime_type", length = 50)
    private String fileMimeType;

    // File size in bytes
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    // Result from external verification provider (e.g. Smile Identity, Youverify)
    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Column(name = "provider_response", columnDefinition = "json")
    private String providerResponse;

    // Review trail
    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_kyc_id", insertable = false, updatable = false)
    private UserKyc userKyc;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public KycDocument() {
    }

    public KycDocument(String id, String userKycId, Long userId, KycDocumentType documentType, KycDocumentStatus status, String documentNumber, String issuingCountry, String issuingAuthority, LocalDate issuedDate, LocalDate expiryDate, String frontFileUrl, String backFileUrl, String selfieFileUrl, String fileMimeType, Long fileSizeBytes, String providerName, String providerReference, String providerResponse, String reviewedBy, LocalDateTime reviewedAt, String rejectionReason, String reviewNote, LocalDateTime createdOn, LocalDateTime updatedOn, UserKyc userKyc) {
        this.id = id;
        this.userKycId = userKycId;
        this.userId = userId;
        this.documentType = documentType;
        this.status = status;
        this.documentNumber = documentNumber;
        this.issuingCountry = issuingCountry;
        this.issuingAuthority = issuingAuthority;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
        this.frontFileUrl = frontFileUrl;
        this.backFileUrl = backFileUrl;
        this.selfieFileUrl = selfieFileUrl;
        this.fileMimeType = fileMimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.providerName = providerName;
        this.providerReference = providerReference;
        this.providerResponse = providerResponse;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.reviewNote = reviewNote;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.userKyc = userKyc;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserKycId() { return userKycId; }
    public void setUserKycId(String userKycId) { this.userKycId = userKycId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public KycDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(KycDocumentType documentType) { this.documentType = documentType; }

    public KycDocumentStatus getStatus() { return status; }
    public void setStatus(KycDocumentStatus status) { this.status = status; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getIssuingCountry() { return issuingCountry; }
    public void setIssuingCountry(String issuingCountry) { this.issuingCountry = issuingCountry; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getFrontFileUrl() { return frontFileUrl; }
    public void setFrontFileUrl(String frontFileUrl) { this.frontFileUrl = frontFileUrl; }

    public String getBackFileUrl() { return backFileUrl; }
    public void setBackFileUrl(String backFileUrl) { this.backFileUrl = backFileUrl; }

    public String getSelfieFileUrl() { return selfieFileUrl; }
    public void setSelfieFileUrl(String selfieFileUrl) { this.selfieFileUrl = selfieFileUrl; }

    public String getFileMimeType() { return fileMimeType; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public UserKyc getUserKyc() { return userKyc; }
    public void setUserKyc(UserKyc userKyc) { this.userKyc = userKyc; }
}
