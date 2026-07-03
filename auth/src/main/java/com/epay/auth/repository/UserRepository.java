package com.epay.auth.repository;

import com.epay.auth.domain.entity.User;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameIn(List<String> usernames);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.record WHERE u.id = :id")
    Optional<User> findByIdWithRecord(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateEnabled(@Param("userId") Long userId, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true, u.accountLockedAt = :lockedAt, " +
           "u.accountLockedReason = :reason WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId,
                     @Param("lockedAt") LocalDateTime lockedAt,
                     @Param("reason") String reason);

    @Modifying
    @Query("UPDATE User u SET u.accountLocked = false, u.accountLockedAt = null, " +
           "u.accountLockedReason = null, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId,
                         @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Modifying
    @Query("UPDATE User u SET u.password = :hashedPassword WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId,
                        @Param("hashedPassword") String hashedPassword);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void markEmailVerified(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = true WHERE u.id = :userId")
    void markPhoneVerified(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.twoFactorEnabled = :enabled WHERE u.id = :userId")
    void updateTwoFactorEnabled(@Param("userId") Long userId,
                                @Param("enabled") boolean enabled);
    @Modifying
    @Query("UPDATE User u SET u.kycStatus = :status WHERE u.id = :userId")
    void updateKycStatus(@Param("userId") Long userId,
                         @Param("status") KycStatus status);

    @Modifying
    @Query("UPDATE User u SET u.kycTier = :tier, u.kycStatus = :status WHERE u.id = :userId")
    void updateKycTierAndStatus(@Param("userId") Long userId,
                                @Param("tier") KycTier tier,
                                @Param("status") KycStatus status);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByKycStatus(KycStatus kycStatus, Pageable pageable);

    Page<User> findByEnabledFalseAndCreatedAtBefore(LocalDateTime before, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.role = :role")
    long countActiveByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = false AND u.role = :role")
    long countInactiveByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.kycStatus = :status")
    long countByKycStatus(@Param("status") KycStatus status);

    @Query("""
           SELECT CAST(FUNCTION('DATE', u.createdAt) AS string) AS label,
                  COUNT(u) AS count
           FROM   User u
           WHERE  u.createdAt >= :since AND u.role = 'USER'
           GROUP  BY FUNCTION('DATE', u.createdAt)
           ORDER  BY FUNCTION('DATE', u.createdAt) ASC
           """)
    List<Object[]> countDailyRegistrations(@Param("since") LocalDateTime since);

    @Query("""
           SELECT FUNCTION('YEAR', u.createdAt)  AS year,
                  FUNCTION('WEEK', u.createdAt)  AS week,
                  COUNT(u)                        AS count
           FROM   User u
           WHERE  u.createdAt >= :since AND u.role = 'USER'
           GROUP  BY FUNCTION('YEAR', u.createdAt), FUNCTION('WEEK', u.createdAt)
           ORDER  BY FUNCTION('YEAR', u.createdAt), FUNCTION('WEEK', u.createdAt) ASC
           """)
    List<Object[]> countWeeklyRegistrations(@Param("since") LocalDateTime since);

    @Query("""
           SELECT FUNCTION('YEAR', u.createdAt)  AS year,
                  FUNCTION('MONTH', u.createdAt) AS month,
                  COUNT(u)                        AS count
           FROM   User u
           WHERE  u.createdAt >= :since AND u.role = 'USER'
           GROUP  BY FUNCTION('YEAR', u.createdAt), FUNCTION('MONTH', u.createdAt)
           ORDER  BY FUNCTION('YEAR', u.createdAt), FUNCTION('MONTH', u.createdAt) ASC
           """)
    List<Object[]> countMonthlyRegistrations(@Param("since") LocalDateTime since);

    @Query("""
           SELECT FUNCTION('YEAR', u.createdAt) AS year,
                  COUNT(u)                       AS count
           FROM   User u
           WHERE  u.role = 'USER'
           GROUP  BY FUNCTION('YEAR', u.createdAt)
           ORDER  BY FUNCTION('YEAR', u.createdAt) ASC
           """)
    List<Object[]> countYearlyRegistrations();
}
