package com.epay.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Extended profile information for a user.
 * Separated from User to keep the auth/security entity lean.
 * One-to-one: every User has at most one UserRecord.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_records", indexes = {
        @Index(name = "idx_user_record_user_id", columnList = "user_id"),
        @Index(name = "idx_user_record_referral_code", columnList = "referral_code")
})
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_record_seq")
    @SequenceGenerator(name = "user_record_seq", sequenceName = "user_record_sequence", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    private String gender;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 500)
    private String address;

    @Column(name = "referral_code", unique = true, length = 36)
    private String referralCode;

    @Column(name = "referred_by_code", length = 36)
    private String referredByCode;

    @Column(name = "total_referrals", nullable = false)
    @Builder.Default
    private Integer totalReferrals = 0;

    @Column(name = "profile_photo_url", length = 1000)
    private String profilePhotoUrl;

    @Column(name = "profile_complete", nullable = false)
    @Builder.Default
    private boolean profileComplete = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
