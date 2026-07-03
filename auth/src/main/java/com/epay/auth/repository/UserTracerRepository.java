package com.epay.auth.repository;

import com.epay.auth.domain.entity.UserTracer;
import com.epay.domain.auth.enums.AttemptType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserTracerRepository extends JpaRepository<UserTracer, Long> {

    Page<UserTracer> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<UserTracer> findByUserIdAndAttemptTypeOrderByCreatedAtDesc(
            Long userId, AttemptType attemptType, Pageable pageable);

    @Query("SELECT COUNT(t) FROM UserTracer t WHERE t.user.id = :userId " +
           "AND t.attemptType = 'LOGIN' AND t.success = false AND t.createdAt > :since")
    long countRecentFailedLogins(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) > 0 FROM UserTracer t WHERE t.user.id = :userId " +
           "AND t.ipAddress = :ip AND t.success = true")
    boolean hasSuccessfulLoginFromIp(@Param("userId") Long userId, @Param("ip") String ipAddress);

    Page<UserTracer> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);
}
