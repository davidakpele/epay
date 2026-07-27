package com.example.auth_user_service.models;

import java.time.LocalDateTime;
import com.example.auth_user_service.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@Entity
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private Users user;

    private String firstName;
    private String lastName;
    private String telephone;
    private String gender;
    private String country;
    private String city;
    private String state;
    private String nextOfKing;
    private String dateofBirth;
    private String address;
    private boolean isTransferPinSet;
    private boolean locked;
    private LocalDateTime lockedAt;
    private boolean isBlocked;
    private Long blockedDuration;
    private String blockedUntil;
    private String blockedReason;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String referralCode;
    private String totalReferers;
    private String referralLink;
    private String referralUsername;
    private String photo;
    private boolean isProfileComplete;

    // KYC documents
    private String passportDoc;
    private String utilityBillDoc;

    public UserRecord() {}

    public UserRecord(Long id, Users user, String firstName, String lastName, String telephone, String gender, String country,
                      String city, String state, String nextOfKing, String dateofBirth, String address, boolean isTransferPinSet,
                      boolean locked, LocalDateTime lockedAt, boolean isBlocked, Long blockedDuration, String blockedUntil,
                      String blockedReason, UserStatus status, String referralCode, String totalReferers, String referralLink,
                      String referralUsername, String photo, boolean isProfileComplete,
                      String passportDoc, String utilityBillDoc) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.gender = gender;
        this.country = country;
        this.city = city;
        this.state = state;
        this.nextOfKing = nextOfKing;
        this.dateofBirth = dateofBirth;
        this.address = address;
        this.isTransferPinSet = isTransferPinSet;
        this.locked = locked;
        this.lockedAt = lockedAt;
        this.isBlocked = isBlocked;
        this.blockedDuration = blockedDuration;
        this.blockedUntil = blockedUntil;
        this.blockedReason = blockedReason;
        this.status = status;
        this.referralCode = referralCode;
        this.totalReferers = totalReferers;
        this.referralLink = referralLink;
        this.referralUsername = referralUsername;
        this.photo = photo;
        this.isProfileComplete = isProfileComplete;
        this.passportDoc = passportDoc;
        this.utilityBillDoc = utilityBillDoc;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getNextOfKing() { return nextOfKing; }
    public void setNextOfKing(String nextOfKing) { this.nextOfKing = nextOfKing; }

    public String getDateofBirth() { return dateofBirth; }
    public void setDateofBirth(String dateofBirth) { this.dateofBirth = dateofBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isTransferPinSet() { return isTransferPinSet; }
    public void setTransferPinSet(boolean transferPinSet) { this.isTransferPinSet = transferPinSet; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }

    public Long getBlockedDuration() { return blockedDuration; }
    public void setBlockedDuration(Long blockedDuration) { this.blockedDuration = blockedDuration; }

    public String getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(String blockedUntil) { this.blockedUntil = blockedUntil; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public String getTotalReferers() { return totalReferers; }
    public void setTotalReferers(String totalReferers) { this.totalReferers = totalReferers; }

    public String getReferralLink() { return referralLink; }
    public void setReferralLink(String referralLink) { this.referralLink = referralLink; }

    public String getReferralUsername() { return referralUsername; }
    public void setReferralUsername(String referralUsername) { this.referralUsername = referralUsername; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public boolean isProfileComplete() { return isProfileComplete; }
    public boolean getIsProfileComplete() { return isProfileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.isProfileComplete = profileComplete; }

    public String getPassportDoc() { return passportDoc; }
    public void setPassportDoc(String passportDoc) { this.passportDoc = passportDoc; }

    public String getUtilityBillDoc() { return utilityBillDoc; }
    public void setUtilityBillDoc(String utilityBillDoc) { this.utilityBillDoc = utilityBillDoc; }

    @PrePersist
    @PreUpdate
    protected void beforeSaveOrUpdate() {
        this.firstName = sanitizeInput(firstName);
        this.lastName = sanitizeInput(lastName);
        this.telephone = sanitizeInput(telephone);
        this.gender = sanitizeInput(gender);
        this.country = sanitizeInput(country);
        this.city = sanitizeInput(city);
        this.state = sanitizeInput(state);
        this.nextOfKing = sanitizeInput(nextOfKing);
        this.address = sanitizeInput(address);
        this.blockedUntil = sanitizeInput(blockedUntil);
        this.blockedReason = sanitizeInput(blockedReason);
        this.referralCode = sanitizeInput(referralCode);
        this.totalReferers = sanitizeInput(totalReferers);
        this.referralLink = sanitizeInput(referralLink);
        this.referralUsername = sanitizeInput(referralUsername);
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        String sanitized = input.replaceAll("<script.*?>.*?</script>", "")
                               .replaceAll("javascript:", "")
                               .replaceAll("onerror=", "")
                               .replaceAll("onload=", "")
                               .replaceAll("onclick=", "")
                               .replaceAll("eval\\(", "");
        sanitized = sanitized.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;")
                             .replace("\"", "&quot;")
                             .replace("'", "&#x27;");
        return sanitized.trim();
    }
}