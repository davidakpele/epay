package com.epay.blacklist.repository;

import com.epay.blacklist.domain.entity.BlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntry, Long> {

    @Query("SELECT COUNT(b) > 0 FROM BlacklistEntry b WHERE b.type = :type AND b.value = :value " +
           "AND b.active = true AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    boolean existsActiveByTypeAndValue(@Param("type") String type,
                                        @Param("value") String value,
                                        @Param("now") LocalDateTime now);
}
