package com.epay.auth.repository;

import com.epay.auth.domain.entity.UserRecord;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByReferralCode(String referralCode);

    Optional<UserRecord> findByReferralCode(String referralCode);

    @Modifying
    @Query("UPDATE UserRecord r SET r.profileComplete = true WHERE r.user.id = :userId")
    void markProfileComplete(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserRecord r SET r.totalReferrals = r.totalReferrals + 1 WHERE r.referralCode = :referralCode")
    void incrementReferralCount(@Param("referralCode") String referralCode);

    @Query("SELECT r FROM UserRecord r WHERE r.user.id = :userId")
    Optional<UserRecord> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM UserRecord r WHERE r.user.id = :userId AND r.locked = true")
    boolean isUserAccountLocked(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM UserRecord r WHERE r.user.id = :userId AND r.isBlocked = true")
    boolean isUserAccountBlocked(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRecord r WHERE r.user.id IN :ids")
    void deleteUserRecordByIxds(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM UserRecord r WHERE r.telephone = :telephone")
    Optional<UserRecord> findByTelephone(@Param("telephone") String telephone);

}
