package com.epay.auth.repository;

import com.epay.auth.domain.entity.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {

    Optional<UserRecord> findByUserId(Long userId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByReferralCode(String referralCode);

    Optional<UserRecord> findByReferralCode(String referralCode);

    @Modifying
    @Query("UPDATE UserRecord r SET r.profileComplete = true WHERE r.user.id = :userId")
    void markProfileComplete(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserRecord r SET r.totalReferrals = r.totalReferrals + 1 WHERE r.referralCode = :referralCode")
    void incrementReferralCount(@Param("referralCode") String referralCode);
}
