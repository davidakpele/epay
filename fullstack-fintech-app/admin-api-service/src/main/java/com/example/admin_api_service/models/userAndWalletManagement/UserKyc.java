package com.example.admin_api_service.models.userAndWalletManagement;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.KycStatus;
import com.example.admin_api_service.enums.KycTier;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;

@Entity
@Table(name = "user_kyc")
public class UserKyc {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", length = 20, nullable = false)
    private KycTier tier = KycTier.TIER_0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private KycStatus status = KycStatus.NOT_STARTED;

    // Personal information
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    // Address
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // BVN / NIN or other national identifier
    @Column(name = "national_id_number", length = 50)
    private String nationalIdNumber;

    @Column(name = "bvn", length = 20)
    private String bvn;

    @Column(name = "bvn_verified", nullable = false)
    private boolean bvnVerified = false;

    @Column(name = "bvn_verified_at")
    private LocalDateTime bvnVerifiedAt;

    // Review trail
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    // Linked approval request if review went through a workflow
    @Column(name = "approval_request_id", length = 36)
    private String approvalRequestId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "userKyc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KycDocument> documents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", insertable = false, updatable = false)
    private ApprovalRequest approvalRequest;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public UserKyc() {
    }

    public UserKyc(String id, Long userId, KycTier tier, KycStatus status, String firstName, String middleName, String lastName, LocalDate dateOfBirth, String gender, String nationality, String phoneNumber, String email, String addressLine1, String addressLine2, String city, String state, String country, String postalCode, String nationalIdNumber, String bvn, boolean bvnVerified, LocalDateTime bvnVerifiedAt, LocalDateTime submittedAt, String reviewedBy, LocalDateTime reviewedAt, String rejectionReason, String reviewNote, String approvalRequestId, LocalDateTime expiresAt, LocalDateTime createdOn, LocalDateTime updatedOn, List<KycDocument> documents, ApprovalRequest approvalRequest) {
        this.id = id;
        this.userId = userId;
        this.tier = tier;
        this.status = status;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.nationalIdNumber = nationalIdNumber;
        this.bvn = bvn;
        this.bvnVerified = bvnVerified;
        this.bvnVerifiedAt = bvnVerifiedAt;
        this.submittedAt = submittedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.reviewNote = reviewNote;
        this.approvalRequestId = approvalRequestId;
        this.expiresAt = expiresAt;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.documents = documents;
        this.approvalRequest = approvalRequest;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public KycTier getTier() { return tier; }
    public void setTier(KycTier tier) { this.tier = tier; }

    public KycStatus getStatus() { return status; }
    public void setStatus(KycStatus status) { this.status = status; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getNationalIdNumber() { return nationalIdNumber; }
    public void setNationalIdNumber(String nationalIdNumber) { this.nationalIdNumber = nationalIdNumber; }

    public String getBvn() { return bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }

    public boolean isBvnVerified() { return bvnVerified; }
    public void setBvnVerified(boolean bvnVerified) { this.bvnVerified = bvnVerified; }

    public LocalDateTime getBvnVerifiedAt() { return bvnVerifiedAt; }
    public void setBvnVerifiedAt(LocalDateTime bvnVerifiedAt) { this.bvnVerifiedAt = bvnVerifiedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public String getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<KycDocument> getDocuments() { return documents; }
    public void setDocuments(List<KycDocument> documents) { this.documents = documents; }

    public ApprovalRequest getApprovalRequest() { return approvalRequest; }
    public void setApprovalRequest(ApprovalRequest approvalRequest) { this.approvalRequest = approvalRequest; }
}