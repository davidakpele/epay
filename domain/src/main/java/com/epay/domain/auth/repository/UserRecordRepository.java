package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {

    @Query("SELECT r FROM UserRecord r WHERE r.user.id = :userId")
    Optional<UserRecord> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM UserRecord r WHERE r.phoneNumber = :phone")
    Optional<UserRecord> findByPhoneNumber(@Param("phone") String phoneNumber);

    @Query("SELECT r FROM UserRecord r WHERE r.referralCode = :code")
    Optional<UserRecord> findByReferralCode(@Param("code") String referralCode);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM UserRecord r WHERE r.phoneNumber = :phone")
    boolean existsByPhoneNumber(@Param("phone") String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM UserRecord r WHERE r.referralCode = :code")
    boolean existsByReferralCode(@Param("code") String referralCode);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM UserRecord r WHERE r.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserRecord r SET r.profileComplete = true WHERE r.user.id = :userId")
    void markProfileComplete(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserRecord r SET r.totalReferrals = r.totalReferrals + 1 " +
           "WHERE r.referralCode = :referralCode")
    void incrementReferralCount(@Param("referralCode") String referralCode);
}
