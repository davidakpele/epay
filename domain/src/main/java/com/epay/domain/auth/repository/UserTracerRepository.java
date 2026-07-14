package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.UserTracer;
import com.epay.domain.auth.enums.AttemptType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserTracerRepository extends JpaRepository<UserTracer, Long> {

    @Query("SELECT t FROM UserTracer t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    Page<UserTracer> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM UserTracer t WHERE t.user.id = :userId AND t.attemptType = :type ORDER BY t.createdAt DESC")
    Page<UserTracer> findByUserIdAndAttemptType(@Param("userId") Long userId,
                                                @Param("type") AttemptType type,
                                                Pageable pageable);

    @Query("SELECT t FROM UserTracer t WHERE t.ipAddress = :ip ORDER BY t.createdAt DESC")
    Page<UserTracer> findByIpAddress(@Param("ip") String ipAddress, Pageable pageable);

    Optional<UserTracer> findBySessionId(String sessionId);

    @Query("SELECT COUNT(t) FROM UserTracer t " +
           "WHERE t.user.id = :userId " +
           "AND t.attemptType = com.epay.domain.auth.enums.AttemptType.LOGIN " +
           "AND t.success = false AND t.createdAt > :since")
    long countRecentFailedLogins(@Param("userId") Long userId,
                                 @Param("since") LocalDateTime since);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM UserTracer t " +
           "WHERE t.user.id = :userId AND t.ipAddress = :ip AND t.success = true")
    boolean hasSuccessfulLoginFromIp(@Param("userId") Long userId,
                                     @Param("ip") String ipAddress);

    @Modifying
    @Query("DELETE FROM UserTracer t WHERE t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
