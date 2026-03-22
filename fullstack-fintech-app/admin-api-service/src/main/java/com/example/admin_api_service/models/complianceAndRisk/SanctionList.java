package com.example.admin_api_service.models.complianceAndRisk;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.SanctionListStatus;
import com.example.admin_api_service.enums.SanctionListType;

@Entity
@Table(name = "sanction_lists")
public class SanctionList {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Name on the sanction list (individual or entity)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "alias_names", columnDefinition = "json")
    private String aliasNames;

    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", length = 30, nullable = false)
    private SanctionListType listType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SanctionListStatus status = SanctionListStatus.ACTIVE;

    // Issuing body: OFAC, UN, EU, CBN, EFCC, etc.
    @Column(name = "issuing_authority", length = 100, nullable = false)
    private String issuingAuthority;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "date_of_birth", length = 20)
    private String dateOfBirth;

    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Column(name = "passport_number", length = 50)
    private String passportNumber;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "reason", length = 1000)
    private String reason;

    // Full raw entry from the source list stored as JSON
    @Column(name = "raw_data", columnDefinition = "json")
    private String rawData;

    @Column(name = "listed_on")
    private LocalDateTime listedOn;

    @Column(name = "delisted_on")
    private LocalDateTime delistedOn;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public SanctionList() {
    }

    public SanctionList(String id, String name, String aliasNames, SanctionListType listType, SanctionListStatus status, String issuingAuthority, String referenceNumber, String nationality, String dateOfBirth, String nationalId, String passportNumber, String address, String reason, String rawData, LocalDateTime listedOn, LocalDateTime delistedOn, String sourceUrl, LocalDateTime lastSyncedAt, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.name = name;
        this.aliasNames = aliasNames;
        this.listType = listType;
        this.status = status;
        this.issuingAuthority = issuingAuthority;
        this.referenceNumber = referenceNumber;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.nationalId = nationalId;
        this.passportNumber = passportNumber;
        this.address = address;
        this.reason = reason;
        this.rawData = rawData;
        this.listedOn = listedOn;
        this.delistedOn = delistedOn;
        this.sourceUrl = sourceUrl;
        this.lastSyncedAt = lastSyncedAt;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAliasNames() { return aliasNames; }
    public void setAliasNames(String aliasNames) { this.aliasNames = aliasNames; }

    public SanctionListType getListType() { return listType; }
    public void setListType(SanctionListType listType) { this.listType = listType; }

    public SanctionListStatus getStatus() { return status; }
    public void setStatus(SanctionListStatus status) { this.status = status; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public LocalDateTime getListedOn() { return listedOn; }
    public void setListedOn(LocalDateTime listedOn) { this.listedOn = listedOn; }

    public LocalDateTime getDelistedOn() { return delistedOn; }
    public void setDelistedOn(LocalDateTime delistedOn) { this.delistedOn = delistedOn; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}
