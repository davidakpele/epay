package com.payrix.administrator.dtos;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRecordDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private String country;
    private String city;
    private boolean isTransferPin;
    private boolean locked;
    private LocalDateTime lockedAt;
    private String referralCode;
    private boolean isBlocked;
    private Long blockedDuration;
    private String blockedUntil;
    private String blockedReason;
    private String totalRefs;
    private String notifications;
    private String referralLink;
    private String photo;


    public UserRecordDTO() {
    }

    public UserRecordDTO(Long id, String firstName, String lastName, String gender, String country, String city, boolean isTransferPin, boolean locked, LocalDateTime lockedAt, String referralCode, boolean isBlocked, Long blockedDuration, String blockedUntil, String blockedReason, String totalRefs, String notifications, String referralLink, String photo) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.country = country;
        this.city = city;
        this.isTransferPin = isTransferPin;
        this.locked = locked;
        this.lockedAt = lockedAt;
        this.referralCode = referralCode;
        this.isBlocked = isBlocked;
        this.blockedDuration = blockedDuration;
        this.blockedUntil = blockedUntil;
        this.blockedReason = blockedReason;
        this.totalRefs = totalRefs;
        this.notifications = notifications;
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

    public boolean isIsTransferPin() {
        return this.isTransferPin;
    }

    public boolean getIsTransferPin() {
        return this.isTransferPin;
    }

    public void setIsTransferPin(boolean isTransferPin) {
        this.isTransferPin = isTransferPin;
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

    public String getReferralCode() {
        return this.referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
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

    public String getTotalRefs() {
        return this.totalRefs;
    }

    public void setTotalRefs(String totalRefs) {
        this.totalRefs = totalRefs;
    }

    public String getNotifications() {
        return this.notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
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

}