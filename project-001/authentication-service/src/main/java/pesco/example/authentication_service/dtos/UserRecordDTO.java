package pesco.example.authentication_service.dtos;

import lombok.Data;
import pesco.example.authentication_service.models.UserRecord;
import java.time.LocalDateTime;

@Data
public class UserRecordDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String telephone;
    private String gender;
    private String country;
    private String city;
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
    private String referralCode;
    private String totalReferers;
    private String referralLink;
    private String photo;

    public UserRecordDTO() {
    }

    public UserRecordDTO(Long id, String firstName, String lastName, String telephone, String gender, String country, String city, String nextOfKing, String dateofBirth, String address, boolean isTransferPinSet, boolean locked, LocalDateTime lockedAt, boolean isBlocked, Long blockedDuration, String blockedUntil, String blockedReason, String referralCode, String totalReferers, String referralLink, String photo) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.gender = gender;
        this.country = country;
        this.city = city;
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
        this.referralCode = referralCode;
        this.totalReferers = totalReferers;
        this.referralLink = referralLink;
        this.photo = photo;
    }
    

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public static UserRecordDTO fromEntity(UserRecord record) {
        return new UserRecordDTO(
            record.getId(),
            record.getFirstName(),
            record.getLastName(),
            record.getTelephone(),
            record.getGender(),
            record.getCountry(),
            record.getCity(),
            record.getNextOfKing(),
            record.getDateofBirth(),
            record.getAddress(),
            record.isIsTransferPinSet(),
            record.isLocked(),
            record.getLockedAt(),
            record.isIsBlocked(),
            record.getBlockedDuration(),
            record.getBlockedUntil(),
            record.getBlockedReason(),
            record.getReferralCode(),
            record.getTotalReferers(),
            record.getReferralLink(),
            record.getPhoto()
        );
    }
}