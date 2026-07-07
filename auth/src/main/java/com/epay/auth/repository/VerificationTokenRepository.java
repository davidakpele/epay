package com.epay.auth.repository;

import com.epay.domain.auth.entity.VerificationToken;
import com.epay.domain.auth.enums.TokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Query("SELECT t FROM VerificationToken t WHERE t.tokenHash = :tokenHash " +
           "AND t.purpose = :purpose AND t.used = false AND t.expiresAt > :now")
    Optional<VerificationToken> findValidToken(@Param("tokenHash") String tokenHash,
                                               @Param("purpose") TokenPurpose purpose,
                                               @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VerificationToken t SET t.used = true " +
           "WHERE t.user.id = :userId AND t.purpose = :purpose AND t.used = false")
    void invalidateAllForUserAndPurpose(@Param("userId") Long userId,
                                        @Param("purpose") TokenPurpose purpose);

    boolean existsByUserIdAndPurposeAndUsedFalseAndExpiresAtAfter(
            Long userId, TokenPurpose purpose, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :before AND t.used = true")
    void deleteExpiredTokens(@Param("before") LocalDateTime before);

    @Query("SELECT v FROM VerificationToken v WHERE v.token=:token")
    VerificationToken findByToken(String token);
}
