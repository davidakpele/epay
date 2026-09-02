package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT t FROM PasswordResetToken t WHERE t.tokenHash = :tokenHash " +
           "AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("tokenHash") String tokenHash,
                                                @Param("now") LocalDateTime now);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId " +
           "ORDER BY t.createdAt DESC LIMIT 1")
    Optional<PasswordResetToken> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true " +
           "WHERE t.user.id = :userId AND t.used = false")
    void invalidateAllForUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :before")
    void deleteExpiredTokens(@Param("before") LocalDateTime before);
}
