package pesco.example.authentication_service.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Builder;
import lombok.Data;
import pesco.example.authentication_service.enums.UserStatus;


@Data
@Builder
@Entity
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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

    public UserRecord() {
    }


    public UserRecord(Long id, Users user, String firstName, String lastName, String telephone, String gender, String country, String city, String state, String nextOfKing, String dateofBirth, String address, boolean isTransferPinSet, boolean locked, LocalDateTime lockedAt, boolean isBlocked, Long blockedDuration, String blockedUntil, String blockedReason, UserStatus status, String referralCode, String totalReferers, String referralLink, String referralUsername, String photo, boolean isProfileComplete) {
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
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return this.user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNextOfKing() {
        return this.nextOfKing;
    }

    public void setNextOfKing(String nextOfKing) {
        this.nextOfKing = nextOfKing;
    }

    public String getDateofBirth() {
        return this.dateofBirth;
    }

    public void setDateofBirth(String dateofBirth) {
        this.dateofBirth = dateofBirth;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isIsTransferPinSet() {
        return this.isTransferPinSet;
    }

    public boolean getIsTransferPinSet() {
        return this.isTransferPinSet;
    }

    public void setIsTransferPinSet(boolean isTransferPinSet) {
        this.isTransferPinSet = isTransferPinSet;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean getLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public LocalDateTime getLockedAt() {
        return this.lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public boolean isIsBlocked() {
        return this.isBlocked;
    }

    public boolean getIsBlocked() {
        return this.isBlocked;
    }

    public void setIsBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public Long getBlockedDuration() {
        return this.blockedDuration;
    }

    public void setBlockedDuration(Long blockedDuration) {
        this.blockedDuration = blockedDuration;
    }

    public String getBlockedUntil() {
        return this.blockedUntil;
    }

    public void setBlockedUntil(String blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public String getBlockedReason() {
        return this.blockedReason;
    }

    public void setBlockedReason(String blockedReason) {
        this.blockedReason = blockedReason;
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getReferralCode() {
        return this.referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getTotalReferers() {
        return this.totalReferers;
    }

    public void setTotalReferers(String totalReferers) {
        this.totalReferers = totalReferers;
    }

    public String getReferralLink() {
        return this.referralLink;
    }

    public void setReferralLink(String referralLink) {
        this.referralLink = referralLink;
    }

    public String getReferralUsername() {
        return this.referralUsername;
    }

    public void setReferralUsername(String referralUsername) {
        this.referralUsername = referralUsername;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isIsProfileComplete() {
        return this.isProfileComplete;
    }

    public boolean getIsProfileComplete() {
        return this.isProfileComplete;
    }

    public void setIsProfileComplete(boolean isProfileComplete) {
        this.isProfileComplete = isProfileComplete;
    }    

    @PrePersist
    @PreUpdate
    protected void beforeSaveOrUpdate() {
        // Sanitize inputs
        this.firstName = sanitizeInput(this.firstName);
        this.lastName = sanitizeInput(this.lastName);
        this.telephone = sanitizeInput(this.telephone);
        this.gender = sanitizeInput(this.gender);
        this.country = sanitizeInput(this.country);
        this.city = sanitizeInput(this.city);
        this.state = sanitizeInput(this.state);
        this.nextOfKing = sanitizeInput(this.nextOfKing);
        this.address = sanitizeInput(this.address);
        this.blockedUntil = sanitizeInput(this.blockedUntil);
        this.blockedReason = sanitizeInput(this.blockedReason);
        this.referralCode = sanitizeInput(this.referralCode);
        this.totalReferers = sanitizeInput(this.totalReferers);
        this.referralLink = sanitizeInput(this.referralLink);
        this.referralUsername = sanitizeInput(this.referralUsername);  
    }


    private String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove script tags
        String sanitized = input.replaceAll("<script.*?>.*?</script>", "")
                               .replaceAll("javascript:", "")
                               .replaceAll("onerror=", "")
                               .replaceAll("onload=", "")
                               .replaceAll("onclick=", "")
                               .replaceAll("eval\\(", "");
        
        // Escape HTML entities
        sanitized = sanitized.replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&#x27;");
        
        return sanitized.trim();
    }

}
