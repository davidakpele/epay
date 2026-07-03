package com.epay.auth.domain.entity;

import com.epay.domain.auth.enums.KycDocumentType;
import com.epay.domain.auth.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores individual KYC document submissions per user.
 * Each document is independently reviewable and auditable.
 * Never store the actual file here — store a secure reference (S3 key, etc).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kyc_documents", indexes = {
        @Index(name = "idx_kyc_doc_user_id", columnList = "user_id"),
        @Index(name = "idx_kyc_doc_type_status", columnList = "document_type, status")
})
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private KycDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    /**
     * Secure storage reference — S3 key, GCS path, etc.
     * Never a public URL. Access must go through a signed URL service.
     */
    @Column(name = "storage_reference", nullable = false)
    private String storageReference;

    /**
     * SHA-256 hash of the uploaded file for integrity verification.
     */
    @Column(name = "file_hash")
    private String fileHash;

    /** Document number on the ID (e.g. passport number). Encrypted at rest. */
    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_country")
    private String issuingCountry;

    // --- Review fields ---

    /** User ID of the compliance reviewer. */
    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /** Compliance reviewer note. Never returned to the client. */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // --- Audit ---

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Version for optimistic locking. */
    @Version
    private Long version;
}
