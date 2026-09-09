package com.epay.domain.auth.entity;

import com.epay.domain.auth.enums.KycDocumentType;
import com.epay.domain.auth.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kyc_doc_seq")
    @SequenceGenerator(name = "kyc_doc_seq", sequenceName = "kyc_document_seq", allocationSize = 1)
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

    @Column(name = "storage_reference", nullable = false)
    private String storageReference;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_country")
    private String issuingCountry;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
