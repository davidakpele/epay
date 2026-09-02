package com.epay.domain.developer.repository;

import com.epay.domain.developer.entity.ApiKey;
import com.epay.domain.developer.enums.ApiKeyStatus;
import com.epay.domain.developer.enums.ApiMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByPublicKey(String publicKey);

    Optional<ApiKey> findByAppIdAndMode(Long appId, ApiMode mode);

    boolean existsByPublicKey(String publicKey);

    @Modifying
    @Query("UPDATE ApiKey k SET k.status = :status, k.revokedAt = :at, k.revokedBy = :by, k.revokeReason = :reason WHERE k.id = :id")
    void revokeKey(@Param("id") Long id,
                   @Param("status") ApiKeyStatus status,
                   @Param("at") LocalDateTime at,
                   @Param("by") Long revokedBy,
                   @Param("reason") String reason);

    @Modifying
    @Query("UPDATE ApiKey k SET k.lastUsedAt = :now, k.requestCount = k.requestCount + 1 WHERE k.id = :id")
    void recordUsage(@Param("id") Long id, @Param("now") LocalDateTime now);
}
