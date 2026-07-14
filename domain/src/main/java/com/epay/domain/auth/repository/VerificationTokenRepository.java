package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query("SELECT t FROM VerificationToken t WHERE t.token = :token")
    VerificationToken findByToken(@Param("token") String token);

    @Query("SELECT t FROM VerificationToken t WHERE t.token = :token")
    Optional<VerificationToken> findOptionalByToken(@Param("token") String token);

    @Query("SELECT t FROM VerificationToken t WHERE t.userId = :userId")
    Optional<VerificationToken> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM VerificationToken t " +
           "WHERE t.userId = :userId AND t.expirationTime > :now")
    boolean existsValidTokenByUserId(@Param("userId") Long userId,
                                     @Param("now") Date now);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expirationTime < :before")
    void deleteExpiredTokens(@Param("before") Date before);
}
