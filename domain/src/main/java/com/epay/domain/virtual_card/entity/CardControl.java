package com.epay.domain.virtual_card.entity;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;

@Data
@Builder
@Entity
@Table(name = "card_controls")
public class CardControl {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "card_id", nullable = false, unique = true)
    private String cardId;
    
    @Column(name = "allow_international")
    private Boolean allowInternational;
    
    @Column(name = "allow_online")
    private Boolean allowOnline;
    
    @Column(name = "allow_atm")
    private Boolean allowAtm;
    
    @Column(name = "allow_contactless")
    private Boolean allowContactless;
    
    @Column(name = "allow_chip")
    private Boolean allowChip;
    
    @Column(name = "allow_swipe")
    private Boolean allowSwipe;
    
    @ElementCollection
    @CollectionTable(name = "card_allowed_countries", 
                    joinColumns = @JoinColumn(name = "control_id"))
    @Column(name = "country_code")
    private Set<String> allowedCountries;
    
    @ElementCollection
    @CollectionTable(name = "card_blocked_countries", 
                    joinColumns = @JoinColumn(name = "control_id"))
    @Column(name = "country_code")
    private Set<String> blockedCountries;
    
    @ElementCollection
    @CollectionTable(name = "card_allowed_merchant_categories", 
                    joinColumns = @JoinColumn(name = "control_id"))
    @Column(name = "mcc")
    private Set<String> allowedMerchantCategories;
    
    @ElementCollection
    @CollectionTable(name = "card_blocked_merchant_categories", 
                    joinColumns = @JoinColumn(name = "control_id"))
    @Column(name = "mcc")
    private Set<String> blockedMerchantCategories;
    
    @ElementCollection
    @CollectionTable(name = "card_blocked_merchants", 
                    joinColumns = @JoinColumn(name = "control_id"))
    @Column(name = "merchant_id")
    private Set<String> blockedMerchants; 
    
    @Column(name = "valid_from_time")
    private LocalTime validFromTime; 
    
    @Column(name = "valid_to_time")
    private LocalTime validToTime; 
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public CardControl() {
    }

    public CardControl(String id, String cardId, Boolean allowInternational, Boolean allowOnline, Boolean allowAtm, Boolean allowContactless, Boolean allowChip, Boolean allowSwipe, Set<String> allowedCountries, Set<String> blockedCountries, Set<String> allowedMerchantCategories, Set<String> blockedMerchantCategories, Set<String> blockedMerchants, LocalTime validFromTime, LocalTime validToTime, LocalDateTime updatedAt) {
        this.id = id;
        this.cardId = cardId;
        this.allowInternational = allowInternational;
        this.allowOnline = allowOnline;
        this.allowAtm = allowAtm;
        this.allowContactless = allowContactless;
        this.allowChip = allowChip;
        this.allowSwipe = allowSwipe;
        this.allowedCountries = allowedCountries;
        this.blockedCountries = blockedCountries;
        this.allowedMerchantCategories = allowedMerchantCategories;
        this.blockedMerchantCategories = blockedMerchantCategories;
        this.blockedMerchants = blockedMerchants;
        this.validFromTime = validFromTime;
        this.validToTime = validToTime;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Boolean isAllowInternational() {
        return this.allowInternational;
    }

    public Boolean getAllowInternational() {
        return this.allowInternational;
    }

    public void setAllowInternational(Boolean allowInternational) {
        this.allowInternational = allowInternational;
    }

    public Boolean isAllowOnline() {
        return this.allowOnline;
    }

    public Boolean getAllowOnline() {
        return this.allowOnline;
    }

    public void setAllowOnline(Boolean allowOnline) {
        this.allowOnline = allowOnline;
    }

    public Boolean isAllowAtm() {
        return this.allowAtm;
    }

    public Boolean getAllowAtm() {
        return this.allowAtm;
    }

    public void setAllowAtm(Boolean allowAtm) {
        this.allowAtm = allowAtm;
    }

    public Boolean isAllowContactless() {
        return this.allowContactless;
    }

    public Boolean getAllowContactless() {
        return this.allowContactless;
    }

    public void setAllowContactless(Boolean allowContactless) {
        this.allowContactless = allowContactless;
    }

    public Boolean isAllowChip() {
        return this.allowChip;
    }

    public Boolean getAllowChip() {
        return this.allowChip;
    }

    public void setAllowChip(Boolean allowChip) {
        this.allowChip = allowChip;
    }

    public Boolean isAllowSwipe() {
        return this.allowSwipe;
    }

    public Boolean getAllowSwipe() {
        return this.allowSwipe;
    }

    public void setAllowSwipe(Boolean allowSwipe) {
        this.allowSwipe = allowSwipe;
    }

    public Set<String> getAllowedCountries() {
        return this.allowedCountries;
    }

    public void setAllowedCountries(Set<String> allowedCountries) {
        this.allowedCountries = allowedCountries;
    }

    public Set<String> getBlockedCountries() {
        return this.blockedCountries;
    }

    public void setBlockedCountries(Set<String> blockedCountries) {
        this.blockedCountries = blockedCountries;
    }

    public Set<String> getAllowedMerchantCategories() {
        return this.allowedMerchantCategories;
    }

    public void setAllowedMerchantCategories(Set<String> allowedMerchantCategories) {
        this.allowedMerchantCategories = allowedMerchantCategories;
    }

    public Set<String> getBlockedMerchantCategories() {
        return this.blockedMerchantCategories;
    }

    public void setBlockedMerchantCategories(Set<String> blockedMerchantCategories) {
        this.blockedMerchantCategories = blockedMerchantCategories;
    }

    public Set<String> getBlockedMerchants() {
        return this.blockedMerchants;
    }

    public void setBlockedMerchants(Set<String> blockedMerchants) {
        this.blockedMerchants = blockedMerchants;
    }

    public LocalTime getValidFromTime() {
        return this.validFromTime;
    }

    public void setValidFromTime(LocalTime validFromTime) {
        this.validFromTime = validFromTime;
    }

    public LocalTime getValidToTime() {
        return this.validToTime;
    }

    public void setValidToTime(LocalTime validToTime) {
        this.validToTime = validToTime;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}